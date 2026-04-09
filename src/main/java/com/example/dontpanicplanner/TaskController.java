package com.example.dontpanicplanner;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // Accepts a Task object in the request body, calculates priority, and adds it.
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        priorityScoreService.applyPriorityScore(task);
        taskStore.add(task);
        return ResponseEntity.ok(task);
    }

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

    // Removes the task at the given index from the store.
    @DeleteMapping("/{index}")
    public ResponseEntity<Void> deleteTask(@PathVariable int index) {
        if (index < 0 || index >= taskStore.size()) {
            return ResponseEntity.notFound().build();
        }

        taskStore.remove(index);
        return ResponseEntity.noContent().build();
    }

    // Returns all current tasks as a downloadable CSV file.
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCSV() throws IOException {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskStore.size(); i++) {
            tasks.add(taskStore.get(i));
        }

        byte[] csvBytes = TaskCSVHandler.exportToBytes(tasks);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "tasks.csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    // Accepts a CSV file upload and adds all parsed tasks to the store.
    @PostMapping("/import")
    public ResponseEntity<List<Task>> importCSV(@RequestParam("file") MultipartFile file) throws IOException {
        List<Task> imported = TaskCSVHandler.importFromBytes(file.getBytes());

        for (Task task : imported) {
            priorityScoreService.applyPriorityScore(task);
            taskStore.add(task);
        }

        return ResponseEntity.ok(imported);
    }
}
