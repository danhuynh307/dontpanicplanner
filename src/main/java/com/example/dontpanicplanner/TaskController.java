package com.example.dontpanicplanner;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.addTask(task));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{index}")
    public ResponseEntity<Task> getTask(@PathVariable int index) {
        Task task = taskService.getTask(index);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{index}")
    public ResponseEntity<Task> updateTask(@PathVariable int index, @RequestBody Task updatedTask) {
        Task task = taskService.updateTask(index, updatedTask);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTaskById(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCSV() throws IOException {
        byte[] csvBytes = TaskCSVHandler.exportToBytes(taskService.exportTasks());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "tasks.csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @PostMapping("/import")
    public ResponseEntity<List<Task>> importCSV(@RequestParam("file") MultipartFile file) throws IOException {
        List<Task> imported = TaskCSVHandler.importFromBytes(file.getBytes());
        taskService.importTasks(imported);
        return ResponseEntity.ok(imported);
    }
}