package com.example.dontpanicplanner;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    // In-memory store using the project's custom data structure
    private final TaskDataStructure<Task> taskStore = new TaskDataStructure<>();

    // ── CREATE ────────────────────────────────────────────────
    // POST /api/tasks
    // Accepts a Task object in the request body and adds it to the store.
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        taskStore.add(task);
        return ResponseEntity.ok(task);
    }

    // ── READ (all) ────────────────────────────────────────────
    // GET /api/tasks
    // Returns all tasks currently in the store.
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> result = new ArrayList<>();
        for (int i = 0; i < taskStore.size(); i++) {
            result.add(taskStore.get(i));
        }
        return ResponseEntity.ok(result);
    }

    // ── READ (single) ─────────────────────────────────────────
    // GET /api/tasks/{index}
    // Returns a single task by its position index in the store.
    @GetMapping("/{index}")
    public ResponseEntity<Task> getTask(@PathVariable int index) {
        if (index < 0 || index >= taskStore.size()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(taskStore.get(index));
    }

    // ── UPDATE ────────────────────────────────────────────────
    // PUT /api/tasks/{index}
    // Replaces the task at the given index with the new task from the request body.
    @PutMapping("/{index}")
    public ResponseEntity<Task> updateTask(@PathVariable int index, @RequestBody Task updatedTask) {
        if (index < 0 || index >= taskStore.size()) {
            return ResponseEntity.notFound().build();
        }
        taskStore.set(index, updatedTask);
        return ResponseEntity.ok(updatedTask);
    }

    // ── DELETE ────────────────────────────────────────────────
    // DELETE /api/tasks/{index}
    // Removes the task at the given index from the store.
    @DeleteMapping("/{index}")
    public ResponseEntity<Void> deleteTask(@PathVariable int index) {
        if (index < 0 || index >= taskStore.size()) {
            return ResponseEntity.notFound().build();
        }
        taskStore.remove(index);
        return ResponseEntity.noContent().build();
    }
}