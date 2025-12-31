<script lang="ts">
    import { Power, ShieldAlert } from "lucide-svelte";
    import { scale } from "svelte/transition";

    export let running: boolean;
    export let permission: boolean;
    export let onInteract: () => void;
</script>

<div class="center-stage">
    <div class="orb-anchor">
        {#if running}
            <div class="ripple-ring" transition:scale></div>
            <div class="ripple-ring delay" transition:scale></div>
        {/if}

        <button
            class="glass-orb"
            class:active={running}
            on:click={onInteract}
        >
            <div class="inner-light"></div>
            <div class="icon-layer">
                {#if !permission}
                    <ShieldAlert size={36} class="warn-icon" />
                {:else}
                    <Power size={36} class="power-icon" />
                {/if}
            </div>
        </button>
    </div>

    <div class="status-label">
        {#if !permission}
            <span class="text-warn">Permission Required</span>
        {:else}
            <span class="text-status">
                {running ? "Broadcasting" : "Ready to Ignite"}
            </span>
        {/if}
    </div>
</div>

<style>
    .center-stage {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 2.5rem;
        transition: transform 0.3s ease;
    }

    .orb-anchor {
        position: relative;
        width: 140px;
        height: 140px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

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
    :global(.warn-icon) {
        color: #f87171;
        filter: drop-shadow(0 0 5px rgba(248, 113, 113, 0.5));
    }

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
