const USER_API_BASE_URL =
  import.meta.env.VITE_USER_API_BASE_URL || "http://localhost:8082/api";

async function sendUserRequest(path, payload) {
  let response;
  try {
    response = await fetch(`${USER_API_BASE_URL}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error("Could not reach the User Service. Check that it is running on port 8082.");
  }

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const message = Array.isArray(data.messages)
      ? data.messages.join(" ")
      : data.message || data.error;
    throw new Error(message || `Request failed with status ${response.status}.`);
  }
  return data;
}

export function registerUser(payload) {
  return sendUserRequest("/users", payload);
}

export function loginUser(payload) {
  return sendUserRequest("/users/login", payload);
}

export async function updateUserProfile(userId, payload) {
  let response;
  try {
    response = await fetch(`${USER_API_BASE_URL}/users/${userId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error("Could not reach the User Service. Check that it is running on port 8082.");
  }

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const message = Array.isArray(data.messages)
      ? data.messages.join(" ")
      : data.message || data.error;
    throw new Error(message || `Request failed with status ${response.status}.`);
  }
  return data;
}
