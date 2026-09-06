<script lang="ts">
  import { onMount } from 'svelte';
  import './lib/android'; // Import types
  import GrassPlains from './components/GrassPlains.svelte';
  import ControlPanel from './ControlPanel.svelte';
  import FileBrowser from './FileBrowser.svelte';
  import WaitingRoom from './WaitingRoom.svelte';
  import RejectedScreen from './RejectedScreen.svelte';
  import { checkAuthStatus, requestAuth } from './lib/authService';

  let isAndroidApp = false;
  let authStatus: "CHECKING" | "PENDING" | "APPROVED" | "REJECTED" | "SHARED" = "CHECKING";
  let shareToken = "";

  onMount(async () => {
    // Preview-only: when a demo auth state is requested via ?demo=, reset any
    // stored session so each switch starts a fresh request to the mock server.
    const params = new URLSearchParams(window.location.search);
    if (params.get("demo")) {
      localStorage.removeItem("lantern_auth");
    }

    // Check if running inside Android WebView with our interface
    if (window.Android) {
        isAndroidApp = true;
        return;
    }
    
    // Check for Magic Link
    const path = window.location.pathname;
    if (path.startsWith("/s/")) {
        shareToken = path.split("/s/")[1];
        if (shareToken) {
            authStatus = "SHARED";
            return;
        }
    }
    const shareParam = params.get("share");
    if (shareParam) {
        shareToken = shareParam;
        authStatus = "SHARED";
        return;
    }

    // Auth Flow for Web Clients
    const status = await checkAuthStatus();
    if (status === "NONE") {
        // New session needed
        try {
            const deviceName = "Web Client " + Math.floor(Math.random() * 1000); // Simple ID for now
            const newStatus = await requestAuth(deviceName);
            authStatus = newStatus as any;
        } catch (e) {
            console.error("Auth init failed", e);
        }
    } else {
        authStatus = status as any;
    }
  });
</script>

{#if isAndroidApp}
  <ControlPanel />
{:else}
  {#if authStatus === "APPROVED"}
    <FileBrowser />
  {:else if authStatus === "SHARED"}
    <FileBrowser shareToken={shareToken} />
  {:else if authStatus === "PENDING"}
    <WaitingRoom onApproved={() => authStatus = "APPROVED"} />
  {:else if authStatus === "REJECTED"}
    <RejectedScreen />
  {:else}
    <!-- Loading state -->
    <div style="position: relative; background: var(--color-bg); height: 100vh; overflow: hidden;">
      <GrassPlains />
    </div>
  {/if}
{/if}