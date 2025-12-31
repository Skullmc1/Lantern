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
