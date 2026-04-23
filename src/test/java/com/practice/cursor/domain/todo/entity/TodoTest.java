package com.practice.cursor.domain.todo.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.practice.cursor.global.exception.CustomException;
import com.practice.cursor.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TodoTest {

    @Test
    @DisplayName("제목·내용이 규칙을 만족하면 Todo를 생성할 수 있다")
    void create_withValidTitleAndContent_succeeds() {
        Todo todo = Todo.create("장보기", "우유, 계란");

        assertThat(todo.getTitle()).isEqualTo("장보기");
        assertThat(todo.getContent()).isEqualTo("우유, 계란");
        assertThat(todo.isDeleted()).isFalse();
        assertThat(todo.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("내용이 없어도 제목만으로 생성할 수 있다")
    void create_withoutContent_succeeds() {
        Todo todo = Todo.create("운동하기", null);

        assertThat(todo.getContent()).isNull();
    }

    @Test
    @DisplayName("제목이 정확히 2자이면 생성할 수 있다")
    void create_titleMinLength_succeeds() {
        Todo todo = Todo.create("가나", null);

        assertThat(todo.getTitle()).isEqualTo("가나");
    }

    @Test
    @DisplayName("제목이 정확히 50자이면 생성할 수 있다")
    void create_titleMaxLength_succeeds() {
        String fifty = "a".repeat(50);

        Todo todo = Todo.create(fifty, null);

        assertThat(todo.getTitle()).hasSize(50);
    }

    @Test
    @DisplayName("내용이 정확히 500자이면 생성할 수 있다")
    void create_contentMaxLength_succeeds() {
        String fiveHundred = "x".repeat(500);

        Todo todo = Todo.create("제목입니다", fiveHundred);

        assertThat(todo.getContent()).hasSize(500);
    }

    @Test
    @DisplayName("제목 앞뒤 공백은 제거된 뒤 길이를 검증한다")
    void create_titleWithOuterSpaces_returnsTrimmedTitle() {
        Todo todo = Todo.create("  가나다  ", null);

        assertThat(todo.getTitle()).isEqualTo("가나다");
    }

    @Test
    @DisplayName("트림 후 제목이 1자이면 생성에 실패한다")
    void create_trimmedTitleTooShort_throwsCustomException() {
        assertThatThrownBy(() -> Todo.create(" a ", null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_TITLE_LENGTH_INVALID.formatMessage(2, 50))
                .hasMessageContaining("제목")
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("제목이 1자이면 생성에 실패한다")
    void create_titleTooShort_throwsCustomException() {
        assertThatThrownBy(() -> Todo.create("a", null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_TITLE_LENGTH_INVALID.formatMessage(2, 50))
                .hasMessageContaining("제목")
                .hasMessageContaining("50");
    }

    @Test
    @DisplayName("제목이 51자 이상이면 생성에 실패한다")
    void create_titleTooLong_throwsCustomException() {
        String fiftyOne = "b".repeat(51);

        assertThatThrownBy(() -> Todo.create(fiftyOne, null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_TITLE_LENGTH_INVALID.formatMessage(2, 50))
                .hasMessageContaining("제목");
    }

    @Test
    @DisplayName("내용이 501자 이상이면 생성에 실패한다")
    void create_contentTooLong_throwsCustomException() {
        String fiveHundredOne = "c".repeat(501);

        assertThatThrownBy(() -> Todo.create("유효한제목입니다", fiveHundredOne))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_CONTENT_LENGTH_EXCEEDED.formatMessage(500))
                .hasMessageContaining("내용")
                .hasMessageContaining("500");
    }

    @Test
    @DisplayName("제목이 null이면 생성에 실패한다")
    void create_nullTitle_throwsCustomException() {
        assertThatThrownBy(() -> Todo.create(null, "내용"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_TITLE_REQUIRED.getMessage())
                .hasMessageContaining("제목");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("제목이 비어 있거나 공백만 있으면 생성에 실패한다")
    void create_blankTitle_throwsCustomException(String title) {
        assertThatThrownBy(() -> Todo.create(title, "내용"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_TITLE_REQUIRED.getMessage())
                .hasMessageContaining("제목");
    }

    @Test
    @DisplayName("complete()는 완료 상태로 만든다")
    void complete_existingTodo_setsCompleted() {
        Todo todo = Todo.create("할일", null);

        todo.complete();

        assertThat(todo.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("삭제된 할 일은 complete()할 수 없다")
    void complete_deletedTodo_throwsCustomException() {
        Todo todo = Todo.create("할일", null);
        todo.delete();

        assertThatThrownBy(todo::complete)
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_DELETED.getMessage());
    }

    @Test
    @DisplayName("delete()는 소프트 삭제한다")
    void delete_existingTodo_setsDeleted() {
        Todo todo = Todo.create("할일", null);

        todo.delete();

        assertThat(todo.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("updateTitleAndContent는 제목·내용을 바꾼다")
    void updateTitleAndContent_validTitleAndContent_changesFields() {
        Todo todo = Todo.create("이전", "옛내용");

        todo.updateTitleAndContent("다음", "새내용");

        assertThat(todo.getTitle()).isEqualTo("다음");
        assertThat(todo.getContent()).isEqualTo("새내용");
    }

    @Test
    @DisplayName("삭제된 할 일은 수정할 수 없다")
    void updateTitleAndContent_deletedTodo_throwsCustomException() {
        Todo todo = Todo.create("할일", null);
        todo.delete();

        assertThatThrownBy(() -> todo.updateTitleAndContent("a", "b"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_DELETED.getMessage());
    }
}
