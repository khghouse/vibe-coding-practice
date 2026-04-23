package com.practice.cursor.domain.todo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.practice.cursor.domain.todo.entity.Todo;
import com.practice.cursor.support.RepositoryTestSupport;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TodoRepository JPA 슬라이스 테스트.
 * RepositoryTestSupport를 상속하여 JPA Repository 기능을 검증한다.
 * 
 * @DataJpaTest가 적용되어 있으므로 @Transactional은 별도 선언하지 않는다.
 */
class TodoRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TodoRepository todoRepository;

    @Test
    @DisplayName("할 일을 저장할 수 있다")
    void save_validTodo_returnsSavedTodo() {
        // given
        Todo todo = Todo.create("할 일 제목", "할 일 내용");

        // when
        Todo savedTodo = todoRepository.save(todo);

        // then
        assertThat(savedTodo.getId()).isNotNull();
        assertThat(savedTodo.getTitle()).isEqualTo("할 일 제목");
        assertThat(savedTodo.getContent()).isEqualTo("할 일 내용");
        assertThat(savedTodo.isCompleted()).isFalse();
        assertThat(savedTodo.isDeleted()).isFalse();
        assertThat(savedTodo.getCreateDateTime()).isNotNull();
        assertThat(savedTodo.getModifiedDateTime()).isNotNull();
    }

    @Test
    @DisplayName("ID로 할 일을 조회할 수 있다")
    void findById_existingTodo_returnsTodo() {
        // given
        Todo savedTodo = todoRepository.save(Todo.create("할 일 제목", "할 일 내용"));

        // when
        Optional<Todo> foundTodo = todoRepository.findById(savedTodo.getId());

        // then
        assertThat(foundTodo).isPresent();
        assertThat(foundTodo.get().getTitle()).isEqualTo("할 일 제목");
        assertThat(foundTodo.get().getContent()).isEqualTo("할 일 내용");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
    void findById_missingTodo_returnsEmptyOptional() {
        // when
        Optional<Todo> foundTodo = todoRepository.findById(999L);

        // then
        assertThat(foundTodo).isEmpty();
    }

    @Test
    @DisplayName("모든 할 일을 조회할 수 있다")
    void findAll_existingTodos_returnsTodos() {
        // given
        todoRepository.save(Todo.create("할 일 1", "내용 1"));
        todoRepository.save(Todo.create("할 일 2", "내용 2"));
        todoRepository.save(Todo.create("할 일 3", "내용 3"));

        // when
        List<Todo> todos = todoRepository.findAll();

        // then
        assertThat(todos).hasSize(3);
        assertThat(todos)
                .extracting(Todo::getTitle)
                .containsExactly("할 일 1", "할 일 2", "할 일 3");
    }

    @Test
    @DisplayName("삭제되지 않은 할 일만 조회할 수 있다")
    void findAllByDeletedFalseOrderByIdAsc_deletedTodo_returnsActiveTodos() {
        // given
        Todo todo1 = todoRepository.save(Todo.create("할 일 1", "내용 1"));
        Todo todo2 = todoRepository.save(Todo.create("할 일 2", "내용 2"));
        Todo todo3 = todoRepository.save(Todo.create("할 일 3", "내용 3"));
        
        // 하나를 삭제 처리
        todo2.delete();
        todoRepository.save(todo2);

        // when
        List<Todo> todos = todoRepository.findAllByDeletedFalseOrderByIdAsc();

        // then
        assertThat(todos).hasSize(2);
        assertThat(todos)
                .extracting(Todo::getTitle)
                .containsExactly("할 일 1", "할 일 3");
    }

    @Test
    @DisplayName("할 일을 수정할 수 있다")
    void save_updatedTodo_persistsChanges() throws InterruptedException {
        // given
        Todo savedTodo = todoRepository.save(Todo.create("원래 제목", "원래 내용"));
        
        // 시간 차이를 만들기 위해 잠시 대기
        Thread.sleep(10);

        // when
        savedTodo.updateTitleAndContent("수정된 제목", "수정된 내용");
        Todo updatedTodo = todoRepository.save(savedTodo);

        // then
        assertThat(updatedTodo.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedTodo.getContent()).isEqualTo("수정된 내용");
        assertThat(updatedTodo.getModifiedDateTime()).isAfterOrEqualTo(updatedTodo.getCreateDateTime());
    }

    @Test
    @DisplayName("할 일을 완료 처리할 수 있다")
    void save_completedTodo_persistsCompletedState() throws InterruptedException {
        // given
        Todo savedTodo = todoRepository.save(Todo.create("할 일 제목", "할 일 내용"));
        
        // 시간 차이를 만들기 위해 잠시 대기
        Thread.sleep(10);

        // when
        savedTodo.complete();
        Todo completedTodo = todoRepository.save(savedTodo);

        // then
        assertThat(completedTodo.isCompleted()).isTrue();
        assertThat(completedTodo.getModifiedDateTime()).isAfterOrEqualTo(completedTodo.getCreateDateTime());
    }

    @Test
    @DisplayName("할 일을 삭제 처리할 수 있다")
    void save_deletedTodo_persistsDeletedState() throws InterruptedException {
        // given
        Todo savedTodo = todoRepository.save(Todo.create("할 일 제목", "할 일 내용"));
        
        // 시간 차이를 만들기 위해 잠시 대기
        Thread.sleep(10);

        // when
        savedTodo.delete();
        Todo deletedTodo = todoRepository.save(savedTodo);

        // then
        assertThat(deletedTodo.isDeleted()).isTrue();
        assertThat(deletedTodo.getModifiedDateTime()).isAfterOrEqualTo(deletedTodo.getCreateDateTime());
    }

    @Test
    @DisplayName("JPA Auditing이 정상 동작한다")
    void jpaAuditing_savedTodo_populatesAuditFields() {
        // given
        Todo todo = Todo.create("할 일 제목", "할 일 내용");

        // when
        Todo savedTodo = todoRepository.save(todo);

        // then
        assertThat(savedTodo.getCreateDateTime()).isNotNull();
        assertThat(savedTodo.getModifiedDateTime()).isNotNull();
        assertThat(savedTodo.getCreateDateTime()).isEqualTo(savedTodo.getModifiedDateTime());
    }
}
