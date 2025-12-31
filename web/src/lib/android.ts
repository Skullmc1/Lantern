export interface AndroidInterface {
    toggleServer: () => void;
    checkPermission: () => boolean;
    requestPermission: () => void;
    getServerStatus: () => string; // Returns JSON string: { isRunning: boolean, ip: string, port: number }
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
        
        window.Android = {
            toggleServer: () => {
                console.log("[MockAndroid] Toggle Server");
                isRunning = !isRunning;
            },
            checkPermission: () => true,
            requestPermission: () => console.log("[MockAndroid] Request Permission"),
            getServerStatus: () => JSON.stringify({
                isRunning,
                ip: "192.168.1.100", // Mock IP
                port: 8080
            })
        };
        console.log("Mock Android Interface Loaded");
    }
}
