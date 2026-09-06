<script lang="ts">
    import { Share2, Trash2, Link, ExternalLink, File as FileIcon, Folder } from "lucide-svelte";
    import { fade, slide } from "svelte/transition";

    interface SharedItem {
        token: string;
        name: string;
        isDirectory: boolean;
        createdAt: number;
    }

    export let shares: SharedItem[] = [];
    export let onRevoke: (token: string) => void;
    export let onPickFile: () => void;
    export let onPickFolder: () => void;
    export let onShowQr: (token: string) => void;

    function formatTime(ts: number) {
        return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
</script>

<div class="shared-manager">
    <div class="section-header">
        <h3><Share2 size={18} /> Shared Items</h3>
        <div class="add-actions">
            <button class="add-btn" on:click={onPickFile}>+ Share File</button>
            <button class="add-btn" on:click={onPickFolder}>+ Share Folder</button>
        </div>
    </div>

    {#if shares.length === 0}
        <div class="empty-state" in:fade>
            <p>No active magic links.</p>
        </div>
    {:else}
        <div class="share-list">
            {#each shares as share (share.token)}
                <div class="share-card" transition:slide>
                    <div class="type-icon">
                        {#if share.isDirectory}<Folder size={18}/>{:else}<FileIcon size={18}/>{/if}
                    </div>
                    <div class="share-info">
                        <span class="share-name">{share.name}</span>
                        <span class="share-meta">Token: {share.token} • {formatTime(share.createdAt)}</span>
                    </div>
                    <div class="share-actions">
                         <button class="action-btn" on:click={() => onShowQr(share.token)} title="Show QR">
                            <Link size={16} />
                        </button>
                        <button class="action-btn danger" on:click={() => onRevoke(share.token)} title="Revoke">
                            <Trash2 size={16} />
                        </button>
                    </div>
                </div>
            {/each}
        </div>
    {/if}
</div>

<style>
    .shared-manager {
        width: 100%;
        max-width: 400px;
        margin-top: 2rem;
        padding: 0 1.5rem;
        z-index: 20;
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    h3 {
        margin: 0;
        font-size: 0.9rem;
        color: var(--lantern-soft);
        text-transform: uppercase;
        letter-spacing: 1px;
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .add-actions {
        display: flex;
        gap: 0.5rem;
    }

    .add-btn {
        background: rgba(255, 184, 77, 0.1);
        border: 1px solid rgba(255, 184, 77, 0.3);
        color: var(--lantern);
        padding: 4px 10px;
        border-radius: 6px;
        font-size: 0.75rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .add-btn:hover {
        background: var(--lantern);
        color: #3a2406;
    }

    .empty-state {
        background: rgba(255, 255, 255, 0.03);
        border: 1px dashed rgba(255, 233, 179, 0.2);
        border-radius: 12px;
        padding: 2rem;
        text-align: center;
        color: #8aa397;
        font-size: 0.85rem;
    }

    .share-list {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .share-card {
        background: var(--glass);
        border: 1px solid var(--glass-border-green);
        border-radius: 12px;
        padding: 0.75rem;
        display: flex;
        align-items: center;
        gap: 0.75rem;
        backdrop-filter: blur(14px);
        -webkit-backdrop-filter: blur(14px);
    }

    .type-icon {
        color: #94a3b8;
    }

    .share-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        min-width: 0;
    }

    .share-name {
        color: #e2e8f0;
        font-size: 0.9rem;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .share-meta {
        font-size: 0.7rem;
        color: #64748b;
        font-family: monospace;
    }

    .share-actions {
        display: flex;
        gap: 0.25rem;
    }

    .action-btn {
        background: none;
        border: none;
        color: #64748b;
        padding: 6px;
        border-radius: 6px;
        cursor: pointer;
        display: flex;
        transition: all 0.2s;
    }

    .action-btn:hover {
        background: rgba(255, 184, 77, 0.12);
        color: var(--lantern);
    }

    .action-btn.danger:hover {
        color: #f87171;
        background: rgba(239, 68, 68, 0.1);
    }
</style>
