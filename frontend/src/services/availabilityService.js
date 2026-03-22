const API_BASE_URL = "http://localhost:8080";

export async function getAvailability() {
  const response = await fetch(`${API_BASE_URL}/availability`);

  if (!response.ok) {
    throw new Error("Failed to load availability");
  }

  return response.json();
}

export async function saveAvailability(blocks) {
  const response = await fetch(`${API_BASE_URL}/availability`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(blocks),
  });

  if (!response.ok) {
    throw new Error("Failed to save availability");
  }

  return response.json();
}