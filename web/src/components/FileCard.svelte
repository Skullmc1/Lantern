<script lang="ts">
    import type { FileItem } from "../lib/types";
    import {
        FileText,
        Folder,
        MoreHorizontal,
        Trash2,
        Edit3,
    } from "lucide-svelte";
    import { slide } from "svelte/transition";

    export let item: FileItem;
    export let onDelete: (item: FileItem) => void;
    export let onRename: (item: FileItem) => void;
    export let onNavigate: (item: FileItem) => void;

    let menuOpen = false;
</script>

<div class="card {menuOpen ? 'active' : ''}">
    <div
        class="card-body"
        on:click={() => onNavigate(item)}
        role="button"
        tabindex="0"
        on:keydown
    >
        <div class="visual">
            {#if item.isDirectory}
                <Folder
                    size={24}
                    color="#34d399"
                    fill="rgba(52, 211, 153, 0.1)"
                />
            {:else}
                <FileText size={24} color="#6ee7b7" />
            {/if}
        </div>

        <div class="meta">
            <span class="filename">{item.name}</span>
            {#if !item.isDirectory}
                <span class="size">{(item.size / 1024).toFixed(1)} KB</span>
            {/if}
        </div>
    </div>

    <button
        class="menu-trigger"
        on:click|stopPropagation={() => (menuOpen = !menuOpen)}
    >
        <MoreHorizontal size={18} />
    </button>

    {#if menuOpen}
        <div class="actions-panel" transition:slide={{ duration: 200 }}>
            <button
                on:click|stopPropagation={() => {
                    onRename(item);
                    menuOpen = false;
                }}
            >
                <Edit3 size={14} /> Rename
            </button>
            <button
                class="danger"
                on:click|stopPropagation={() => {
                    onDelete(item);
                    menuOpen = false;
                }}
            >
                <Trash2 size={14} /> Delete
            </button>
        </div>
    {/if}
</div>

<style>
    .card {
        background: linear-gradient(
            145deg,
            rgba(6, 78, 59, 0.4),
            rgba(4, 47, 46, 0.4)
        );
        border: 1px solid rgba(52, 211, 153, 0.1);
        border-radius: 16px;
        position: relative;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        overflow: hidden;
    }

    .card:hover {
        transform: translateY(-2px);
        border-color: rgba(52, 211, 153, 0.3);
        box-shadow:
            0 8px 30px rgba(0, 0, 0, 0.3),
            0 0 0 1px rgba(52, 211, 153, 0.1);
    }

    .card.active {
        background: rgba(6, 78, 59, 0.8);
        border-color: #34d399;
    }

    .card-body {
        padding: 1rem;
        display: flex;
        align-items: center;
        gap: 1rem;
        cursor: pointer;
    }

    .visual {
        padding: 0.75rem;
        background: rgba(0, 0, 0, 0.2);
        border-radius: 12px;
        display: flex;
    }

    .meta {
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }

    .filename {
        color: #ecfdf5;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: 1rem;
    }

    .size {
        font-size: 0.75rem;
        color: #6ee7b7;
        opacity: 0.7;
        margin-top: 2px;
        font-family: monospace;
    }

    .menu-trigger {
        position: absolute;
        right: 1rem;
        top: 1.25rem;
        background: none;
        border: none;
        color: #34d399;
        opacity: 0.5;
        cursor: pointer;
        transition: opacity 0.2s;
        padding: 4px;
    }

    .menu-trigger:hover,
    .card:hover .menu-trigger {
        opacity: 1;
    }

    .actions-panel {
        display: flex;
        border-top: 1px solid rgba(52, 211, 153, 0.1);
        background: rgba(0, 0, 0, 0.2);
    }

    .actions-panel button {
        flex: 1;
        background: none;
        border: none;
        color: #a7f3d0;
        padding: 12px;
        font-size: 0.85rem;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        transition: background 0.2s;
    }

    .actions-panel button:hover {
        background: rgba(52, 211, 153, 0.1);
        color: #fff;
    }

    .actions-panel button.danger:hover {
        background: rgba(220, 38, 38, 0.2);
        color: #fca5a5;
    }
</style>
