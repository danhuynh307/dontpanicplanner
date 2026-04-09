// frontend/src/services/taskService.js
const API_BASE = "http://localhost:8080/api/tasks";

// triggers a browser download of all tasks as tasks.csv
export async function exportTasksCSV() {
  const response = await fetch(`${API_BASE}/export`);
  if (!response.ok) throw new Error("Failed to export tasks");

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "tasks.csv";
  link.click();
  URL.revokeObjectURL(url);
}

// uploads a CSV file and returns the list of imported tasks
export async function importTasksCSV(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${API_BASE}/import`, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) throw new Error("Failed to import tasks");
  return response.json();
}

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