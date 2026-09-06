<script lang="ts">
    import { Ban, RefreshCw } from "lucide-svelte";
    import { fade } from "svelte/transition";
    import GrassPlains from "./components/GrassPlains.svelte";

    function retry() {
        // Clear session and reload to trigger new auth flow
        localStorage.removeItem("lantern_auth");
        window.location.reload();
    }
</script>

<div class="rejected-screen" in:fade>
    <GrassPlains />
    <div class="card">
        <div class="icon-pulse">
            <Ban size={48} />
        </div>
        <h2>Access Denied</h2>
        <p>Your connection request was declined by the host.</p>
        
        <button class="retry-btn" on:click={retry}>
            <RefreshCw size={18} />
            <span>Try Again</span>
        </button>
    </div>
</div>

<style>
    .rejected-screen {
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
        border: 1px solid rgba(248, 113, 113, 0.25);
        text-align: center;
        max-width: 400px;
        box-shadow: 0 20px 50px rgba(0,0,0,0.35);
    }

    .icon-pulse {
        background: rgba(248, 113, 113, 0.12);
        width: 100px;
        height: 100px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 1.5rem auto;
        color: #f87171;
        animation: pulse 2s infinite;
    }

    h2 {
        font-size: 2rem;
        margin: 0 0 1rem 0;
        color: #fca5a5;
    }

    p {
        color: #94a3b8;
        line-height: 1.5;
        margin-bottom: 2rem;
    }

    .retry-btn {
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        color: #ecfdf5;
        padding: 0.75rem 1.5rem;
        border-radius: 99px;
        font-size: 1rem;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        transition: all 0.2s;
        width: 100%;
    }

    .retry-btn:hover {
        background: rgba(255, 255, 255, 0.1);
        transform: translateY(-2px);
    }

    @keyframes pulse {
        0% { box-shadow: 0 0 0 0 rgba(248, 113, 113, 0.4); }
        70% { box-shadow: 0 0 0 20px rgba(248, 113, 113, 0); }
        100% { box-shadow: 0 0 0 0 rgba(248, 113, 113, 0); }
    }
</style>
