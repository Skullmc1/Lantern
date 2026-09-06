export interface SharedItem {
    token: string;
    name: string;
    isDirectory: boolean;
    createdAt: number;
}

export interface PendingSession {
    id: string;
    deviceName: string;
    ipAddress: string;
    status: string;
    timestamp: number;
}

export interface AndroidInterface {
    toggleServer: () => void;
    checkPermission: () => boolean;
    requestPermission: () => void;
    getServerStatus: () => string; // Returns JSON string: { isRunning: boolean, ip: string, port: number }
    getPendingSessions: () => string; // Returns JSON string of PendingSession[]
    approveSession: (id: string) => void;
    rejectSession: (id: string) => void;
    getShares: () => string; // Returns JSON string of SharedItem[]
    createShare: (path: string) => string;
    revokeShare: (token: string) => void;
    pickFile: () => void;
    pickFolder: () => void;
}

declare global {
    interface Window {
        Android?: AndroidInterface;
    }
}

// Mock implementation for Preview Mode
if (typeof window !== 'undefined') {
    const params = new URLSearchParams(window.location.search);
    if (params.get('mode') === 'android') {
        let isRunning = false;
        let pending: PendingSession[] = [];
        let shares: SharedItem[] = [];

        const seedDemo = () => {
            if (pending.length === 0) {
                pending = [
                    { id: "demo-1", deviceName: "Web Client 482", ipAddress: "192.168.1.55", status: "PENDING", timestamp: Date.now() - 4000 },
                    { id: "demo-2", deviceName: "Sara's Laptop", ipAddress: "192.168.1.42", status: "PENDING", timestamp: Date.now() - 9000 },
                ];
            }
            if (shares.length === 0) {
                shares = [
                    { token: "7f3k", name: "Photo.jpg", isDirectory: false, createdAt: Date.now() - 60000 },
                    { token: "q2m9", name: "Projects", isDirectory: true, createdAt: Date.now() - 180000 },
                ];
            }
        };

        window.Android = {
            toggleServer: () => {
                console.log("[MockAndroid] Toggle Server");
                isRunning = !isRunning;
                if (isRunning) seedDemo();
            },
            checkPermission: () => true,
            requestPermission: () => console.log("[MockAndroid] Request Permission"),
            getServerStatus: () => JSON.stringify({
                isRunning,
                ip: "192.168.1.100", // Mock IP
                port: 8080
            }),
            getPendingSessions: () => {
                seedDemo();
                return JSON.stringify(pending);
            },
            approveSession: (id) => {
                console.log("[MockAndroid] Approve", id);
                pending = pending.filter(s => s.id !== id);
            },
            rejectSession: (id) => {
                console.log("[MockAndroid] Reject", id);
                pending = pending.filter(s => s.id !== id);
            },
            getShares: () => {
                seedDemo();
                return JSON.stringify(shares);
            },
            createShare: (path) => {
                const item: SharedItem = {
                    token: Math.random().toString(36).slice(2, 6),
                    name: path.split("/").pop() || path,
                    isDirectory: false,
                    createdAt: Date.now()
                };
                shares = [item, ...shares];
                return JSON.stringify(item);
            },
            revokeShare: (token) => {
                console.log("[MockAndroid] Revoke", token);
                shares = shares.filter(s => s.token !== token);
            },
            pickFile: () => {
                console.log("[MockAndroid] Pick File");
                const item: SharedItem = {
                    token: Math.random().toString(36).slice(2, 6),
                    name: "Picked File.pdf",
                    isDirectory: false,
                    createdAt: Date.now()
                };
                shares = [item, ...shares];
            },
            pickFolder: () => {
                console.log("[MockAndroid] Pick Folder");
                const item: SharedItem = {
                    token: Math.random().toString(36).slice(2, 6),
                    name: "Picked Folder",
                    isDirectory: true,
                    createdAt: Date.now()
                };
                shares = [item, ...shares];
            },
        };
        console.log("Mock Android Interface Loaded");
    }
}
