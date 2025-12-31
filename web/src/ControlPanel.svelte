<script lang="ts">
    import { onMount, onDestroy } from "svelte";
    import {
        Power,
        Wifi,
        ShieldAlert,
        Sparkles,
        Copy,
        Check,
    } from "lucide-svelte";
    import { scale } from "svelte/transition";

    // --- Logic Layer ---
    interface ServerState {
        running: boolean;
        ip: string;
        port: number;
        permission: boolean;
    }

    let state: ServerState = {
        running: false,
        ip: "...",
        port: 8080,
        permission: false,
    };

    let interval: number;
    let copied = false;

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

    async function copyToClipboard() {
        if (!state.ip || state.ip === "...") return;
        try {
            await navigator.clipboard.writeText(
                `http://${state.ip}:${state.port}`,
            );
            copied = true;
            setTimeout(() => (copied = false), 2000);
        } catch (err) {
            console.error("Failed to copy", err);
        }
    }

    onMount(() => {
        syncState();
        interval = setInterval(syncState, 1500);
    });

    onDestroy(() => clearInterval(interval));
</script>

<div class="bio-zen-shell" class:active={state.running}>
    <div class="ambient-layer">
        <div class="spore s1"></div>
        <div class="spore s2"></div>
        <div class="spore s3"></div>
    </div>

    <header>
        <div class="brand-pill">
            <Sparkles size={14} class={state.running ? "spin-slow" : ""} />
            <span>LANTERN</span>
        </div>
    </header>

    <main>
        <div class="center-stage">
            <div class="orb-anchor">
                {#if state.running}
                    <div class="ripple-ring" transition:scale></div>
                    <div class="ripple-ring delay" transition:scale></div>
                {/if}

                <button
                    class="glass-orb"
                    class:active={state.running}
                    on:click={handleInteraction}
                >
                    <div class="inner-light"></div>
                    <div class="icon-layer">
                        {#if !state.permission}
                            <ShieldAlert size={36} class="warn-icon" />
                        {:else}
                            <Power size={36} class="power-icon" />
                        {/if}
                    </div>
                </button>
            </div>

            <div class="status-label">
                {#if !state.permission}
                    <span class="text-warn">Permission Required</span>
                {:else}
                    <span class="text-status"
                        >{state.running
                            ? "Broadcasting"
                            : "Ready to Ignite"}</span
                    >
                {/if}
            </div>
        </div>
    </main>

    <footer>
        <button
            class="connection-card {state.running ? 'visible' : ''} {copied
                ? 'copied'
                : ''}"
            on:click={copyToClipboard}
            disabled={!state.running}
        >
            <div class="conn-icon">
                {#if copied}
                    <Check size={16} />
                {:else}
                    <Wifi size={16} />
                {/if}
            </div>
            <div class="conn-info">
                <span class="label"
                    >{copied ? "Copied to Clipboard" : "Local Address"}</span
                >
                <span class="value">{state.ip}:{state.port}</span>
            </div>
            {#if !copied}
                <div class="copy-hint">
                    <Copy size={14} />
                </div>
            {/if}
        </button>
    </footer>
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

    header {
        padding: 2rem;
        display: flex;
        justify-content: center;
        z-index: 10;
    }
    .brand-pill {
        background: rgba(255, 255, 255, 0.05);
        backdrop-filter: blur(10px);
        padding: 8px 16px;
        border-radius: 50px;
        border: 1px solid rgba(255, 255, 255, 0.1);
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.8rem;
        letter-spacing: 2px;
        font-weight: 600;
        color: #6ee7b7;
    }

    main {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10;
    }

    .center-stage {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 2.5rem; /* Space between Orb and Text */
    }

    /* --- ALIGNMENT FIX --- */
    .orb-anchor {
        position: relative;
        width: 140px;
        height: 140px;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    /* --------------------- */

    .glass-orb {
        width: 140px;
        height: 140px;
        border-radius: 50%;
        border: 1px solid rgba(255, 255, 255, 0.1);
        background: linear-gradient(
            145deg,
            rgba(255, 255, 255, 0.1),
            rgba(255, 255, 255, 0.02)
        );
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        position: relative;
        cursor: pointer;
        box-shadow:
            inset 0 0 20px rgba(255, 255, 255, 0.05),
            0 10px 30px rgba(0, 0, 0, 0.3);
        transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 5;
    }

    .glass-orb.active {
        border-color: rgba(52, 211, 153, 0.5);
        box-shadow:
            inset 0 0 30px rgba(52, 211, 153, 0.2),
            0 0 50px rgba(16, 185, 129, 0.3);
        transform: scale(0.95);
    }

    .inner-light {
        position: absolute;
        inset: 20px;
        border-radius: 50%;
        background: radial-gradient(
            circle,
            rgba(52, 211, 153, 0.4) 0%,
            transparent 70%
        );
        opacity: 0.5;
        transition: opacity 0.5s;
    }
    .glass-orb.active .inner-light {
        opacity: 1;
        background: radial-gradient(
            circle,
            rgba(52, 211, 153, 0.8) 0%,
            transparent 70%
        );
    }

    .icon-layer {
        color: #6ee7b7;
        transition: color 0.3s;
        filter: drop-shadow(0 0 5px rgba(52, 211, 153, 0.5));
    }
    .glass-orb.active .icon-layer {
        color: #fff;
        filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.8));
    }
    .warn-icon {
        color: #f87171;
        filter: drop-shadow(0 0 5px rgba(248, 113, 113, 0.5));
    }

    /* Ripple Animations - Now centered relative to .orb-anchor */
    .ripple-ring {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 140px;
        height: 140px;
        border-radius: 50%;
        border: 2px solid rgba(52, 211, 153, 0.3);
        opacity: 0;
        pointer-events: none;
        animation: ripple 2s infinite linear;
        z-index: 1;
    }
    .delay {
        animation-delay: 1s;
    }

    .status-label {
        min-height: 24px;
        font-size: 0.9rem;
        letter-spacing: 1px;
        text-transform: uppercase;
        text-align: center;
    }
    .text-status {
        color: #a7f3d0;
        text-shadow: 0 0 10px rgba(16, 185, 129, 0.3);
    }
    .text-warn {
        color: #fca5a5;
        font-weight: bold;
    }

    footer {
        padding: 2rem;
        display: flex;
        justify-content: center;
        z-index: 10;
    }

    /* Interactive Footer Button */
    .connection-card {
        display: flex;
        align-items: center;
        gap: 16px;
        background: rgba(6, 78, 59, 0.4);
        border: 1px solid rgba(52, 211, 153, 0.2);
        padding: 12px 24px;
        border-radius: 16px;
        opacity: 0;
        transform: translateY(20px);
        transition: all 0.3s ease;
        cursor: pointer;
        text-align: left;
        /* Reset button styles */
        color: inherit;
        font-family: inherit;
    }

    .connection-card:hover {
        background: rgba(6, 78, 59, 0.6);
        transform: translateY(0) scale(1.02);
    }
    .connection-card:active {
        transform: translateY(0) scale(0.98);
    }
    .connection-card.visible {
        opacity: 1;
        transform: translateY(0);
    }

    .connection-card.copied {
        background: #064e3b;
        border-color: #34d399;
    }

    .conn-icon {
        width: 32px;
        height: 32px;
        background: #065f46;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #34d399;
        transition: all 0.2s;
    }
    .connection-card.copied .conn-icon {
        background: #34d399;
        color: #022c22;
    }

    .conn-info {
        display: flex;
        flex-direction: column;
        min-width: 120px;
    }
    .conn-info .label {
        font-size: 0.7rem;
        color: #6ee7b7;
        opacity: 0.8;
    }
    .conn-info .value {
        font-family: monospace;
        font-size: 1rem;
        color: #fff;
        font-weight: bold;
    }

    .copy-hint {
        opacity: 0.5;
        transition: opacity 0.2s;
    }
    .connection-card:hover .copy-hint {
        opacity: 1;
    }

    :global(.spin-slow) {
        animation: spin 4s linear infinite;
    }

    @keyframes float {
        0% {
            transform: translate(0, 0);
        }
        100% {
            transform: translate(20px, -20px);
        }
    }
    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }
    @keyframes ripple {
        0% {
            width: 140px;
            height: 140px;
            opacity: 0.5;
            border-width: 2px;
        }
        100% {
            width: 250px;
            height: 250px;
            opacity: 0;
            border-width: 0px;
        }
    }
</style>
