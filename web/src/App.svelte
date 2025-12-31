<script lang="ts">
  import { onMount } from 'svelte';
  import './lib/android'; // Import types
  import ControlPanel from './ControlPanel.svelte';
  import FileBrowser from './FileBrowser.svelte';

  let isAndroidApp = false;

  onMount(() => {
    // Check if running inside Android WebView with our interface
    if (window.Android) {
        isAndroidApp = true;
    }
  });
</script>

{#if isAndroidApp}
  <ControlPanel />
{:else}
  <FileBrowser />
{/if}