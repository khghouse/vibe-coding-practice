package com.practice.cursor.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.cursor.domain.todo.dto.request.TodoCreateServiceRequest;
import com.practice.cursor.domain.todo.dto.response.TodoResponse;
import com.practice.cursor.domain.todo.entity.Todo;
import com.practice.cursor.domain.todo.repository.TodoRepository;
import com.practice.cursor.domain.todo.service.TodoService;
import com.practice.cursor.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Todo 도메인 E2E 통합 테스트.
 * Controller와 Repository를 전부 관통하는 흐름을 검증한다.
 * IntegrationTestSupport를 상속하여 실제 Spring Context와 DB를 사용한다.
 * 
 * @Transactional을 클래스 레벨에 선언하여 데이터 격리를 보장한다.
 */
@Transactional
class TodoIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Test
    @DisplayName("할 일 등록부터 완료까지 전체 플로우가 정상 동작한다")
    void todoCompleteFlow_registeredTodo_completesSuccessfully() {
        // given - 할 일 등록
        TodoCreateServiceRequest createRequest = TodoCreateServiceRequest.of("통합 테스트", "E2E 플로우 검증");
        
        // when - 할 일 등록
        TodoResponse createdTodo = todoService.register(createRequest);
        
        // then - 등록 검증
        assertThat(createdTodo.id()).isNotNull();
        assertThat(createdTodo.title()).isEqualTo("통합 테스트");
        assertThat(createdTodo.content()).isEqualTo("E2E 플로우 검증");
        assertThat(createdTodo.completed()).isFalse();
        assertThat(createdTodo.deleted()).isFalse();

        // when - 할 일 완료 처리
        TodoResponse completedTodo = todoService.complete(createdTodo.id());
        
        // then - 완료 처리 검증
        assertThat(completedTodo.id()).isEqualTo(createdTodo.id());
        assertThat(completedTodo.completed()).isTrue();
        assertThat(completedTodo.deleted()).isFalse();

        // when - DB에서 직접 조회
        Todo todoFromDb = todoRepository.findById(createdTodo.id()).orElseThrow();
        
        // then - DB 상태 검증
        assertThat(todoFromDb.isCompleted()).isTrue();
        assertThat(todoFromDb.isDeleted()).isFalse();
        assertThat(todoFromDb.getTitle()).isEqualTo("통합 테스트");
    }

    @Test
    @DisplayName("여러 할 일을 등록하고 목록 조회가 정상 동작한다")
    void multipleTodosFlow_deletedTodo_returnsOnlyActiveTodos() {
        // given - 여러 할 일 등록
        TodoCreateServiceRequest request1 = TodoCreateServiceRequest.of("할 일 1", "첫 번째");
        TodoCreateServiceRequest request2 = TodoCreateServiceRequest.of("할 일 2", "두 번째");
        TodoCreateServiceRequest request3 = TodoCreateServiceRequest.of("할 일 3", "세 번째");
        
        // when - 할 일들 등록
        TodoResponse todo1 = todoService.register(request1);
        TodoResponse todo2 = todoService.register(request2);
        TodoResponse todo3 = todoService.register(request3);
        
        // 하나는 삭제 처리
        todoService.delete(todo2.id());
        
        // when - 전체 목록 조회 (삭제되지 않은 것만)
        List<TodoResponse> allTodos = todoService.findAll();
        
        // then - 삭제되지 않은 할 일만 조회되는지 검증
        assertThat(allTodos).hasSize(2);
        assertThat(allTodos)
                .extracting(TodoResponse::title)
                .containsExactly("할 일 1", "할 일 3");
        
        // DB에서도 검증
        List<Todo> todosFromDb = todoRepository.findAllByDeletedFalseOrderByIdAsc();
        assertThat(todosFromDb).hasSize(2);
        assertThat(todosFromDb)
                .extracting(Todo::getTitle)
                .containsExactly("할 일 1", "할 일 3");
    }

    @Test
    @DisplayName("할 일 수정 플로우가 정상 동작한다")
    void todoUpdateFlow_existingTodo_updatesSuccessfully() {
        // given - 할 일 등록
        TodoCreateServiceRequest createRequest = TodoCreateServiceRequest.of("원래 제목", "원래 내용");
        TodoResponse createdTodo = todoService.register(createRequest);
        
        // when - 할 일 수정
        TodoResponse updatedTodo = todoService.update(
                createdTodo.id(), 
                com.practice.cursor.domain.todo.dto.request.TodoUpdateServiceRequest.of("수정된 제목", "수정된 내용")
        );
        
        // then - 수정 결과 검증
        assertThat(updatedTodo.id()).isEqualTo(createdTodo.id());
        assertThat(updatedTodo.title()).isEqualTo("수정된 제목");
        assertThat(updatedTodo.content()).isEqualTo("수정된 내용");
        assertThat(updatedTodo.completed()).isFalse();
        assertThat(updatedTodo.deleted()).isFalse();
        
        // when - 단건 조회로 재검증
        TodoResponse retrievedTodo = todoService.getById(createdTodo.id());
        
        // then - 조회 결과도 수정된 내용인지 검증
        assertThat(retrievedTodo.title()).isEqualTo("수정된 제목");
        assertThat(retrievedTodo.content()).isEqualTo("수정된 내용");
        
        // DB에서도 검증
        Todo todoFromDb = todoRepository.findById(createdTodo.id()).orElseThrow();
        assertThat(todoFromDb.getTitle()).isEqualTo("수정된 제목");
        assertThat(todoFromDb.getContent()).isEqualTo("수정된 내용");
    }
}
