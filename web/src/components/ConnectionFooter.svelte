<script lang="ts">
    import { Wifi, Check, Copy, QrCode } from "lucide-svelte";

    export let running: boolean;
    export let ip: string;
    export let port: number;
    export let onShowQr: () => void;

    let copied = false;

    async function copyToClipboard() {
        if (!ip || ip === "...") return;
        try {
            await navigator.clipboard.writeText(`http://${ip}:${port}`);
            copied = true;
            setTimeout(() => (copied = false), 2000);
        } catch (err) {
            console.error("Failed to copy", err);
        }
    }
</script>

<footer>
    <button
        class="connection-card {running ? 'visible' : ''} {copied
            ? 'copied'
            : ''}"
        on:click={copyToClipboard}
        disabled={!running}
    >
        <div class="conn-icon">
            {#if copied}
                <Check size={16} />
            {:else}
                <Wifi size={16} />
            {/if}
        </div>
        <div class="conn-info">
            <span class="label">
                {copied ? "Copied to Clipboard" : "Local Address"}
            </span>
            <span class="value">{ip}:{port}</span>
        </div>
    </button>
    
    <button 
        class="qr-btn {running ? 'visible' : ''}" 
        on:click={onShowQr}
        disabled={!running}
        title="Show QR Code"
    >
        <QrCode size={20} />
    </button>
</footer>

<style>
    footer {
        padding: 2rem;
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 0.75rem;
        z-index: 10;
    }

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
        color: inherit;
        font-family: inherit;
        flex: 1;
        max-width: 300px;
    }

    .qr-btn {
        width: 50px;
        height: 50px; /* match connection-card approx height if needed, or square */
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(6, 78, 59, 0.4);
        border: 1px solid rgba(52, 211, 153, 0.2);
        border-radius: 16px;
        color: #34d399;
        cursor: pointer;
        opacity: 0;
        transform: translateY(20px);
        transition: all 0.3s ease;
    }
    
    .qr-btn:hover {
        background: rgba(52, 211, 153, 0.1);
        transform: translateY(0) scale(1.05);
    }
    
    .qr-btn.visible, .connection-card.visible {
        opacity: 1;
        transform: translateY(0);
    }

    .connection-card:hover {
        background: rgba(6, 78, 59, 0.6);
        transform: translateY(0) scale(1.02);
    }
    .connection-card:active {
        transform: translateY(0) scale(0.98);
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
</style>
