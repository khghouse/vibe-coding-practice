package com.practice.cursor.domain.todo.controller;

import com.practice.cursor.domain.todo.dto.request.TodoCreateRequest;
import com.practice.cursor.domain.todo.dto.request.TodoCreateServiceRequest;
import com.practice.cursor.domain.todo.dto.request.TodoUpdateRequest;
import com.practice.cursor.domain.todo.dto.request.TodoUpdateServiceRequest;
import com.practice.cursor.domain.todo.dto.response.TodoResponse;
import com.practice.cursor.domain.todo.service.TodoService;
import com.practice.cursor.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TodoController REST Docs 문서화 테스트.
 * RestDocsSupport를 상속하여 API 문서를 생성한다.
 */
class TodoControllerDocsTest extends RestDocsSupport {

    private final TodoService todoService = mock(TodoService.class);

    @Override
    protected Object initController() {
        return new TodoController(todoService);
    }

    @Test
    @DisplayName("할 일 생성 API 문서화")
    void createTodo_validRequest_generatesDocument() throws Exception {
        // given
        TodoCreateRequest request = new TodoCreateRequest("할 일 제목", "할 일 내용");
        TodoResponse response = new TodoResponse(
                1L,
                "할 일 제목",
                "할 일 내용",
                false,
                false,
                LocalDateTime.of(2026, 4, 17, 12, 0, 0),
                LocalDateTime.of(2026, 4, 17, 12, 0, 0)
        );

        given(todoService.register(any(TodoCreateServiceRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 제목 (2-50자)"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(key("required").value("false"))
                                        .description("할 일 내용 (최대 500자)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(key("required").value("false"))
                                        .description("할 일 내용"),
                                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("삭제 여부"),
                                fieldWithPath("data.completed").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("완료 여부"),
                                fieldWithPath("data.createDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("생성 일시 (ISO-8601)"),
                                fieldWithPath("data.modifiedDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("수정 일시 (ISO-8601)")
                        )
                ));
    }

    @Test
    @DisplayName("할 일 단건 조회 API 문서화")
    void getTodo_existingTodo_generatesDocument() throws Exception {
        // given
        TodoResponse response = new TodoResponse(
                1L,
                "할 일 제목",
                "할 일 내용",
                false,
                false,
                LocalDateTime.of(2026, 4, 17, 12, 0, 0),
                LocalDateTime.of(2026, 4, 17, 12, 0, 0)
        );

        given(todoService.getById(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/todos/{id}", 1L))
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id")
                                        .attributes(key("type").value("Number"))
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(key("required").value("false"))
                                        .description("할 일 내용"),
                                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("삭제 여부"),
                                fieldWithPath("data.completed").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("완료 여부"),
                                fieldWithPath("data.createDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("생성 일시 (ISO-8601)"),
                                fieldWithPath("data.modifiedDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("수정 일시 (ISO-8601)")
                        )
                ));
    }

    @Test
    @DisplayName("할 일 목록 조회 API 문서화")
    void getTodos_existingTodos_generatesDocument() throws Exception {
        // given
        List<TodoResponse> responses = List.of(
                new TodoResponse(
                        1L,
                        "할 일 1",
                        "첫 번째 할 일",
                        false,
                        false,
                        LocalDateTime.of(2026, 4, 17, 12, 0, 0),
                        LocalDateTime.of(2026, 4, 17, 12, 0, 0)
                ),
                new TodoResponse(
                        2L,
                        "할 일 2",
                        "두 번째 할 일",
                        false,
                        true,
                        LocalDateTime.of(2026, 4, 17, 13, 0, 0),
                        LocalDateTime.of(2026, 4, 17, 14, 0, 0)
                )
        );

        given(todoService.findAll()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andDo(document.document(
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data[]").type(JsonFieldType.ARRAY)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 목록"),
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID"),
                                fieldWithPath("data[].title").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 제목"),
                                fieldWithPath("data[].content").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(key("required").value("false"))
                                        .description("할 일 내용"),
                                fieldWithPath("data[].deleted").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("삭제 여부"),
                                fieldWithPath("data[].completed").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("완료 여부"),
                                fieldWithPath("data[].createDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("생성 일시 (ISO-8601)"),
                                fieldWithPath("data[].modifiedDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("수정 일시 (ISO-8601)")
                        )
                ));
    }

    @Test
    @DisplayName("할 일 수정 API 문서화")
    void updateTodo_validRequest_generatesDocument() throws Exception {
        // given
        TodoUpdateRequest request = new TodoUpdateRequest("수정된 제목", "수정된 내용");
        TodoResponse response = new TodoResponse(
                1L,
                "수정된 제목",
                "수정된 내용",
                false,
                false,
                LocalDateTime.of(2026, 4, 17, 12, 0, 0),
                LocalDateTime.of(2026, 4, 17, 15, 0, 0)
        );

        given(todoService.update(eq(1L), any(TodoUpdateServiceRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/todos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id")
                                        .attributes(key("type").value("Number"))
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 제목 (2-50자)"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(key("required").value("false"))
                                        .description("할 일 내용 (최대 500자)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(key("required").value("false"))
                                        .description("할 일 내용"),
                                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("삭제 여부"),
                                fieldWithPath("data.completed").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("완료 여부"),
                                fieldWithPath("data.createDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("생성 일시 (ISO-8601)"),
                                fieldWithPath("data.modifiedDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("수정 일시 (ISO-8601)")
                        )
                ));
    }

    @Test
    @DisplayName("할 일 완료 처리 API 문서화")
    void completeTodo_existingTodo_generatesDocument() throws Exception {
        // given
        TodoResponse response = new TodoResponse(
                1L,
                "할 일 제목",
                "할 일 내용",
                false,
                true,
                LocalDateTime.of(2026, 4, 17, 12, 0, 0),
                LocalDateTime.of(2026, 4, 17, 15, 0, 0)
        );

        given(todoService.complete(1L)).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/todos/{id}/complete", 1L))
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id")
                                        .attributes(key("type").value("Number"))
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(key("required").value("false"))
                                        .description("할 일 내용"),
                                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("삭제 여부"),
                                fieldWithPath("data.completed").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("완료 여부"),
                                fieldWithPath("data.createDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("생성 일시 (ISO-8601)"),
                                fieldWithPath("data.modifiedDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("수정 일시 (ISO-8601)")
                        )
                ));
    }

    @Test
    @DisplayName("할 일 삭제 API 문서화")
    void deleteTodo_existingTodo_generatesDocument() throws Exception {
        // given
        TodoResponse response = new TodoResponse(
                1L,
                "할 일 제목",
                "할 일 내용",
                true,
                false,
                LocalDateTime.of(2026, 4, 17, 12, 0, 0),
                LocalDateTime.of(2026, 4, 17, 15, 0, 0)
        );

        given(todoService.delete(1L)).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/todos/{id}", 1L))
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id")
                                        .attributes(key("type").value("Number"))
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("할 일 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(key("required").value("false"))
                                        .description("할 일 내용"),
                                fieldWithPath("data.deleted").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("삭제 여부"),
                                fieldWithPath("data.completed").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("완료 여부"),
                                fieldWithPath("data.createDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("생성 일시 (ISO-8601)"),
                                fieldWithPath("data.modifiedDateTime").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("수정 일시 (ISO-8601)")
                        )
                ));
    }
}
