package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private final TaskDataStructure<Task> taskStore = new TaskDataStructure<>();
    private final PriorityScoreService priorityScoreService;

    public TaskService(PriorityScoreService priorityScoreService) {
        this.priorityScoreService = priorityScoreService;
    }

    public void loadFromFile(String filePath) {
        try {
            List<Task> imported = TaskCSVHandler.importFromFile(filePath);
            taskStore.clear();
            for (Task task : imported) {
                priorityScoreService.applyPriorityScore(task);
                taskStore.add(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getAllTasks() {
        List<Task> result = new ArrayList<>();
        for (int i = 0; i < taskStore.size(); i++) {
            Task task = taskStore.get(i);
            priorityScoreService.applyPriorityScore(task);
            result.add(task);
        }
        return result;
    }

    public Task getTask(int index) {
        if (index < 0 || index >= taskStore.size()) {
            return null;
        }
        Task task = taskStore.get(index);
        priorityScoreService.applyPriorityScore(task);
        return task;
    }

    public Task addTask(Task task) {
        priorityScoreService.applyPriorityScore(task);
        taskStore.add(task);
        return task;
    }

    public Task updateTask(int index, Task updatedTask) {
        if (index < 0 || index >= taskStore.size()) {
            return null;
        }
        priorityScoreService.applyPriorityScore(updatedTask);
        taskStore.set(index, updatedTask);
        return updatedTask;
    }

    public boolean deleteTask(int index) {
        if (index < 0 || index >= taskStore.size()) {
            return false;
        }
        taskStore.remove(index);
        return true;
    }

    public void importTasks(List<Task> tasks) {
        for (Task task : tasks) {
            priorityScoreService.applyPriorityScore(task);
            taskStore.add(task);
        }
    }

    public List<Task> exportTasks() {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskStore.size(); i++) {
            tasks.add(taskStore.get(i));
        }
        return tasks;
    }
}