package com.example.dontpanicplanner;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



@Service
public class TaskService {
    private long nextId = 1;
    private final TaskDataStructure<Task> taskStore = new TaskDataStructure<>();
    private final PriorityScoreService priorityScoreService;
    private final TaskRankSystem taskRankSystem;

    public TaskService(PriorityScoreService priorityScoreService, TaskRankSystem taskRankSystem) {
        this.priorityScoreService = priorityScoreService;
        this.taskRankSystem = taskRankSystem;
    }

    public void loadFromFile(String filePath) {
        try {
            List<Task> imported = TaskCSVHandler.importFromFile(filePath);
            taskStore.clear();
            for (Task task : imported) {
                if (task.getId() == null) {
                    task.setId(nextId++);
                }
                priorityScoreService.applyPriorityScore(task);
                taskStore.add(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getAllTasks() {
        TaskDataStructure<Task> rankedCopy = new TaskDataStructure<>();
        for (int i = 0; i < taskStore.size(); i++) {
            Task task = taskStore.get(i);
            priorityScoreService.applyPriorityScore(task);
            rankedCopy.add(task);
        }
        taskRankSystem.rankTasks(rankedCopy);

        List<Task> result = new ArrayList<>();
        for (int i = 0; i < rankedCopy.size(); i++) {
            result.add(rankedCopy.get(i));
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
        if (task.getId() == null) {
            task.setId(nextId++);
        }
        priorityScoreService.applyPriorityScore(task);
        taskStore.add(task);
        return task;
    }

    public Task updateTaskById(Long id, Task updatedTask) {
        for (int i = 0; i < taskStore.size(); i++) {
            Task existingTask = taskStore.get(i);
            if (Objects.equals(existingTask.getId(), id)) {
                if (updatedTask.getId() == null) {
                    updatedTask.setId(existingTask.getId());
                }
                priorityScoreService.applyPriorityScore(updatedTask);
                taskStore.set(i, updatedTask);
                return updatedTask;
            }
        }
        return null;
    }

    public boolean deleteTaskById(Long id) {
        for (int i = 0; i < taskStore.size(); i++) {
            Task task = taskStore.get(i);
            if (Objects.equals(task.getId(), id)) {
                taskStore.remove(i);
                return true;
            }
        }
        return false;
    }

    public void importTasks(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getId() == null) {
                task.setId(nextId++);
            }
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