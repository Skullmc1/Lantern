<script lang="ts">
    import { onMount, onDestroy } from "svelte";
    import GrassPlains from "./components/GrassPlains.svelte";
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
    <GrassPlains />

    <StatusHeader running={state.running} />

    <main>
        <div class="center-content">
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
        </div>
        
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
        background: var(--color-bg);
    }

    .bio-zen-shell {
        height: 100vh;
        width: 100%;
        position: relative;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        background: transparent;
        color: #ecfdf5;
        transition: background 1s ease;
        padding-top: env(safe-area-inset-top, 0px);
        padding-bottom: env(safe-area-inset-bottom, 0px);
    }

    .bio-zen-shell.active {
        background: transparent;
    }

    main {
        flex: 1;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        z-index: 10;
        position: relative;
        scrollbar-width: thin;
        scrollbar-color: rgba(255, 184, 77, 0.3) transparent;
    }

    .center-content {
        margin: auto 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 100%;
        padding: 1rem 0 4rem;
    }
</style>