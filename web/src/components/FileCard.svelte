<script lang="ts">
    import type { FileItem } from "../lib/types";
    import {
        FileText,
        Folder,
        MoreVertical,
        Trash2,
        Edit3,
        Image,
        Music,
        Video,
        Code,
        Download
    } from "lucide-svelte";
    import { slide } from "svelte/transition";
    import { getDownloadUrl } from "../lib/fileService";

    export let item: FileItem;
    export let viewMode: 'grid' | 'list' = 'grid';
    export let onDelete: (item: FileItem) => void = () => {};
    export let onRename: (item: FileItem) => void = () => {};
    export let onNavigate: (item: FileItem) => void = () => {};

    let menuOpen = false;
    let cardRef: HTMLDivElement;

    function getIcon(name: string, isDir: boolean) {
        if (isDir) return Folder;
        const ext = name.split('.').pop()?.toLowerCase();
        if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext || '')) return Image;
        if (['mp3', 'wav', 'ogg'].includes(ext || '')) return Music;
        if (['mp4', 'mkv', 'webm'].includes(ext || '')) return Video;
        if (['js', 'ts', 'html', 'css', 'json', 'py', 'kt'].includes(ext || '')) return Code;
        return FileText;
    }

    $: Icon = getIcon(item.name, item.isDirectory);

    function handleOutsideClick(event: MouseEvent) {
        if (menuOpen && cardRef && !cardRef.contains(event.target as Node)) {
            menuOpen = false;
        }
    }

    function handleKeydown(event: KeyboardEvent) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            if (item.isDirectory) onNavigate(item);
        }
    }

    function handleMenuKeydown(event: KeyboardEvent) {
        if (event.key === "Escape") menuOpen = false;
    }

    function handleDownload() {
        // Construct path from onNavigate if available, but FileItem doesn't store full path.
        // The parent FileBrowser handles paths. We can't easily get full path here unless passed.
        // BUT: onNavigate usually takes the item. 
        // We'll rely on the parent logic via onNavigate for simple open,
        // but for specific download action we might need to emit an event or just call onNavigate 
        // if onNavigate handles downloads for files (which it currently does).
        // Wait, onNavigate opens file in new tab which triggers download.
        // Let's just use that for now, effectively "Download/Open".
        onNavigate(item);
        menuOpen = false;
    }
</script>

<svelte:window on:click={handleOutsideClick} />

<div class="card {viewMode} {menuOpen ? 'active' : ''}" bind:this={cardRef}>
    <div
        class="card-body"
        on:click={() => {
            if (item.isDirectory) onNavigate(item);
            // If file, do nothing on main click as requested to avoid accidental downloads
        }}
        on:keydown={handleKeydown}
        role="button"
        tabindex="0"
    >
        <div class="visual">
            <svelte:component 
                this={Icon} 
                size={viewMode === 'list' ? 20 : 28} 
                color={item.isDirectory ? '#34d399' : '#94a3b8'} 
                fill={item.isDirectory ? 'rgba(52, 211, 153, 0.1)' : 'none'}
            />
        </div>

        <div class="meta">
            <span class="filename" title={item.name}>{item.name}</span>
            {#if !item.isDirectory}
                <span class="size">{(item.size / 1024).toFixed(1)} KB</span>
            {/if}
        </div>
    </div>

    <button
        class="menu-trigger"
        on:click|stopPropagation={() => (menuOpen = !menuOpen)}
        on:keydown={handleMenuKeydown}
        aria-label="More options"
        aria-expanded={menuOpen}
    >
        <MoreVertical size={16} />
    </button>

    {#if menuOpen}
        <div class="actions-popover" role="menu" transition:slide={{ duration: 150 }}>
            {#if !item.isDirectory}
                <button
                    role="menuitem"
                    on:click|stopPropagation={handleDownload}
                >
                    <Download size={14} /> Download
                </button>
            {/if}
            <button
                role="menuitem"
                on:click|stopPropagation={() => {
                    onRename(item);
                    menuOpen = false;
                }}
            >
                <Edit3 size={14} /> Rename
            </button>
            <button
                class="danger"
                role="menuitem"
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
        background: var(--glass);
        border: 1px solid var(--glass-border-green);
        border-radius: 12px;
        position: relative;
        transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        overflow: hidden; /* Contains the slide menu */
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
    }

    /* --- Hover Effects --- */
    .card:hover {
        background: var(--glass-strong);
        border-color: rgba(255, 184, 77, 0.35);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
    }
    
    .card.active {
        border-color: var(--lantern);
        background: rgba(255, 184, 77, 0.08);
        z-index: 10; 
    }

    /* --- Layouts --- */
    .card.grid {
        height: 100%;
        display: flex;
        flex-direction: column;
    }

    .card.list {
        display: flex;
        align-items: center;
        flex-direction: row;
    }

    .card-body {
        padding: 0.75rem;
        display: flex;
        align-items: center;
        gap: 0.75rem;
        cursor: pointer;
        flex: 1;
        min-width: 0; /* Text truncation fix */
    }

    .card.grid .card-body {
        flex-direction: column;
        text-align: center;
        padding: 1.25rem 0.75rem;
    }

    /* --- Visual Icon --- */
    .visual {
        padding: 0.5rem;
        background: rgba(10, 36, 23, 0.4);
        border-radius: 10px;
        display: flex;
        flex-shrink: 0;
    }

    .card.grid .visual {
        padding: 0.75rem;
        background: rgba(255, 184, 77, 0.06);
        margin-bottom: 0.5rem;
    }

    /* --- Meta Data --- */
    .meta {
        display: flex;
        flex-direction: column;
        overflow: hidden;
        min-width: 0;
        width: 100%;
    }

    .filename {
        color: #e2e8f0;
        font-weight: 500;
        font-size: 0.95rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .size {
        font-size: 0.75rem;
        color: #64748b;
        margin-top: 2px;
    }

    /* --- Menu Button --- */
    .menu-trigger {
        background: none;
        border: none;
        color: #64748b;
        cursor: pointer;
        padding: 0.5rem;
        border-radius: 6px;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 0.25rem;
    }

    .card.grid .menu-trigger {
        position: absolute;
        top: 0.5rem;
        right: 0.5rem;
        margin: 0;
    }

    .menu-trigger:hover {
        background: rgba(255, 184, 77, 0.1);
        color: #eef3ec;
    }

    /* --- Actions Popover --- */
    .actions-popover {
        position: absolute;
        inset: 0;
        background: rgba(10, 24, 18, 0.92);
        backdrop-filter: blur(10px);
        display: flex;
        flex-direction: column; /* Or row depending on preference */
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        z-index: 20;
    }
    
    .card.list .actions-popover {
        flex-direction: row;
        padding: 0 1rem;
        justify-content: flex-end;
        left: auto;
        width: auto;
        background: none;
        backdrop-filter: none;
        /* For list view, we might want it differently, 
           but absolute inset works for overlaying content. 
           Let's make it cover the whole card for simple mobile touch targets */
    }

    .actions-popover button {
        background: rgba(255, 255, 255, 0.05);
        border: none;
        color: #e2e8f0;
        padding: 0.5rem 1rem;
        border-radius: 8px;
        font-size: 0.85rem;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        width: 80%; /* Good for grid view overlay */
        justify-content: center;
    }
    
    .card.list .actions-popover button {
        width: auto;
        background: var(--glass-strong);
        border: 1px solid var(--glass-border);
    }

    .actions-popover button:hover {
        background: rgba(255, 184, 77, 0.12);
        color: var(--lantern);
    }

    .actions-popover button.danger {
        color: #f87171;
    }
    
    .actions-popover button.danger:hover {
        background: rgba(220, 38, 38, 0.1);
    }
</style>
