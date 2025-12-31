$ErrorActionPreference = "Stop"

Write-Host "Building Web UI..."
cd web
bun install
bun run build
cd ..

# Destination for WebView (accessible via file:///android_asset/)
$assetsDir = "app/src/main/assets/files"
if (Test-Path $assetsDir) {
    Remove-Item -Recurse -Force $assetsDir
}
New-Item -ItemType Directory -Force -Path $assetsDir | Out-Null

# Destination for Ktor Server (accessible via ClassLoader/resources)
$resourcesDir = "app/src/main/resources/files"
if (Test-Path $resourcesDir) {
    Remove-Item -Recurse -Force $resourcesDir
}
New-Item -ItemType Directory -Force -Path $resourcesDir | Out-Null

Write-Host "Copying assets to Android Assets (for WebView)..."
Copy-Item -Recurse -Force "web/dist/*" $assetsDir

Write-Host "Copying assets to Android Resources (for Ktor)..."
Copy-Item -Recurse -Force "web/dist/*" $resourcesDir

Write-Host "Web UI updated successfully."