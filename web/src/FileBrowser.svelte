<script lang="ts">
    import { onMount } from "svelte";
    import { fade, fly, slide } from "svelte/transition";
    import {
        UploadCloud,
        Loader2,
        Search,
        Grid,
        List as ListIcon,
        ArrowDownAZ,
        ArrowUpAZ,
        ArrowDown01,
        ArrowUp01,
        Menu,
        X
    } from "lucide-svelte";
    import type { FileItem } from "./lib/types";
    import {
        fetchFiles,
        fetchSharedFiles,
        deleteFileItem,
        renameFileItem,
        uploadFile,
        getDownloadUrl,
    } from "./lib/fileService";
    import Navigator from "./components/Navigator.svelte";
    import FileCard from "./components/FileCard.svelte";

    export let shareToken: string = "";

    let currentPath = "";
    let files: FileItem[] = [];
    let loading = true;
    let error = "";
    let fileInput: HTMLInputElement;

    // UI State
    let viewMode: "grid" | "list" = "grid";
    let searchQuery = "";
    let sortBy: "name" | "size" = "name";
    let sortDesc = false;
    let mobileMenuOpen = false;

    // Computed
    $: filteredFiles = files
        .filter((f) =>
            f.name.toLowerCase().includes(searchQuery.toLowerCase())
        )
        .sort((a, b) => {
            let res = 0;
            // Always put directories first
            if (a.isDirectory !== b.isDirectory) {
                return a.isDirectory ? -1 : 1;
            }
            if (sortBy === "name") {
                res = a.name.localeCompare(b.name);
            } else {
                res = a.size - b.size;
            }
            return sortDesc ? -res : res;
        });

    async function load(path: string) {
        loading = true;
        error = "";
        try {
            let data;
            if (shareToken) {
                data = await fetchSharedFiles(shareToken, path);
            } else {
                data = await fetchFiles(path);
            }
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
            searchQuery = ""; // Clear search on nav
        } else {
            const dlPath = currentPath
                ? `${currentPath}/${item.name}`
                : item.name;
            window.open(getDownloadUrl(dlPath, shareToken), "_blank");
        }
    }

    async function handleDelete(item: FileItem) {
        if (shareToken) return; // Read-only
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
        if (shareToken) return; // Read-only
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
        if (shareToken) return; // Read-only
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

    function toggleSort() {
        if (sortBy === "name") {
            if (!sortDesc) sortDesc = true;
            else {
                sortBy = "size";
                sortDesc = true; // default to largest first
            }
        } else {
            if (sortDesc) sortDesc = false;
            else {
                sortBy = "name";
                sortDesc = false;
            }
        }
    }

    onMount(() => load(""));
</script>

<div class="lantern-shell">
    <div class="background-fx">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="grid-overlay"></div>
    </div>

    <div class="main-container">
        <header>
            <div class="brand">
                <div class="logo-mark"></div>
                <h1>Lantern {shareToken ? '(Shared)' : ''}</h1>
            </div>
            
            <div class="controls-desktop">
                <div class="search-bar">
                    <Search size={16} />
                    <input 
                        type="text" 
                        placeholder="Filter files..." 
                        bind:value={searchQuery}
                    />
                </div>

                <div class="actions">
                    <button class="icon-btn" on:click={toggleSort} title="Sort">
                        {#if sortBy === 'name'}
                            {#if sortDesc}<ArrowDownAZ size={18} />{:else}<ArrowUpAZ size={18} />{/if}
                        {:else}
                            {#if sortDesc}<ArrowDown01 size={18} />{:else}<ArrowUp01 size={18} />{/if}
                        {/if}
                    </button>
                    <div class="divider"></div>
                    <button 
                        class="icon-btn {viewMode === 'grid' ? 'active' : ''}" 
                        on:click={() => viewMode = 'grid'}
                    >
                        <Grid size={18} />
                    </button>
                    <button 
                        class="icon-btn {viewMode === 'list' ? 'active' : ''}" 
                        on:click={() => viewMode = 'list'}
                    >
                        <ListIcon size={18} />
                    </button>
                </div>
            </div>

            <!-- Mobile Controls Toggle -->
            <button class="mobile-menu-btn" on:click={() => mobileMenuOpen = !mobileMenuOpen}>
                {#if mobileMenuOpen}<X />{:else}<Menu />{/if}
            </button>
        </header>

        {#if mobileMenuOpen}
            <div class="mobile-controls" transition:slide>
                 <div class="search-bar mobile">
                    <Search size={16} />
                    <input 
                        type="text" 
                        placeholder="Filter files..." 
                        bind:value={searchQuery}
                    />
                </div>
                <div class="mobile-actions">
                     <button class="icon-btn" on:click={toggleSort}>
                        Sort: {sortBy} {sortDesc ? '↓' : '↑'}
                    </button>
                    <div class="view-toggles">
                        <button 
                            class="icon-btn {viewMode === 'grid' ? 'active' : ''}" 
                            on:click={() => viewMode = 'grid'}
                        >
                            <Grid size={18} />
                        </button>
                        <button 
                            class="icon-btn {viewMode === 'list' ? 'active' : ''}" 
                            on:click={() => viewMode = 'list'}
                        >
                            <ListIcon size={18} />
                        </button>
                    </div>
                </div>
            </div>
        {/if}

        <Navigator {currentPath} onNavigate={(path) => load(path)} />

        <div class="viewport">
            {#if loading}
                <div class="center-state" in:fade>
                    <div class="spinner-container">
                        <Loader2 size={40} class="spin" />
                        <p>Scanning...</p>
                    </div>
                </div>
            {:else if error}
                <div class="center-state error" in:fade>
                    <p>{error}</p>
                </div>
            {:else if filteredFiles.length === 0}
                <div class="center-state empty" in:fade>
                    <p>No files found</p>
                </div>
            {:else}
                <div class="file-grid {viewMode}">
                    {#each filteredFiles as file (file.name)}
                        <div in:fly={{ y: 10, duration: 200 }} class="file-wrapper">
                            <FileCard
                                item={file}
                                viewMode={viewMode}
                                onNavigate={handleNavigate}
                                onDelete={shareToken ? undefined : handleDelete}
                                onRename={shareToken ? undefined : handleRename}
                            />
                        </div>
                    {/each}
                </div>
            {/if}
        </div>
    </div>

    {#if !shareToken}
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
    {/if}
</div>

<style>
    :global(body) {
        margin: 0;
        background-color: #020617;
        font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }

    .lantern-shell {
        min-height: 100vh;
        position: relative;
        overflow-x: hidden;
        color: #e2e8f0;
    }

    /* --- Animated Background --- */
    .background-fx {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        z-index: 0;
        pointer-events: none;
        overflow: hidden;
    }

    .grid-overlay {
        position: absolute;
        inset: 0;
        background-image: 
            linear-gradient(rgba(16, 185, 129, 0.03) 1px, transparent 1px),
            linear-gradient(90deg, rgba(16, 185, 129, 0.03) 1px, transparent 1px);
        background-size: 40px 40px;
    }

    .orb {
        position: absolute;
        border-radius: 50%;
        filter: blur(80px);
        opacity: 0.4;
        animation: float 20s infinite ease-in-out;
    }

    .orb-1 {
        top: -10%;
        right: -10%;
        width: 600px;
        height: 600px;
        background: radial-gradient(circle, #059669 0%, transparent 70%);
    }

    .orb-2 {
        bottom: -20%;
        left: -10%;
        width: 500px;
        height: 500px;
        background: radial-gradient(circle, #047857 0%, transparent 70%);
        animation-delay: -5s;
    }

    @keyframes float {
        0%, 100% { transform: translate(0, 0); }
        50% { transform: translate(30px, 50px); }
    }

    /* --- Layout --- */
    .main-container {
        position: relative;
        z-index: 1;
        max-width: 1200px;
        margin: 0 auto;
        padding: 1.5rem;
        padding-bottom: 6rem;
    }

    header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 2rem;
    }

    .brand {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .logo-mark {
        width: 12px;
        height: 12px;
        background: #34d399;
        border-radius: 50%;
        box-shadow: 0 0 15px #34d399;
        animation: pulse 3s infinite;
    }

    @keyframes pulse {
        0% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.7); }
        70% { box-shadow: 0 0 0 10px rgba(52, 211, 153, 0); }
        100% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0); }
    }

    h1 {
        font-size: 1.5rem;
        font-weight: 700;
        margin: 0;
        background: linear-gradient(to right, #ecfdf5, #34d399);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        letter-spacing: -0.5px;
    }

    /* --- Controls --- */
    .controls-desktop {
        display: flex;
        gap: 1rem;
        align-items: center;
    }

    .search-bar {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        background: rgba(2, 6, 23, 0.4);
        border: 1px solid rgba(52, 211, 153, 0.2);
        padding: 0.5rem 1rem;
        border-radius: 99px;
        transition: all 0.2s;
        width: 250px;
    }

    .search-bar:focus-within {
        border-color: #34d399;
        box-shadow: 0 0 0 2px rgba(52, 211, 153, 0.1);
        background: rgba(2, 6, 23, 0.6);
    }

    .search-bar input {
        background: none;
        border: none;
        color: white;
        width: 100%;
        outline: none;
        font-size: 0.9rem;
    }

    .search-bar :global(svg) {
        color: #64748b;
    }

    .actions {
        display: flex;
        align-items: center;
        gap: 0.25rem;
        background: rgba(2, 6, 23, 0.4);
        padding: 0.25rem;
        border-radius: 12px;
        border: 1px solid rgba(52, 211, 153, 0.2);
    }

    .icon-btn {
        background: none;
        border: none;
        color: #94a3b8;
        padding: 0.5rem;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .icon-btn:hover {
        background: rgba(52, 211, 153, 0.1);
        color: #34d399;
    }

    .icon-btn.active {
        background: #34d399;
        color: #020617;
    }

    .divider {
        width: 1px;
        height: 20px;
        background: rgba(255, 255, 255, 0.1);
        margin: 0 0.25rem;
    }

    /* --- Mobile Menu --- */
    .mobile-menu-btn {
        display: none;
        background: none;
        border: none;
        color: #34d399;
        cursor: pointer;
    }

    .mobile-controls {
        background: rgba(6, 78, 59, 0.2);
        padding: 1rem;
        border-radius: 16px;
        margin-bottom: 1.5rem;
        border: 1px solid rgba(52, 211, 153, 0.1);
    }

    .mobile-actions {
        display: flex;
        justify-content: space-between;
        margin-top: 1rem;
        align-items: center;
    }

    .view-toggles {
        display: flex;
        gap: 0.5rem;
    }

    /* --- Grid System --- */
    .file-grid {
        display: grid;
        gap: 1rem;
        transition: all 0.3s;
    }

    .file-grid.grid {
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    }

    .file-grid.list {
        grid-template-columns: 1fr;
        gap: 0.5rem;
    }

    /* --- States --- */
    .center-state {
        min-height: 300px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #6ee7b7;
    }

    .spinner-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 1rem;
    }

    .error { color: #f87171; }
    .empty { color: #64748b; font-style: italic; }

    :global(.spin) { animation: spin 1s linear infinite; }
    @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

    /* --- FAB --- */
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
        box-shadow: 0 10px 25px -5px rgba(16, 185, 129, 0.5);
        cursor: pointer;
        z-index: 100;
        transition: transform 0.2s, box-shadow 0.2s;
    }

    .fab:hover {
        transform: translateY(-4px) scale(1.05);
        background: #34d399;
        box-shadow: 0 15px 30px -5px rgba(16, 185, 129, 0.6);
    }

    /* --- Responsive --- */
    @media (max-width: 768px) {
        .controls-desktop {
            display: none;
        }

        .mobile-menu-btn {
            display: block;
        }

        .search-bar.mobile {
            width: 100%;
        }

        .file-grid.grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }

        .fab-text { display: none; }
        .fab { padding: 1rem; border-radius: 50%; }
        
        .main-container {
            padding: 1rem;
            padding-bottom: 5rem;
        }
    }
</style>
