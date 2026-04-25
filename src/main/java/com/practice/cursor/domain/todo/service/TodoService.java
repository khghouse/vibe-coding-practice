package com.practice.cursor.domain.todo.service;

import com.practice.cursor.global.exception.CustomException;
import com.practice.cursor.global.exception.ErrorCode;
import com.practice.cursor.domain.todo.dto.request.TodoCreateServiceRequest;
import com.practice.cursor.domain.todo.dto.response.TodoResponse;
import com.practice.cursor.domain.todo.dto.request.TodoUpdateServiceRequest;
import com.practice.cursor.domain.todo.entity.Todo;
import com.practice.cursor.domain.todo.repository.TodoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    @Transactional
    public TodoResponse register(TodoCreateServiceRequest request) {
        Todo todo = Todo.create(request.title(), request.content());
        Todo saved = todoRepository.save(todo);
        return TodoResponse.from(saved);
    }

    @Transactional
    public TodoResponse update(Long id, TodoUpdateServiceRequest request) {
        Todo todo = getActiveTodo(id);
        todo.updateTitleAndContent(request.title(), request.content());
        return TodoResponse.from(todo);
    }

    public TodoResponse getById(Long id) {
        Todo todo = getActiveTodo(id);
        return TodoResponse.from(todo);
    }

    public List<TodoResponse> findAll() {
        return todoRepository.findAllByDeletedFalseOrderByIdAsc().stream()
                .map(TodoResponse::from)
                .toList();
    }

    @Transactional
    public TodoResponse complete(Long id) {
        Todo todo = getActiveTodo(id);
        todo.complete();
        return TodoResponse.from(todo);
    }

    @Transactional
    public void delete(Long id) {
        Todo todo = getTodoOrThrow(id);
        todo.delete();
    }

    private Todo getActiveTodo(Long id) {
        Todo todo = getTodoOrThrow(id);
        if (todo.isDeleted()) {
            throw new CustomException(ErrorCode.TODO_NOT_FOUND);
        }
        return todo;
    }

    private Todo getTodoOrThrow(Long id) {
        return todoRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.TODO_NOT_FOUND));
    }
}
