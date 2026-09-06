<script lang="ts">
    import { UserPlus, X, CheckCircle } from "lucide-svelte";
    import { fly } from "svelte/transition";

    interface ClientSession {
        id: string;
        deviceName: string;
        ipAddress: string;
    }

    export let sessions: ClientSession[] = [];
    export let onApprove: (id: string) => void;
    export let onReject: (id: string) => void;
</script>

{#if sessions.length > 0}
    <div class="requests-stack" transition:fly={{ y: 50, duration: 300 }}>
        {#each sessions as session (session.id)}
            <div class="request-card">
                <div class="req-icon">
                    <UserPlus size={20} />
                </div>
                <div class="req-info">
                    <span class="req-name">{session.deviceName}</span>
                    <span class="req-ip">{session.ipAddress}</span>
                </div>
                <div class="req-actions">
                    <button class="btn-reject" on:click={() => onReject(session.id)}>
                        <X size={18} />
                    </button>
                    <button class="btn-approve" on:click={() => onApprove(session.id)}>
                        <CheckCircle size={18} />
                    </button>
                </div>
            </div>
        {/each}
    </div>
{/if}

<style>
    .requests-stack {
        position: absolute;
        bottom: 1rem;
        left: 0;
        right: 0;
        padding: 0 1.5rem;
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        max-height: 300px;
        overflow-y: auto;
        z-index: 20;
    }

    .request-card {
        background: var(--glass-strong);
        border: 1px solid var(--glass-border);
        border-radius: 12px;
        padding: 0.75rem 1rem;
        display: flex;
        align-items: center;
        gap: 1rem;
        box-shadow: 0 10px 25px rgba(0,0,0,0.45);
        backdrop-filter: blur(14px);
        -webkit-backdrop-filter: blur(14px);
    }
    
    .req-icon {
        background: rgba(255, 184, 77, 0.14);
        color: var(--lantern);
        padding: 8px;
        border-radius: 50%;
    }
    
    .req-info {
        flex: 1;
        display: flex;
        flex-direction: column;
    }
    
    .req-name {
        font-weight: 600;
        color: #ecfdf5;
        font-size: 0.95rem;
    }
    .req-ip {
        font-size: 0.75rem;
        color: #94a3b8;
        font-family: monospace;
    }
    
    .req-actions {
        display: flex;
        gap: 0.5rem;
    }
    
    .btn-approve, .btn-reject {
        border: none;
        width: 36px;
        height: 36px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: transform 0.2s;
    }
    
    .btn-approve {
        background: var(--lantern);
        color: #3a2406;
    }
    
    .btn-reject {
        background: rgba(239, 68, 68, 0.2);
        color: #f87171;
    }
    
    .btn-approve:active, .btn-reject:active {
        transform: scale(0.9);
    }
</style>
