import type { DirectoryListing, FileItem } from "./types";

const API_BASE = "";

export async function fetchFiles(path: string): Promise<DirectoryListing> {
  const res = await fetch(
    `${API_BASE}/api/list?path=${encodeURIComponent(path)}`,
  );
  if (!res.ok) throw new Error("Failed to load files");
  return res.json();
}

export async function deleteFileItem(path: string): Promise<void> {
  const res = await fetch(
    `${API_BASE}/api/delete?path=${encodeURIComponent(path)}`,
    {
      method: "DELETE",
    },
  );
  if (!res.ok) throw new Error("Failed to delete");
}

export async function renameFileItem(
  path: string,
  newName: string,
): Promise<void> {
  const res = await fetch(`${API_BASE}/api/rename`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ path, newName }),
  });
  if (!res.ok) throw new Error("Failed to rename");
}

export async function uploadFile(path: string, file: File): Promise<void> {
  const formData = new FormData();
  formData.append("file", file);
  const res = await fetch(
    `${API_BASE}/api/upload?path=${encodeURIComponent(path)}`,
    {
      method: "POST",
      body: formData,
    },
  );
  if (!res.ok) throw new Error("Failed to upload");
}

export function getDownloadUrl(path: string): string {
  return `${API_BASE}/api/download?path=${encodeURIComponent(path)}`;
}
