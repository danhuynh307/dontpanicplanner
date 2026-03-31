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
    private final PriorityScoreService priorityScoreService;

    public TaskController(PriorityScoreService priorityScoreService) {
        this.priorityScoreService = priorityScoreService;
    }

    // ── CREATE ────────────────────────────────────────────────
    // POST /api/tasks
    // Accepts a Task object in the request body, calculates priority, and adds it.
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        priorityScoreService.applyPriorityScore(task);
        taskStore.add(task);
        return ResponseEntity.ok(task);
    }

    // ── READ (all) ────────────────────────────────────────────
    // GET /api/tasks
    // Returns all tasks currently in the store, with fresh priority scores.
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> result = new ArrayList<>();

        for (int i = 0; i < taskStore.size(); i++) {
            Task task = taskStore.get(i);
            priorityScoreService.applyPriorityScore(task);
            result.add(task);
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

        Task task = taskStore.get(index);
        priorityScoreService.applyPriorityScore(task);
        return ResponseEntity.ok(task);
    }

    // ── UPDATE ────────────────────────────────────────────────
    // PUT /api/tasks/{index}
    // Replaces the task at the given index with the new task from the request body.
    @PutMapping("/{index}")
    public ResponseEntity<Task> updateTask(@PathVariable int index, @RequestBody Task updatedTask) {
        if (index < 0 || index >= taskStore.size()) {
            return ResponseEntity.notFound().build();
        }

        priorityScoreService.applyPriorityScore(updatedTask);
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
