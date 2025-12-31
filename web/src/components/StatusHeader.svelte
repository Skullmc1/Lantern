<script lang="ts">
    import { Sparkles } from "lucide-svelte";

    export let running: boolean;
</script>

<header>
    <div class="brand-pill">
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
    :global(.spin-slow) {
        animation: spin 4s linear infinite;
    }
    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }
</style>
