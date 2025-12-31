<script lang="ts">
    import { onMount } from "svelte";
    import QRCode from "qrcode";
    import { X, Copy, Check } from "lucide-svelte";
    import { fade, scale } from "svelte/transition";

    export let value: string;
    export let title: string = "Scan Code";
    export let onClose: () => void;

    let canvas: HTMLCanvasElement;
    let copied = false;

    onMount(async () => {
        if (canvas && value) {
            try {
                await QRCode.toCanvas(canvas, value, {
                    width: 256,
                    margin: 2,
                    color: {
                        dark: "#064e3b",
                        light: "#ecfdf5",
                    },
                });
            } catch (err) {
                console.error(err);
            }
        }
    });

    async function copyLink() {
        try {
            await navigator.clipboard.writeText(value);
            copied = true;
            setTimeout(() => (copied = false), 2000);
        } catch (e) {
            console.error(e);
        }
    }
</script>

<div class="modal-backdrop" on:click={onClose} transition:fade>
    <div class="modal-content" on:click|stopPropagation transition:scale>
        <div class="modal-header">
            <h3>{title}</h3>
            <button class="close-btn" on:click={onClose}>
                <X size={20} />
            </button>
        </div>

        <div class="qr-wrapper">
            <canvas bind:this={canvas}></canvas>
        </div>

        <div class="link-actions">
            <div class="link-text">{value}</div>
            <button class="copy-btn" on:click={copyLink}>
                {#if copied}
                    <Check size={18} />
                {:else}
                    <Copy size={18} />
                {/if}
            </button>
        </div>
    </div>
</div>

<style>
    .modal-backdrop {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.8);
        backdrop-filter: blur(5px);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 100;
    }

    .modal-content {
        background: #020617;
        border: 1px solid #34d399;
        border-radius: 20px;
        padding: 1.5rem;
        width: 90%;
        max-width: 320px;
        box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;
    }

    h3 {
        margin: 0;
        color: #ecfdf5;
        font-size: 1.25rem;
    }

    .close-btn {
        background: none;
        border: none;
        color: #94a3b8;
        cursor: pointer;
        padding: 4px;
        border-radius: 50%;
        transition: all 0.2s;
        display: flex;
    }

    .close-btn:hover {
        background: rgba(255, 255, 255, 0.1);
        color: #fff;
    }

    .qr-wrapper {
        background: #ecfdf5;
        padding: 1rem;
        border-radius: 12px;
        display: flex;
        justify-content: center;
        margin-bottom: 1.5rem;
    }

    canvas {
        display: block;
        max-width: 100%;
        height: auto !important;
    }

    .link-actions {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        background: rgba(255, 255, 255, 0.05);
        padding: 0.5rem;
        border-radius: 8px;
        border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .link-text {
        flex: 1;
        font-family: monospace;
        font-size: 0.85rem;
        color: #94a3b8;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .copy-btn {
        background: none;
        border: none;
        color: #34d399;
        cursor: pointer;
        padding: 4px;
        border-radius: 4px;
    }

    .copy-btn:hover {
        background: rgba(52, 211, 153, 0.1);
    }
</style>
