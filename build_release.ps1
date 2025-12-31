$ErrorActionPreference = "Stop"

Write-Host "Building Lantern..."
./gradlew clean assembleDebug

# Extract version name from build.gradle.kts
$buildFile = Get-Content "app/build.gradle.kts"
$versionLine = $buildFile | Select-String 'versionName = "(.*)"'
if ($versionLine -match 'versionName = "(.*)"') {
    $version = $matches[1]
} else {
    Write-Warning "Could not find version name, defaulting to 1.0"
    $version = "1.0"
}

$apkPath = "app/build/outputs/apk/debug/app-debug.apk"
$destPath = "Lantern-$version.apk"

if (Test-Path $apkPath) {
    Copy-Item $apkPath $destPath
    Write-Host "Build Success! APK copied to: $destPath"
} else {
    Write-Error "APK file not found at $apkPath"
}
