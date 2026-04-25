package com.practice.cursor.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.cursor.global.exception.CustomException;
import com.practice.cursor.global.exception.ErrorCode;
import com.practice.cursor.global.exception.GlobalExceptionHandler;
import com.practice.cursor.global.security.MemberPrincipal;
import com.practice.cursor.domain.member.entity.Role;
import com.practice.cursor.domain.todo.dto.request.TodoCreateRequest;
import com.practice.cursor.domain.todo.dto.request.TodoCreateServiceRequest;
import com.practice.cursor.domain.todo.dto.response.TodoResponse;
import com.practice.cursor.domain.todo.dto.request.TodoUpdateRequest;
import com.practice.cursor.domain.todo.dto.request.TodoUpdateServiceRequest;
import com.practice.cursor.domain.todo.service.TodoService;
import com.practice.cursor.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@WebMvcTest(TodoController.class)
class TodoControllerTest extends ControllerTestSupport {

    @MockBean
    private TodoService todoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static TodoResponse sample(Long id) {
        LocalDateTime now = LocalDateTime.of(2026, 4, 2, 12, 0, 0);
        return new TodoResponse(id, "할 일", "상세", false, now, now);
    }

    private Authentication authenticatedUser() {
        MemberPrincipal memberPrincipal = MemberPrincipal.authenticated(1L, Role.USER);
        return new UsernamePasswordAuthenticationToken(
                memberPrincipal,
                null,
                memberPrincipal.getAuthorities());
    }

    @Test
    @DisplayName("POST 등록 요청이 성공하면 201과 ApiResponse로 본문을 반환한다")
    void register_validRequest_returnsOkWithApiResponse() throws Exception {
        when(todoService.register(any(TodoCreateServiceRequest.class))).thenReturn(sample(1L));

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(authentication(authenticatedUser()))
                        .content(objectMapper.writeValueAsString(new TodoCreateRequest("할 일", "상세"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("할 일"))
                .andExpect(jsonPath("$.data.content").value("상세"));

        verify(todoService).register(any(TodoCreateServiceRequest.class));
    }

    @Test
    @DisplayName("제목이 규칙에 맞지 않으면 400이며 서비스는 호출되지 않는다")
    void register_invalidTitle_returnsBadRequestWithoutCallingService() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(authentication(authenticatedUser()))
                        .content(objectMapper.writeValueAsString(new TodoCreateRequest("a", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").isString());

        verify(todoService, never()).register(any());
    }

    @Test
    @DisplayName("PUT 수정 성공 시 200과 ApiResponse")
    void update_validRequest_returnsOkWithApiResponse() throws Exception {
        when(todoService.update(eq(1L), any(TodoUpdateServiceRequest.class))).thenReturn(sample(1L));

        mockMvc.perform(put("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(authentication(authenticatedUser()))
                        .content(objectMapper.writeValueAsString(new TodoUpdateRequest("할 일", "상세"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(todoService).update(eq(1L), any(TodoUpdateServiceRequest.class));
    }

    @Test
    @DisplayName("GET 단건 조회 성공")
    void getById_existingTodo_returnsOkWithApiResponse() throws Exception {
        when(todoService.getById(1L)).thenReturn(sample(1L));

        mockMvc.perform(get("/api/todos/1")
                        .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("할 일"));
    }

    @Test
    @DisplayName("GET 단건 없으면 404")
    void getById_missingTodo_returnsNotFound() throws Exception {
        when(todoService.getById(99L)).thenThrow(new CustomException(ErrorCode.TODO_NOT_FOUND));

        mockMvc.perform(get("/api/todos/99")
                        .with(authentication(authenticatedUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET 전체 조회")
    void findAll_existingTodos_returnsOkWithList() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 4, 2, 12, 0, 0);
        when(todoService.findAll())
                .thenReturn(
                        List.of(
                                new TodoResponse(1L, "a", null, false, now, now),
                                new TodoResponse(2L, "b", "x", true, now, now)));

        mockMvc.perform(get("/api/todos")
                        .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("a"))
                .andExpect(jsonPath("$.data[1].title").value("b"));
    }

    @Test
    @DisplayName("PATCH 완료 처리")
    void complete_existingTodo_returnsOkWithCompletedTodo() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 4, 2, 12, 0, 0);
        when(todoService.complete(1L)).thenReturn(new TodoResponse(1L, "할 일", null, true, now, now));

        mockMvc.perform(patch("/api/todos/1/complete")
                        .with(csrf())
                        .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    @DisplayName("DELETE 소프트 삭제")
    void delete_existingTodo_returnsOkWithoutData() throws Exception {
        mockMvc.perform(delete("/api/todos/1")
                        .with(csrf())
                        .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(todoService).delete(1L);
    }
}
