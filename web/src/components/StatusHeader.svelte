<script lang="ts">
    import { Sparkles } from "lucide-svelte";

    export let running: boolean;
</script>

<header>
    <div class="brand-pill" class:lit={running}>
        <Sparkles size={14} class={running ? "spin-slow" : ""} />
        <span>LANTERN</span>
    </div>
</header>

<style>
    header {
        padding: 2rem;
        display: flex;
        justify-content: center;
        z-index: 10;
    }
    .brand-pill {
        background: var(--glass);
        backdrop-filter: blur(14px);
        -webkit-backdrop-filter: blur(14px);
        padding: 8px 16px;
        border-radius: 50px;
        border: 1px solid var(--glass-border);
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.8rem;
        letter-spacing: 2px;
        font-weight: 600;
        color: var(--lantern-soft);
        box-shadow: 0 0 24px rgba(255, 184, 77, 0.12);
    }
    .brand-pill.lit {
        border-color: rgba(255, 209, 102, 0.4);
        box-shadow: 0 0 30px rgba(255, 184, 77, 0.3);
    }
    :global(.spin-slow) {
        animation: spin 4s linear infinite;
    }
    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }
</style>
