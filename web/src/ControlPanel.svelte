<script lang="ts">
    import { onMount, onDestroy } from "svelte";
    import {
        Cast,
        AlertTriangle,
        PlayCircle,
        StopCircle,
        Copy,
    } from "lucide-svelte";
    // ... Logic remains the same ...
    let isServerRunning = false;
    let ipAddress = "...";
    let port = 8080;
    let hasPermission = false;
    let interval: number;

    function updateStatus() {
        if (typeof window !== "undefined" && window.Android) {
            try {
                const status = JSON.parse(window.Android.getServerStatus());
                isServerRunning = status.isRunning;
                ipAddress = status.ip;
                port = status.port;
                hasPermission = window.Android.checkPermission();
            } catch (e) {}
        }
    }
    function toggleServer() {
        if (window.Android) {
            !hasPermission
                ? window.Android.requestPermission()
                : window.Android.toggleServer();
            setTimeout(updateStatus, 500);
        }
    }
    onMount(() => {
        updateStatus();
        interval = setInterval(updateStatus, 2000);
    });
    onDestroy(() => clearInterval(interval));
</script>

<div class="glass-container">
    <div class="top-bar">
        <div class="brand">
            <Cast size={20} color="#10b981" />
            <span>LANTERN OS</span>
        </div>
        <div class="led {isServerRunning ? 'on' : ''}"></div>
    </div>

    <div class="content">
        <div class="card status-card">
            <span class="label">Status</span>
            <h2 class={isServerRunning ? "active" : "inactive"}>
                {isServerRunning ? "BROADCASTING" : "STANDBY"}
            </h2>
        </div>

        {#if isServerRunning}
            <div class="card url-card">
                <span class="label">Access Point</span>
                <div class="url-row">
                    <span class="protocol">http://</span>
                    <span class="ip">{ipAddress}</span>
                    <span class="port">:{port}</span>
                </div>
                <button class="copy-btn"><Copy size={16} /></button>
            </div>
        {/if}

        {#if !hasPermission}
            <div class="card warning-card">
                <AlertTriangle size={20} />
                <span>Storage Access Required</span>
            </div>
        {/if}
    </div>

    <div class="controls">
        <button
            class="slider-btn {isServerRunning ? 'stop' : 'start'}"
            on:click={toggleServer}
        >
            <span class="btn-text">
                {#if !hasPermission}
                    Authorize Access
                {:else}
                    {isServerRunning
                        ? "TERMINATE SESSION"
                        : "INITIALIZE SERVER"}
                {/if}
            </span>
            {#if isServerRunning}
                <StopCircle size={24} />
            {:else}
                <PlayCircle size={24} />
            {/if}
        </button>
    </div>
</div>

<style>
    .glass-container {
        height: 100vh;
        background: #0f172a;
        background-image:
            linear-gradient(rgba(16, 185, 129, 0.05) 1px, transparent 1px),
            linear-gradient(
                90deg,
                rgba(16, 185, 129, 0.05) 1px,
                transparent 1px
            );
        background-size: 30px 30px;
        display: flex;
        flex-direction: column;
        padding: 1.5rem;
        box-sizing: border-box;
        font-family: "JetBrains Mono", "Courier New", monospace;
        color: #e2e8f0;
    }

    .top-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 2rem;
    }

    .brand {
        display: flex;
        gap: 10px;
        font-weight: 700;
        color: #10b981;
        letter-spacing: 2px;
    }

    .led {
        width: 8px;
        height: 8px;
        background: #334155;
        border-radius: 50%;
        box-shadow: 0 0 0 2px #1e293b;
    }
    .led.on {
        background: #10b981;
        box-shadow: 0 0 10px #10b981;
    }

    .content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 1rem;
        justify-content: center;
    }

    .card {
        background: rgba(30, 41, 59, 0.7);
        backdrop-filter: blur(10px);
        border: 1px solid #334155;
        padding: 1.5rem;
        border-radius: 8px;
        position: relative;
        overflow: hidden;
    }

    .card::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        width: 4px;
        height: 100%;
        background: #334155;
    }

    .status-card::before {
        background: #10b981;
    }

    .label {
        font-size: 0.75rem;
        color: #94a3b8;
        text-transform: uppercase;
        display: block;
        margin-bottom: 0.5rem;
    }

    h2 {
        margin: 0;
        font-size: 1.8rem;
        letter-spacing: -1px;
    }
    h2.active {
        color: #10b981;
        text-shadow: 0 0 15px rgba(16, 185, 129, 0.3);
    }
    h2.inactive {
        color: #64748b;
    }

    .url-card {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .url-row {
        font-size: 1.25rem;
    }
    .protocol {
        color: #64748b;
    }
    .port {
        color: #10b981;
    }

    .copy-btn {
        background: none;
        border: none;
        color: #94a3b8;
        cursor: pointer;
    }

    .warning-card {
        border-color: #7f1d1d;
        background: rgba(127, 29, 29, 0.1);
        color: #fca5a5;
        display: flex;
        align-items: center;
        gap: 12px;
    }
    .warning-card::before {
        background: #ef4444;
    }

    .controls {
        margin-top: auto;
    }

    .slider-btn {
        width: 100%;
        padding: 1.25rem;
        border: none;
        border-radius: 4px;
        font-family: inherit;
        font-weight: bold;
        display: flex;
        justify-content: space-between;
        align-items: center;
        cursor: pointer;
        transition: all 0.2s;
        text-transform: uppercase;
    }

    .slider-btn.start {
        background: #10b981;
        color: #022c22;
    }
    .slider-btn.stop {
        background: #ef4444;
        color: white;
    }

    .slider-btn:active {
        transform: translateY(2px);
    }
</style>
