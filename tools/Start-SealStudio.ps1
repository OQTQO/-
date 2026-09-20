param(
    [switch]$CompileOnly,
    [switch]$ForceLocalJdk
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version 2.0

$Root = Split-Path -Parent $PSScriptRoot
$RuntimeDir = Join-Path $Root ".runtime"
$LocalJdk = Join-Path $RuntimeDir "jdk"
$OutDir = Join-Path $Root "out"
$MainClass = "cn.localhost01.seal.ui.SealStudioApp"

function Write-Step([string]$Message) {
    Write-Host "[SealStudio] $Message"
}

function Get-JavacMajor([string]$JavacPath) {
    try {
        $text = (& $JavacPath -version 2>&1 | Out-String).Trim()
        if ($text -match "javac\s+(\d+)(?:\.(\d+))?") {
            $major = [int]$Matches[1]
            if ($major -eq 1 -and $Matches[2]) {
                $major = [int]$Matches[2]
            }
            return $major
        }
    } catch {
    }
    return 0
}

function Test-Jdk([string]$JavacPath) {
    if ([string]::IsNullOrWhiteSpace($JavacPath)) {
        return $false
    }
    if (-not (Test-Path -LiteralPath $JavacPath -PathType Leaf)) {
        return $false
    }
    return (Get-JavacMajor $JavacPath) -ge 17
}

function Find-UsableJavac {
    $candidates = New-Object System.Collections.Generic.List[string]

    $local = Join-Path $LocalJdk "bin\javac.exe"
    if (Test-Path -LiteralPath $local) {
        $candidates.Add($local)
    }

    foreach ($javaHomeCandidate in @($env:JAVA_HOME, $env:JDK_HOME)) {
        if (-not [string]::IsNullOrWhiteSpace($javaHomeCandidate)) {
            $candidates.Add((Join-Path $javaHomeCandidate "bin\javac.exe"))
        }
    }

    $cmd = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($null -ne $cmd) {
        $candidates.Add($cmd.Source)
    }

    foreach ($candidate in $candidates) {
        if (Test-Jdk $candidate) {
            return $candidate
        }
    }

    return $null
}

function Get-AdoptiumArch {
    $arch = [System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture.ToString().ToLowerInvariant()
    switch ($arch) {
        "x64"   { return "x64" }
        "arm64" { return "aarch64" }
        "x86"   { return "x32" }
        default { throw "Unsupported Windows CPU architecture: $arch" }
    }
}

function Download-LocalJdk {
    New-Item -ItemType Directory -Force -Path $RuntimeDir | Out-Null

    if (Test-Path -LiteralPath $LocalJdk) {
        Remove-Item -LiteralPath $LocalJdk -Recurse -Force
    }

    $zip = Join-Path $RuntimeDir "temurin17.zip"
    $extract = Join-Path $RuntimeDir "_jdk_extract"
    if (Test-Path -LiteralPath $zip) {
        Remove-Item -LiteralPath $zip -Force
    }
    if (Test-Path -LiteralPath $extract) {
        Remove-Item -LiteralPath $extract -Recurse -Force
    }

    $arch = Get-AdoptiumArch
    $url = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/$arch/jdk/hotspot/normal/eclipse"
    Write-Step "JDK 17 not found. Downloading Eclipse Temurin 17 ($arch)..."

    $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
    if ($null -ne $curl) {
        & $curl.Source -L --fail --retry 3 --connect-timeout 20 -o $zip $url
        if ($LASTEXITCODE -ne 0) {
            throw "JDK download failed via curl.exe."
        }
    } else {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
    }

    Write-Step "Extracting JDK..."
    New-Item -ItemType Directory -Force -Path $extract | Out-Null
    Expand-Archive -LiteralPath $zip -DestinationPath $extract -Force

    $javac = Get-ChildItem -LiteralPath $extract -Filter "javac.exe" -Recurse -File |
        Where-Object { $_.FullName -match "\\bin\\javac\.exe$" } |
        Select-Object -First 1

    if ($null -eq $javac) {
        throw "Downloaded JDK archive does not contain bin\javac.exe."
    }

    $jdkHome = Split-Path -Parent (Split-Path -Parent $javac.FullName)
    if ((Resolve-Path -LiteralPath $jdkHome).Path -eq (Resolve-Path -LiteralPath $extract).Path) {
        Move-Item -LiteralPath $extract -Destination $LocalJdk
    } else {
        Move-Item -LiteralPath $jdkHome -Destination $LocalJdk
        Remove-Item -LiteralPath $extract -Recurse -Force
    }

    Remove-Item -LiteralPath $zip -Force -ErrorAction SilentlyContinue

    $localJavac = Join-Path $LocalJdk "bin\javac.exe"
    if (-not (Test-Jdk $localJavac)) {
        throw "Downloaded JDK could not be validated."
    }

    Write-Step "Local JDK 17 is ready."
    return $localJavac
}

function Compile-SealStudio([string]$JavacPath) {
    Write-Step "Compiling Java sources..."

    if (Test-Path -LiteralPath $OutDir) {
        Remove-Item -LiteralPath $OutDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

    $sources = @(
        Get-ChildItem -LiteralPath (Join-Path $Root "src") -Filter "*.java" -Recurse -File |
        Sort-Object FullName |
        ForEach-Object { $_.FullName }
    )

    if ($sources.Count -eq 0) {
        throw "No Java source files were found."
    }

    & $JavacPath "-encoding" "UTF-8" "-d" $OutDir @sources
    if ($LASTEXITCODE -ne 0) {
        throw "javac returned exit code $LASTEXITCODE."
    }

    Write-Step "Compilation succeeded."
}

try {
    if ($env:OS -ne "Windows_NT") {
        throw "This launcher currently supports Windows only."
    }

    $javac = $null
    if (-not $ForceLocalJdk) {
        $javac = Find-UsableJavac
    }

    if ($null -eq $javac) {
        $javac = Download-LocalJdk
    } else {
        Write-Step "Using JDK $((Get-JavacMajor $javac)) at: $javac"
    }

    Compile-SealStudio $javac

    if ($CompileOnly) {
        Write-Step "Compile-only check completed."
        exit 0
    }

    $java = Join-Path (Split-Path -Parent $javac) "java.exe"
    if (-not (Test-Path -LiteralPath $java -PathType Leaf)) {
        throw "java.exe was not found next to javac.exe."
    }

    Write-Step "Starting SealStudio..."
    $arguments = '-cp "{0}" {1}' -f $OutDir, $MainClass
    Start-Process -FilePath $java -ArgumentList $arguments -WorkingDirectory $Root
    exit 0
} catch {
    Write-Host ""
    Write-Host "[SealStudio] ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "[SealStudio] Please keep this window open and copy the error if you need help."
    exit 1
}
