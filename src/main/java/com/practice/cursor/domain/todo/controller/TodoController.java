package com.practice.cursor.domain.todo.controller;

import com.practice.cursor.global.response.ApiResponse;
import com.practice.cursor.domain.todo.dto.request.TodoCreateRequest;
import com.practice.cursor.domain.todo.dto.request.TodoCreateServiceRequest;
import com.practice.cursor.domain.todo.dto.response.TodoResponse;
import com.practice.cursor.domain.todo.dto.request.TodoUpdateRequest;
import com.practice.cursor.domain.todo.dto.request.TodoUpdateServiceRequest;
import com.practice.cursor.domain.todo.service.TodoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ApiResponse<TodoResponse> register(@Valid @RequestBody TodoCreateRequest request) {
        return ApiResponse.ok(todoService.register(TodoCreateServiceRequest.from(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<TodoResponse> update(
            @PathVariable Long id, @Valid @RequestBody TodoUpdateRequest request) {
        return ApiResponse.ok(todoService.update(id, TodoUpdateServiceRequest.from(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<TodoResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(todoService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<TodoResponse>> findAll() {
        return ApiResponse.ok(todoService.findAll());
    }

    @PatchMapping("/{id}/complete")
    public ApiResponse<TodoResponse> complete(@PathVariable Long id) {
        return ApiResponse.ok(todoService.complete(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ApiResponse.ok();
    }
}
