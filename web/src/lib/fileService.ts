import type { DirectoryListing, FileItem } from "./types";
import { getSessionId } from "./authService";

const API_BASE = "";

function getHeaders() {
    const headers: Record<string, string> = {};
    const sessionId = getSessionId();
    if (sessionId) headers["X-Session-ID"] = sessionId;
    return headers;
}

export async function fetchFiles(path: string): Promise<DirectoryListing> {
  const res = await fetch(
    `${API_BASE}/api/list?path=${encodeURIComponent(path)}`,
    { headers: getHeaders() }
  );
  if (!res.ok) throw new Error("Failed to load files");
  return res.json();
}

export async function fetchSharedFiles(token: string, path: string): Promise<DirectoryListing> {
  const res = await fetch(
    `${API_BASE}/api/share/list?token=${token}&path=${encodeURIComponent(path)}`
  );
  if (!res.ok) throw new Error("Failed to load shared files");
  return res.json();
}

export async function deleteFileItem(path: string): Promise<void> {
  const res = await fetch(
    `${API_BASE}/api/delete?path=${encodeURIComponent(path)}`,
    {
      method: "DELETE",
      headers: getHeaders()
    },
  );
  if (!res.ok) throw new Error("Failed to delete");
}

export async function renameFileItem(
  path: string,
  newName: string,
): Promise<void> {
  const headers = getHeaders();
  headers["Content-Type"] = "application/json";
  
  const res = await fetch(`${API_BASE}/api/rename`, {
    method: "POST",
    headers: headers,
    body: JSON.stringify({ path, newName }),
  });
  if (!res.ok) throw new Error("Failed to rename");
}

export async function uploadFile(path: string, file: File): Promise<void> {
  const formData = new FormData();
  formData.append("file", file);
  
  const headers = getHeaders();
  // Note: Do NOT set Content-Type for FormData, browser does it with boundary
  // But we need to add our custom header. 
  
  const res = await fetch(
    `${API_BASE}/api/upload?path=${encodeURIComponent(path)}`,
    {
      method: "POST",
      headers: { "X-Session-ID": headers["X-Session-ID"] || "" },
      body: formData,
    },
  );
  if (!res.ok) throw new Error("Failed to upload");
}

export function getDownloadUrl(path: string, shareToken?: string): string {
  if (shareToken) {
      return `${API_BASE}/api/share/list?token=${shareToken}&path=${encodeURIComponent(path)}`;
  }
  const sessionId = getSessionId();
  return `${API_BASE}/api/download?path=${encodeURIComponent(path)}&sessionId=${sessionId}`;
}
