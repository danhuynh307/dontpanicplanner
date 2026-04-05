const API_BASE_URL = "http://localhost:8080/api";

export async function fetchWeeklySchedule(weekStart) {
  const response = await fetch(`${API_BASE_URL}/schedule/weekly?weekStart=${weekStart}`);

  if (!response.ok) {
    throw new Error("Failed to fetch weekly schedule");
  }

  return response.json();
}