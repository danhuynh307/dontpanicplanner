// frontend/src/services/taskService.js
const API_BASE = "http://localhost:8080/api/tasks";

export async function getTasks() {
  const response = await fetch(API_BASE);
  if (!response.ok) {
    throw new Error("Failed to fetch tasks");
  }
  return response.json();
}

export async function createTask(task) {
  const response = await fetch(API_BASE, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(task),
  });

  if (!response.ok) {
    throw new Error("Failed to create task");
  }
  return response.json();
}

export async function updateTask(index, task) {
  const response = await fetch(`${API_BASE}/${index}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(task),
  });

  if (!response.ok) {
    throw new Error("Failed to update task");
  }
  return response.json();
}

export async function deleteTask(index) {
  const response = await fetch(`${API_BASE}/${index}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error("Failed to delete task");
  }
}