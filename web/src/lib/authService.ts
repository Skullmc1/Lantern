const AUTH_KEY = "lantern_auth";

interface AuthState {
  sessionId: string | null;
  status: "PENDING" | "APPROVED" | "REJECTED" | "NONE";
}

let currentState: AuthState = {
  sessionId: localStorage.getItem(AUTH_KEY),
  status: "NONE"
};

export function getSessionId() {
  return currentState.sessionId;
}

export function saveSessionId(id: string) {
  currentState.sessionId = id;
  localStorage.setItem(AUTH_KEY, id);
}

export async function requestAuth(deviceName: string): Promise<string> {
  const res = await fetch("/api/auth/request", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ deviceName })
  });
  if (!res.ok) throw new Error("Auth request failed");
  const data = await res.json();
  saveSessionId(data.sessionId);
  currentState.status = data.status;
  return data.status;
}

export async function checkAuthStatus(): Promise<string> {
  if (!currentState.sessionId) return "NONE";
  
  try {
    const res = await fetch("/api/auth/status", {
        headers: { "X-Session-ID": currentState.sessionId }
    });
    
    if (res.status === 401) {
        // Session invalid
        localStorage.removeItem(AUTH_KEY);
        currentState.sessionId = null;
        return "NONE";
    }
    
    const data = await res.json();
    currentState.status = data.status;
    return data.status;
  } catch (e) {
      console.error(e);
      return "ERROR";
  }
}
