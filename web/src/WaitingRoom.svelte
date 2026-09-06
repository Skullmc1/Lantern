<script lang="ts">
    import { onMount, onDestroy } from "svelte";
    import { checkAuthStatus } from "./lib/authService";
    import { Loader2, Lock } from "lucide-svelte";
    import { fade } from "svelte/transition";
    import GrassPlains from "./components/GrassPlains.svelte";

    export let onApproved: () => void;

    let interval: ReturnType<typeof setInterval>;

    onMount(() => {
        interval = setInterval(async () => {
            const status = await checkAuthStatus();
            if (status === "APPROVED") {
                onApproved();
            } else if (status === "REJECTED" || status === "NONE") {
                 // Handle rejection? For now just stay here or show error
                 // Ideally reload page to reset flow
                 window.location.reload();
            }
        }, 2000);
    });

    onDestroy(() => {
        clearInterval(interval);
    });
</script>

<div class="waiting-room" in:fade>
    <GrassPlains />
    <div class="card">
        <div class="icon-pulse">
            <Lock size={48} />
        </div>
        <h2>Knock Knock!</h2>
        <p>Please approve this connection on your Android device.</p>
        
        <div class="status">
            <Loader2 size={20} class="spin" />
            <span>Waiting for approval...</span>
        </div>
    </div>
</div>

<style>
    .waiting-room {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100vh;
        background: var(--color-bg);
        color: #ecfdf5;
    }

    .card {
        position: relative;
        z-index: 10;
        background: var(--glass);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        padding: 3rem 2rem;
        border-radius: 24px;
        border: 1px solid var(--glass-border);
        text-align: center;
        max-width: 400px;
        box-shadow: 0 20px 50px rgba(0,0,0,0.35);
    }

    .icon-pulse {
        background: rgba(255, 184, 77, 0.12);
        width: 100px;
        height: 100px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 1.5rem auto;
        color: var(--lantern);
        animation: pulse 2s infinite;
    }

    h2 {
        font-size: 2rem;
        margin: 0 0 1rem 0;
        background: linear-gradient(to right, #ffe9b3, #34d399);
        background-clip: text;
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
    }

    p {
        color: #b7cbbf;
        line-height: 1.5;
        margin-bottom: 2rem;
    }

    .status {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        color: var(--lantern-soft);
        font-family: monospace;
        background: rgba(0,0,0,0.25);
        padding: 0.75rem;
        border-radius: 99px;
    }

    :global(.spin) {
        animation: spin 1s linear infinite;
    }

    @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
    
    @keyframes pulse {
        0% { box-shadow: 0 0 0 0 rgba(255, 184, 77, 0.4); }
        70% { box-shadow: 0 0 0 20px rgba(255, 184, 77, 0); }
        100% { box-shadow: 0 0 0 0 rgba(255, 184, 77, 0); }
    }
</style>
