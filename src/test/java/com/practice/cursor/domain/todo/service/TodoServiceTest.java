package com.practice.cursor.domain.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.practice.cursor.global.exception.CustomException;
import com.practice.cursor.global.exception.ErrorCode;
import com.practice.cursor.domain.todo.dto.request.TodoCreateServiceRequest;
import com.practice.cursor.domain.todo.dto.request.TodoUpdateServiceRequest;
import com.practice.cursor.domain.todo.dto.response.TodoResponse;
import com.practice.cursor.domain.todo.entity.Todo;
import com.practice.cursor.domain.todo.repository.TodoRepository;
import com.practice.cursor.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * TodoService 통합 테스트.
 * IntegrationTestSupport를 상속하여 실제 DB와 연동한 테스트를 작성한다.
 * 
 * @Transactional을 클래스 레벨에 선언하여 데이터 격리를 보장한다.
 */
@Transactional
class TodoServiceTest extends IntegrationTestSupport {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Test
    @DisplayName("할 일을 등록할 수 있다")
    void register_validRequest_returnsTodoResponse() {
        // given
        TodoCreateServiceRequest request = TodoCreateServiceRequest.of("할 일 제목", "할 일 내용");

        // when
        TodoResponse response = todoService.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("할 일 제목");
        assertThat(response.content()).isEqualTo("할 일 내용");
        assertThat(response.completed()).isFalse();
        assertThat(response.deleted()).isFalse();
        assertThat(response.createDateTime()).isNotNull();
        assertThat(response.modifiedDateTime()).isNotNull();

        // DB 검증
        List<Todo> todos = todoRepository.findAll();
        assertThat(todos).hasSize(1);
        assertThat(todos.get(0).getTitle()).isEqualTo("할 일 제목");
    }

    @Test
    @DisplayName("할 일을 수정할 수 있다")
    void update_existingTodo_updatesTodo() {
        // given
        Todo savedTodo = todoRepository.save(Todo.create("원래 제목", "원래 내용"));
        TodoUpdateServiceRequest request = TodoUpdateServiceRequest.of("수정된 제목", "수정된 내용");

        // when
        TodoResponse response = todoService.update(savedTodo.getId(), request);

        // then
        assertThat(response.id()).isEqualTo(savedTodo.getId());
        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(response.content()).isEqualTo("수정된 내용");
        assertThat(response.completed()).isFalse();
        assertThat(response.deleted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 할 일을 수정하려 하면 예외가 발생한다")
    void update_missingTodo_throwsCustomException() {
        // given
        TodoUpdateServiceRequest request = TodoUpdateServiceRequest.of("수정된 제목", "수정된 내용");

        // when & then
        assertThatThrownBy(() -> todoService.update(999L, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("할 일을 단건 조회할 수 있다")
    void getById_existingTodo_returnsTodoResponse() {
        // given
        Todo savedTodo = todoRepository.save(Todo.create("할 일 제목", "할 일 내용"));

        // when
        TodoResponse response = todoService.getById(savedTodo.getId());

        // then
        assertThat(response.id()).isEqualTo(savedTodo.getId());
        assertThat(response.title()).isEqualTo("할 일 제목");
        assertThat(response.content()).isEqualTo("할 일 내용");
        assertThat(response.completed()).isFalse();
        assertThat(response.deleted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 할 일을 조회하려 하면 예외가 발생한다")
    void getById_missingTodo_throwsCustomException() {
        // when & then
        assertThatThrownBy(() -> todoService.getById(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("모든 할 일을 조회할 수 있다")
    void findAll_existingTodos_returnsTodoResponses() {
        // given
        todoRepository.save(Todo.create("할 일 1", "내용 1"));
        todoRepository.save(Todo.create("할 일 2", "내용 2"));
        todoRepository.save(Todo.create("할 일 3", "내용 3"));

        // when
        List<TodoResponse> responses = todoService.findAll();

        // then
        assertThat(responses).hasSize(3);
        assertThat(responses)
                .extracting(TodoResponse::title)
                .containsExactly("할 일 1", "할 일 2", "할 일 3");
    }

    @Test
    @DisplayName("할 일을 완료 처리할 수 있다")
    void complete_existingTodo_marksCompleted() {
        // given
        Todo savedTodo = todoRepository.save(Todo.create("할 일 제목", "할 일 내용"));

        // when
        TodoResponse response = todoService.complete(savedTodo.getId());

        // then
        assertThat(response.id()).isEqualTo(savedTodo.getId());
        assertThat(response.completed()).isTrue();
        assertThat(response.deleted()).isFalse();

        // DB 검증
        Todo updatedTodo = todoRepository.findById(savedTodo.getId()).orElseThrow();
        assertThat(updatedTodo.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 할 일을 완료 처리하려 하면 예외가 발생한다")
    void complete_missingTodo_throwsCustomException() {
        // when & then
        assertThatThrownBy(() -> todoService.complete(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("할 일을 삭제할 수 있다")
    void delete_existingTodo_marksDeleted() {
        // given
        Todo savedTodo = todoRepository.save(Todo.create("할 일 제목", "할 일 내용"));

        // when
        TodoResponse response = todoService.delete(savedTodo.getId());

        // then
        assertThat(response.id()).isEqualTo(savedTodo.getId());
        assertThat(response.completed()).isFalse();
        assertThat(response.deleted()).isTrue();

        // DB 검증
        Todo deletedTodo = todoRepository.findById(savedTodo.getId()).orElseThrow();
        assertThat(deletedTodo.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 할 일을 삭제하려 하면 예외가 발생한다")
    void delete_missingTodo_throwsCustomException() {
        // when & then
        assertThatThrownBy(() -> todoService.delete(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_NOT_FOUND.getMessage());
    }
}
