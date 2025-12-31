<script lang="ts">
    import { Terminal, ChevronRight } from "lucide-svelte";

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
    <div class="icon-zone">
        <Terminal size={16} />
    </div>
    <div class="path-scroll">
        <button class="node root" on:click={() => onNavigate("")}>
            ~/root
        </button>

        {#each getParts(currentPath) as part}
            <ChevronRight size={12} class="sep" />
            <button class="node" on:click={() => onNavigate(part.fullPath)}>
                {part.name}
            </button>
        {/each}
    </div>
</div>

<style>
    .navigator {
        background: rgba(6, 78, 59, 0.3);
        border: 1px solid rgba(52, 211, 153, 0.2);
        backdrop-filter: blur(10px);
        border-radius: 16px;
        padding: 0.5rem;
        display: flex;
        align-items: center;
        gap: 0.75rem;
        margin-bottom: 2rem;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    }

    .icon-zone {
        background: rgba(16, 185, 129, 0.1);
        color: #34d399;
        width: 32px;
        height: 32px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
    }

    .path-scroll {
        display: flex;
        align-items: center;
        gap: 0.25rem;
        overflow-x: auto;
        scrollbar-width: none;
        white-space: nowrap;
        padding-right: 1rem;
    }

    .node {
        background: none;
        border: none;
        color: #a7f3d0;
        font-family: "Courier New", Courier, monospace;
        font-size: 0.9rem;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 6px;
        transition: all 0.2s ease;
    }

    .node:hover {
        background: rgba(52, 211, 153, 0.1);
        color: #fff;
        text-shadow: 0 0 8px rgba(52, 211, 153, 0.5);
    }

    .root {
        color: #34d399;
        font-weight: bold;
    }

    :global(.sep) {
        color: #065f46;
    }
</style>
