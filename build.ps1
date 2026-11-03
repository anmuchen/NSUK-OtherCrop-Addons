param(
    [string]$McRoot = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$versionDir = Split-Path -Parent $root
$versionName = Split-Path -Leaf $versionDir
if (-not $McRoot) {
    $McRoot = (Resolve-Path (Join-Path $versionDir "../..")).Path
}

$versionJson = Join-Path $versionDir "$versionName.json"
$vanillaJar = Join-Path $versionDir "$versionName.jar"
$libraries = Join-Path $McRoot "libraries"
if (-not (Test-Path $versionJson)) {
    throw "Version json not found: $versionJson"
}

$json = Get-Content -Raw -Encoding UTF8 $versionJson | ConvertFrom-Json
$neoForgeVersion = "21.1.250"
for ($i = 0; $i -lt $json.arguments.jvm.Count; $i++) {
    if ($json.arguments.jvm[$i] -eq "--fml.neoForgeVersion") {
        $neoForgeVersion = $json.arguments.jvm[$i + 1]
    }
}
$neoClient = Join-Path $libraries "net/neoforged/neoforge/$neoForgeVersion/neoforge-$neoForgeVersion-client.jar"
$neoUniversal = Join-Path $libraries "net/neoforged/neoforge/$neoForgeVersion/neoforge-$neoForgeVersion-universal.jar"
if (-not (Test-Path $neoClient)) { throw "NeoForge artifact not found: $neoClient" }

$mojmapJar = Join-Path $versionDir "nsukcropapi-dev/build/mc-mojmap-1.21.1.jar"
if (-not (Test-Path $mojmapJar)) { throw "Mojmap compile jar not found: $mojmapJar" }

$apiJar = Join-Path $versionDir "nsukcropapi-dev/build/libs/nsukcropapi-1.0.0.jar"
if (-not (Test-Path $apiJar)) { throw "nsukcropapi jar not found, build nsukcropapi-dev first: $apiJar" }

$classpath = @($neoClient, $neoUniversal, $mojmapJar, $apiJar)
foreach ($lib in $json.libraries) {
    if ($lib.downloads -and $lib.downloads.artifact -and $lib.downloads.artifact.path) {
        $p = Join-Path $libraries ($lib.downloads.artifact.path -replace '/', '\')
        if (Test-Path $p) { $classpath += $p }
    }
}

$buildDir = Join-Path $root "build"
$classesDir = Join-Path $buildDir "classes"
$libsDir = Join-Path $buildDir "libs"
$resources = Join-Path $root "src/main/resources"
if (Test-Path $classesDir) { Remove-Item -Recurse -Force $classesDir }
New-Item -ItemType Directory -Force -Path $classesDir, $libsDir | Out-Null

$sources = Get-ChildItem (Join-Path $root "src/main/java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& javac --release 21 -encoding UTF-8 -proc:none -cp ($classpath -join ";") -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw "Mod compilation failed" }

$outJar = Join-Path $libsDir "nsukothercropaddons-1.0.0.jar"
if (Test-Path $outJar) { Remove-Item -Force $outJar }
& jar --create --file $outJar -C $classesDir . -C $resources .
if ($LASTEXITCODE -ne 0) { throw "Packaging failed" }

Write-Host "Done: $outJar" -ForegroundColor Green
