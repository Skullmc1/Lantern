<script lang="ts">
    import { onMount, onDestroy } from "svelte";
    import StatusHeader from "./components/StatusHeader.svelte";
    import OrbControl from "./components/OrbControl.svelte";
    import RequestStack from "./components/RequestStack.svelte";
    import ConnectionFooter from "./components/ConnectionFooter.svelte";
    import QrModal from "./components/QrModal.svelte";
    import SharedItemsList from "./components/SharedItemsList.svelte";

    // --- Logic Layer ---
    interface ServerState {
        running: boolean;
        ip: string;
        port: number;
        permission: boolean;
    }

    interface ClientSession {
        id: string;
        deviceName: string;
        ipAddress: string;
        status: string;
        timestamp: number;
    }
    
    interface SharedItem {
        token: string;
        name: string;
        isDirectory: boolean;
        createdAt: number;
    }

    let state: ServerState = {
        running: false,
        ip: "...",
        port: 8080,
        permission: false,
    };
    
    let pendingSessions: ClientSession[] = [];
    let sharedItems: SharedItem[] = [];
    let interval: ReturnType<typeof setInterval>;
    
    // UI State
    let showQr = false;
    let qrValue = "";
    let qrTitle = "Connect via QR";

    function syncState() {
        if (typeof window !== "undefined" && window.Android) {
            try {
                const status = JSON.parse(window.Android.getServerStatus());
                state = {
                    running: status.isRunning,
                    ip: status.ip,
                    port: status.port,
                    permission: window.Android.checkPermission(),
                };
                
                // Fetch pending sessions
                const sessionsJson = window.Android.getPendingSessions();
                pendingSessions = JSON.parse(sessionsJson);
                
                // Fetch shared items
                const sharesJson = window.Android.getShares();
                sharedItems = JSON.parse(sharesJson);
                
            } catch (e) {
                console.error("Bridge Error", e);
            }
        }
    }

    function handleInteraction() {
        if (!window.Android) return;

        if (!state.permission) {
            window.Android.requestPermission();
        } else {
            window.Android.toggleServer();
        }
        setTimeout(syncState, 400);
    }
    
    function approve(id: string) {
        if(window.Android) window.Android.approveSession(id);
        syncState();
    }
    
    function reject(id: string) {
        if(window.Android) window.Android.rejectSession(id);
        syncState();
    }
    
    function openQr() {
        if (!state.ip || state.ip === "...") return;
        qrTitle = "Connect via QR";
        qrValue = `http://${state.ip}:${state.port}`;
        showQr = true;
    }
    
    function openShareQr(token: string) {
        if (!state.ip || state.ip === "...") return;
        qrTitle = "Magic Link QR";
        qrValue = `http://${state.ip}:${state.port}/s/${token}`;
        showQr = true;
    }
    
    function revokeShare(token: string) {
        if(window.Android) window.Android.revokeShare(token);
        syncState();
    }
    
    function pickFile() {
        if(window.Android) window.Android.pickFile();
    }
    
    function pickFolder() {
        if(window.Android) window.Android.pickFolder();
    }

    onMount(() => {
        syncState();
        interval = setInterval(syncState, 1000);
    });

    onDestroy(() => clearInterval(interval));
</script>

<div class="bio-zen-shell" class:active={state.running}>
    <div class="ambient-layer">
        <div class="spore s1"></div>
        <div class="spore s2"></div>
        <div class="spore s3"></div>
    </div>

    <StatusHeader running={state.running} />

    <main>
        <OrbControl 
            running={state.running}
            permission={state.permission}
            onInteract={handleInteraction}
        />
        
        <SharedItemsList 
            shares={sharedItems}
            onRevoke={revokeShare}
            onPickFile={pickFile}
            onPickFolder={pickFolder}
            onShowQr={openShareQr}
        />
        
        <RequestStack 
            sessions={pendingSessions} 
            onApprove={approve} 
            onReject={reject} 
        />
    </main>

    <ConnectionFooter 
        running={state.running}
        ip={state.ip}
        port={state.port}
        onShowQr={openQr}
    />
    
    {#if showQr}
        <QrModal 
            value={qrValue} 
            title={qrTitle}
            onClose={() => showQr = false} 
        />
    {/if}
</div>

<style>
    :global(body) {
        margin: 0;
        font-family:
            "Inter",
            -apple-system,
            sans-serif;
        background: #000;
    }

    .bio-zen-shell {
        height: 100vh;
        width: 100%;
        position: relative;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        background: radial-gradient(circle at top, #112822 0%, #020617 100%);
        color: #ecfdf5;
        transition: background 1s ease;
    }

    .bio-zen-shell.active {
        background: radial-gradient(circle at top, #064e3b 0%, #020617 80%);
    }

    .ambient-layer {
        position: absolute;
        inset: 0;
        pointer-events: none;
        overflow: hidden;
    }
    .spore {
        position: absolute;
        border-radius: 50%;
        filter: blur(40px);
        opacity: 0.4;
        animation: float 10s infinite ease-in-out alternate;
    }
    .s1 {
        width: 200px;
        height: 200px;
        background: #10b981;
        top: -50px;
        left: -50px;
    }
    .s2 {
        width: 300px;
        height: 300px;
        background: #059669;
        bottom: 10%;
        right: -100px;
        animation-duration: 15s;
    }
    .s3 {
        width: 150px;
        height: 150px;
        background: #34d399;
        top: 40%;
        left: 30%;
        opacity: 0.1;
        animation-duration: 20s;
    }

    main {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        z-index: 10;
        position: relative;
    }

    @keyframes float {
        0% {
            transform: translate(0, 0);
        }
        100% {
            transform: translate(20px, -20px);
        }
    }
</style>
