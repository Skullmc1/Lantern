<script lang="ts">
    import { Terminal, ChevronRight, Home } from "lucide-svelte";

    export let currentPath: string;
    export let onNavigate: (path: string) => void;

    function getParts(path: string) {
        if (!path) return [];
        return path.split("/").reduce(
            (acc, part, index) => {
                const pathSoFar =
                    index === 0 ? part : `${acc[index - 1].fullPath}/${part}`;
                acc.push({ name: part, fullPath: pathSoFar });
                return acc;
            },
            [] as { name: string; fullPath: string }[],
        );
    }
</script>

<div class="navigator">
    <button class="home-btn" on:click={() => onNavigate("")} title="Root">
        <Home size={16} />
    </button>
    
    <div class="divider"></div>

    <div class="path-scroll">
        {#each getParts(currentPath) as part, i}
             <div class="crumb-wrapper">
                <ChevronRight size={14} class="sep" />
                <button class="node" on:click={() => onNavigate(part.fullPath)}>
                    {part.name}
                </button>
             </div>
        {/each}
        {#if !currentPath}
            <span class="placeholder">/</span>
        {/if}
    </div>
</div>

<style>
    .navigator {
        background: var(--glass);
        border: 1px solid var(--glass-border-green);
        backdrop-filter: blur(14px);
        -webkit-backdrop-filter: blur(14px);
        border-radius: 12px;
        padding: 0.5rem;
        display: flex;
        align-items: center;
        margin-bottom: 2rem;
        height: 50px;
    }

    .home-btn {
        background: none;
        border: none;
        color: var(--lantern);
        width: 36px;
        height: 36px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        cursor: pointer;
        transition: all 0.2s;
    }

    .home-btn:hover {
        background: rgba(255, 184, 77, 0.12);
        color: var(--lantern-soft);
    }

    .divider {
        width: 1px;
        height: 24px;
        background: rgba(255, 255, 255, 0.1);
        margin: 0 0.5rem;
    }

    .path-scroll {
        display: flex;
        align-items: center;
        overflow-x: auto;
        scrollbar-width: none;
        white-space: nowrap;
        padding-right: 1rem;
        flex: 1;
    }
    
    .path-scroll::-webkit-scrollbar {
        display: none;
    }

    .crumb-wrapper {
        display: flex;
        align-items: center;
        animation: fadeSlide 0.3s ease-out;
    }

    .node {
        background: none;
        border: none;
        color: #e2e8f0;
        font-family: inherit;
        font-size: 0.9rem;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 6px;
        transition: all 0.2s ease;
        font-weight: 500;
    }

    .node:hover {
        background: rgba(255, 184, 77, 0.1);
        color: var(--lantern);
    }

    .placeholder {
        color: #64748b;
        font-family: monospace;
        margin-left: 0.5rem;
    }

    :global(.sep) {
        color: #64748b;
        margin: 0 2px;
    }

    @keyframes fadeSlide {
        from { opacity: 0; transform: translateX(-10px); }
        to { opacity: 1; transform: translateX(0); }
    }
</style>
