<script lang="ts">
    import { onMount, onDestroy } from "svelte";
    import { Wifi, Power, Lock, Zap } from "lucide-svelte";

    // --- Logic Module ---
    let state = {
        running: false,
        ip: "...",
        port: 8080,
        permission: false,
    };
    let interval: number;

    function syncState() {
        if (window.Android) {
            try {
                const status = JSON.parse(window.Android.getServerStatus());
                state = {
                    running: status.isRunning,
                    ip: status.ip,
                    port: status.port,
                    permission: window.Android.checkPermission(),
                };
            } catch (e) {
                console.error(e);
            }
        }
    }

    function handleToggle() {
        if (!window.Android) return;
        if (!state.permission) window.Android.requestPermission();
        else window.Android.toggleServer();
        setTimeout(syncState, 300);
    }

    onMount(() => {
        syncState();
        interval = setInterval(syncState, 2000);
    });
    onDestroy(() => clearInterval(interval));
</script>

<div class="viewport">
    <div class="bg-layer"></div>
    <div class="rain-overlay"></div>

    <div class="glass-panel">
        <header class="animated-header">
            <div class="signal-bars">
                <div class="bar" style="animation-delay: 0.1s"></div>
                <div class="bar" style="animation-delay: 0.2s"></div>
                <div class="bar" style="animation-delay: 0.3s"></div>
            </div>
            <span class="app-name">LANTERN</span>
        </header>

        <div class="content">
            <div class="status-display">
                <h2 class:active={state.running}>
                    {state.running ? "ONLINE" : "OFFLINE"}
                </h2>
                <div class="meta-row">
                    {#if state.running}
                        <span class="tag ip">{state.ip}:{state.port}</span>
                    {/if}
                    {#if !state.permission}
                        <span class="tag warn"
                            ><Lock size={10} /> Perms Needed</span
                        >
                    {/if}
                </div>
            </div>

            <button
                class="glass-toggle"
                class:active={state.running}
                on:click={handleToggle}
            >
                <div class="icon-box">
                    <Power size={32} />
                </div>
                <span class="toggle-label"
                    >{state.running ? "Deactivate" : "Activate"}</span
                >
            </button>
        </div>
    </div>
</div>

<style>
    :global(body) {
        margin: 0;
        background: #000;
        font-family: "Inter", sans-serif;
        overflow: hidden;
    }

    .viewport {
        height: 100vh;
        width: 100vw;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
    }

    /* --- Optimized Background --- */
    .bg-layer {
        position: absolute;
        inset: -20px; /* Bleed for movement */
        background: url("https://images.unsplash.com/photo-1518531933037-91b2f5f229cc?q=80&w=1000&auto=format&fit=crop")
            center/cover;
        filter: brightness(0.4);
        animation: slowPan 60s ease-in-out infinite alternate;
        will-change: transform;
        z-index: 0;
    }

    .rain-overlay {
        position: absolute;
        inset: 0;
        background-image: url("https://upload.wikimedia.org/wikipedia/commons/6/66/Load.gif"); /* Using a noise texture works too */
        opacity: 0.05;
        mix-blend-mode: overlay;
        pointer-events: none;
        z-index: 1;
    }

    /* --- Glass Panel --- */
    .glass-panel {
        width: 90%;
        max-width: 360px;
        background: rgba(255, 255, 255, 0.05);
        backdrop-filter: blur(20px);
        -webkit-backdrop-filter: blur(20px);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 24px;
        padding: 2rem;
        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        z-index: 10;
        display: flex;
        flex-direction: column;
        gap: 2rem;
    }

    /* --- Header --- */
    .animated-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        padding-bottom: 1rem;
    }

    .app-name {
        color: #fff;
        letter-spacing: 4px;
        font-weight: 200;
        font-size: 0.9rem;
    }

    .signal-bars {
        display: flex;
        gap: 3px;
        align-items: flex-end;
        height: 16px;
    }
    .bar {
        width: 3px;
        background: #10b981;
        animation: equalize 1s infinite;
        height: 50%;
    }

    /* --- Content --- */
    .status-display {
        text-align: center;
        margin: 1rem 0;
    }

    h2 {
        font-size: 2.5rem;
        margin: 0;
        color: #52525b;
        transition: color 0.3s;
        letter-spacing: -1px;
    }
    h2.active {
        color: #fff;
        text-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
    }

    .meta-row {
        display: flex;
        justify-content: center;
        gap: 10px;
        margin-top: 10px;
        min-height: 24px;
    }

    .tag {
        background: rgba(0, 0, 0, 0.3);
        padding: 4px 8px;
        border-radius: 4px;
        color: #a1a1aa;
        font-size: 0.8rem;
        font-family: monospace;
    }
    .tag.ip {
        color: #34d399;
        border: 1px solid rgba(52, 211, 153, 0.2);
    }
    .tag.warn {
        color: #f87171;
        display: flex;
        align-items: center;
        gap: 4px;
    }

    /* --- Toggle Button --- */
    .glass-toggle {
        background: linear-gradient(
            145deg,
            rgba(255, 255, 255, 0.05),
            rgba(255, 255, 255, 0.01)
        );
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 16px;
        padding: 1.5rem;
        display: flex;
        align-items: center;
        justify-content: space-between;
        color: #fff;
        cursor: pointer;
        transition: all 0.3s;
    }

    .glass-toggle:active {
        transform: scale(0.98);
    }

    .glass-toggle.active {
        background: rgba(16, 185, 129, 0.2);
        border-color: rgba(52, 211, 153, 0.4);
        box-shadow: 0 0 30px rgba(16, 185, 129, 0.1);
    }

    .icon-box {
        background: rgba(255, 255, 255, 0.1);
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background 0.3s;
    }

    .glass-toggle.active .icon-box {
        background: #10b981;
        color: #022c22;
    }

    @keyframes slowPan {
        from {
            transform: scale(1.1) translate(0, 0);
        }
        to {
            transform: scale(1.1) translate(-20px, -10px);
        }
    }
    @keyframes equalize {
        0%,
        100% {
            height: 30%;
        }
        50% {
            height: 100%;
        }
    }
</style>
