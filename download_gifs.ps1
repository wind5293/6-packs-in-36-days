$repo = "wind5293/Exercise_GIFs"
$releaseUrl = "https://api.github.com/repos/$repo/releases/latest"
$assetsDir = "app/src/main/assets/gifs"

# Ensure the destination directory exists
if (-not (Test-Path -Path $assetsDir)) {
    New-Item -ItemType Directory -Path $assetsDir | Out-Null
    Write-Host "Created directory: $assetsDir"
}

Write-Host "Fetching latest release from $repo..."
$release = Invoke-RestMethod -Uri $releaseUrl

if ($null -eq $release.assets -or $release.assets.Count -eq 0) {
    Write-Host "No assets found in the latest release."
    exit
}

Write-Host "Found $($release.assets.Count) GIFs to download."

$client = New-Object System.Net.WebClient

foreach ($asset in $release.assets) {
    $fileName = $asset.name
    $downloadUrl = $asset.browser_download_url
    $destination = Join-Path $assetsDir $fileName

    Write-Host "Downloading: $fileName..."
    try {
        $client.DownloadFile($downloadUrl, $destination)
        Write-Host "  -> Downloaded successfully to $destination"
    } catch {
        Write-Host "  -> Failed to download $fileName. Error: $_"
    }
}

Write-Host "All downloads finished!"
