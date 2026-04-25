package com.practice.cursor.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.cursor.domain.member.entity.Member;
import com.practice.cursor.domain.member.entity.Role;
import com.practice.cursor.domain.member.repository.MemberRepository;
import com.practice.cursor.domain.todo.entity.Todo;
import com.practice.cursor.domain.todo.repository.TodoRepository;
import com.practice.cursor.global.service.RedisTokenService;
import com.practice.cursor.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Todo 도메인 E2E 통합 테스트.
 * Controller와 Repository를 전부 관통하는 흐름을 검증한다.
 * IntegrationTestSupport를 상속하여 실제 Spring Context와 DB를 사용한다.
 *
 * @Transactional을 클래스 레벨에 선언하여 데이터 격리를 보장한다.
 */
@Transactional
@AutoConfigureMockMvc
class TodoIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RedisTokenService redisTokenService;

    @Test
    @DisplayName("할 일 등록부터 완료까지 전체 플로우가 정상 동작한다")
    void todoCompleteFlow_registeredTodo_completesSuccessfully() throws Exception {
        // given
        String accessToken = authenticate("integration-user", "password123", "통합테스터");

        // when - 할 일 등록
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", bearer(accessToken))
                        .content("""
                                {
                                  "title": "통합 테스트",
                                  "content": "E2E 플로우 검증"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("통합 테스트"))
                .andReturn();

        Long todoId = readLong(createResult, "/data/id");

        // when - 할 일 완료 처리
        mockMvc.perform(patch("/api/todos/{id}/complete", todoId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(todoId))
                .andExpect(jsonPath("$.data.completed").value(true));

        // then - DB 상태 검증
        Todo todoFromDb = todoRepository.findById(todoId).orElseThrow();
        assertThat(todoFromDb.isCompleted()).isTrue();
        assertThat(todoFromDb.isDeleted()).isFalse();
        assertThat(todoFromDb.getTitle()).isEqualTo("통합 테스트");
    }

    @Test
    @DisplayName("여러 할 일을 등록하고 목록 조회가 정상 동작한다")
    void multipleTodosFlow_deletedTodo_returnsOnlyActiveTodos() throws Exception {
        // given
        String accessToken = authenticate("list-user", "password123", "목록테스터");

        Long todo1Id = createTodo(accessToken, "할 일 1", "첫 번째");
        Long todo2Id = createTodo(accessToken, "할 일 2", "두 번째");
        Long todo3Id = createTodo(accessToken, "할 일 3", "세 번째");

        // when - 하나는 삭제 처리
        mockMvc.perform(delete("/api/todos/{id}", todo2Id)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        // when - 전체 목록 조회
        mockMvc.perform(get("/api/todos")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("할 일 1"))
                .andExpect(jsonPath("$.data[1].title").value("할 일 3"));

        // then - DB에서도 검증
        List<Todo> todosFromDb = todoRepository.findAllByDeletedFalseOrderByIdAsc();
        assertThat(todosFromDb).hasSize(2);
        assertThat(todosFromDb)
                .extracting(Todo::getTitle)
                .containsExactly("할 일 1", "할 일 3");
        assertThat(todoRepository.findById(todo1Id)).isPresent();
        assertThat(todoRepository.findById(todo2Id).orElseThrow().isDeleted()).isTrue();
        assertThat(todoRepository.findById(todo3Id)).isPresent();
    }

    @Test
    @DisplayName("할 일 수정 플로우가 정상 동작한다")
    void todoUpdateFlow_existingTodo_updatesSuccessfully() throws Exception {
        // given
        String accessToken = authenticate("update-user", "password123", "수정테스터");
        Long todoId = createTodo(accessToken, "원래 제목", "원래 내용");

        // when - 할 일 수정
        mockMvc.perform(put("/api/todos/{id}", todoId)
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", bearer(accessToken))
                        .content("""
                                {
                                  "title": "수정된 제목",
                                  "content": "수정된 내용"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(todoId))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                .andExpect(jsonPath("$.data.completed").value(false));

        // when - 단건 조회로 재검증
        mockMvc.perform(get("/api/todos/{id}", todoId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"));

        // then - DB에서도 검증
        Todo todoFromDb = todoRepository.findById(todoId).orElseThrow();
        assertThat(todoFromDb.getTitle()).isEqualTo("수정된 제목");
        assertThat(todoFromDb.getContent()).isEqualTo("수정된 내용");
    }

    private String authenticate(String loginId, String password, String nickname) throws Exception {
        doNothing().when(redisTokenService).saveRefreshToken(anyLong(), anyString());
        doNothing().when(redisTokenService).deleteRefreshToken(anyLong());
        doNothing().when(redisTokenService).addToBlacklist(anyString(), anyLong());
        when(redisTokenService.isBlacklisted(anyString())).thenReturn(false);

        Member member = Member.create(loginId, passwordEncoder.encode(password), nickname);
        memberRepository.save(member);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(loginId, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        return readText(loginResult, "/data/accessToken");
    }

    private Long createTodo(String accessToken, String title, String content) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", bearer(accessToken))
                        .content(objectMapper.writeValueAsString(new TodoPayload(title, content))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        return readLong(createResult, "/data/id");
    }

    private Long readLong(MvcResult result, String pointer) throws Exception {
        return responseBody(result).at(pointer).asLong();
    }

    private String readText(MvcResult result, String pointer) throws Exception {
        return responseBody(result).at(pointer).asText();
    }

    private JsonNode responseBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record LoginPayload(String loginId, String password) {
    }

    private record TodoPayload(String title, String content) {
    }
}
