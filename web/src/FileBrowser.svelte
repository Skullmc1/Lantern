<script lang="ts">
    import { onMount } from "svelte";
    import { fade, fly } from "svelte/transition";
    import { UploadCloud, Loader2 } from "lucide-svelte";
    import type { FileItem } from "./lib/types";
    import {
        fetchFiles,
        deleteFileItem,
        renameFileItem,
        uploadFile,
        getDownloadUrl,
    } from "./lib/fileService";
    import Navigator from "./components/Navigator.svelte";
    import FileCard from "./components/FileCard.svelte";

    let currentPath = "";
    let files: FileItem[] = [];
    let loading = true;
    let error = "";
    let fileInput: HTMLInputElement;

    async function load(path: string) {
        loading = true;
        error = "";
        try {
            const data = await fetchFiles(path);
            files = data.files;
            currentPath = data.path;
        } catch (e) {
            error = (e as Error).message;
        } finally {
            loading = false;
        }
    }

    function handleNavigate(item: FileItem) {
        if (item.isDirectory) {
            const newPath = currentPath
                ? `${currentPath}/${item.name}`
                : item.name;
            load(newPath);
        } else {
            const dlPath = currentPath
                ? `${currentPath}/${item.name}`
                : item.name;
            window.open(getDownloadUrl(dlPath), "_blank");
        }
    }

    async function handleDelete(item: FileItem) {
        if (!confirm(`Delete ${item.name}?`)) return;
        const itemPath = currentPath
            ? `${currentPath}/${item.name}`
            : item.name;
        try {
            await deleteFileItem(itemPath);
            load(currentPath);
        } catch (e) {
            alert((e as Error).message);
        }
    }

    async function handleRename(item: FileItem) {
        const newName = prompt("Rename to:", item.name);
        if (!newName || newName === item.name) return;
        const itemPath = currentPath
            ? `${currentPath}/${item.name}`
            : item.name;
        try {
            await renameFileItem(itemPath, newName);
            load(currentPath);
        } catch (e) {
            alert((e as Error).message);
        }
    }

    async function onFileUpload(e: Event) {
        const target = e.target as HTMLInputElement;
        if (!target.files?.length) return;

        try {
            loading = true;
            await uploadFile(currentPath, target.files[0]);
            load(currentPath);
        } catch (e) {
            alert((e as Error).message);
            loading = false;
        } finally {
            target.value = "";
        }
    }

    onMount(() => load(""));
</script>

<div class="lantern-shell">
    <div class="glow-orb"></div>

    <div class="content-wrapper">
        <header>
            <h1>Lantern</h1>
        </header>

        <Navigator {currentPath} onNavigate={(path) => load(path)} />

        <div class="viewport">
            {#if loading}
                <div class="center-state" in:fade>
                    <div class="spinner">
                        <Loader2 size={40} class="spin" />
                    </div>
                </div>
            {:else if error}
                <div class="center-state error" in:fade>
                    <p>{error}</p>
                </div>
            {:else if files.length === 0}
                <div class="center-state empty" in:fade>
                    <p>Directory Empty</p>
                </div>
            {:else}
                <div class="grid">
                    {#each files as file (file.name)}
                        <div in:fly={{ y: 20, duration: 300 }}>
                            <FileCard
                                item={file}
                                onNavigate={handleNavigate}
                                onDelete={handleDelete}
                                onRename={handleRename}
                            />
                        </div>
                    {/each}
                </div>
            {/if}
        </div>
    </div>

    <button class="fab" on:click={() => fileInput.click()}>
        <UploadCloud size={24} />
        <span class="fab-text">Upload</span>
    </button>
    <input
        type="file"
        bind:this={fileInput}
        on:change={onFileUpload}
        style="display: none;"
    />
</div>

<style>
    :global(body) {
        margin: 0;
        background-color: #020617;
        font-family: "Inter", sans-serif;
    }

    .lantern-shell {
        min-height: 100vh;
        background: radial-gradient(
            circle at top left,
            #064e3b 0%,
            #020617 40%
        );
        color: #ecfdf5;
        position: relative;
        overflow-x: hidden;
    }

    .glow-orb {
        position: fixed;
        top: -100px;
        right: -100px;
        width: 400px;
        height: 400px;
        background: #10b981;
        filter: blur(150px);
        opacity: 0.15;
        pointer-events: none;
        z-index: 0;
    }

    .content-wrapper {
        position: relative;
        z-index: 1;
        max-width: 800px;
        margin: 0 auto;
        padding: 2rem 1.5rem 6rem 1.5rem;
    }

    header {
        margin-bottom: 1.5rem;
    }

    h1 {
        font-size: 2.5rem;
        font-weight: 800;
        margin: 0;
        background: linear-gradient(to right, #34d399, #059669);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        letter-spacing: -1px;
    }

    .grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 1rem;
    }

    .center-state {
        min-height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #6ee7b7;
        font-family: monospace;
    }

    .error {
        color: #f87171;
    }

    :global(.spin) {
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        from {
            transform: rotate(0deg);
        }
        to {
            transform: rotate(360deg);
        }
    }

    .fab {
        position: fixed;
        bottom: 2rem;
        right: 2rem;
        background: #10b981;
        color: #022c22;
        border: none;
        border-radius: 50px;
        padding: 1rem 1.5rem;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-weight: 700;
        box-shadow: 0 4px 20px rgba(16, 185, 129, 0.4);
        cursor: pointer;
        z-index: 100;
        transition:
            transform 0.2s,
            box-shadow 0.2s;
    }

    .fab:hover {
        transform: translateY(-4px) scale(1.05);
        box-shadow: 0 10px 30px rgba(16, 185, 129, 0.6);
        background: #34d399;
    }

    @media (max-width: 600px) {
        .grid {
            grid-template-columns: 1fr;
        }
        .fab-text {
            display: none;
        }
        .fab {
            padding: 1rem;
            border-radius: 50%;
        }
        h1 {
            font-size: 2rem;
        }
    }
</style>
