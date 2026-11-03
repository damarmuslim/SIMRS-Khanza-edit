# =======================================================
# Script Update JAR setelah compile Java
# Cara pakai: .\update-jar.ps1
# PASTIKAN APLIKASI SUDAH DITUTUP sebelum menjalankan
# =======================================================

$base    = $PSScriptRoot
$javac   = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\javac.exe"
$jarExe  = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\jar.exe"
$jarFile = "$base\dist\SIMRS_Khanza_Edit.jar"
$jarTemp = "$base\dist\SIMRS_Khanza_Edit.tmp.jar"
$outDir  = "$base\build\classes"

$allJars = (Get-ChildItem "C:\Users\Damar\Lib" -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"
$cp = "$allJars;$outDir"

# Compile RMTriaseIGD.java
Write-Host "Compiling RMTriaseIGD.java..." -ForegroundColor Cyan
$result = & "$javac" -encoding UTF-8 -cp "$cp" -d "$outDir" "$base\src\rekammedis\RMTriaseIGD.java" 2>&1
$errors = $result | Where-Object { $_ -match "error:" }
if ($errors) {
    Write-Host "COMPILE ERROR:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "  $_" }
    exit 1
}
Write-Host "Compile OK" -ForegroundColor Green

# Cek apakah JAR sedang dipakai
Write-Host "Updating JAR..." -ForegroundColor Cyan
$rmClasses = Get-ChildItem "$outDir\rekammedis" -Filter "RMTriaseIGD*.class" | ForEach-Object { "rekammedis/$($_.Name)" }
$wgClasses = Get-ChildItem "$outDir\widget"     -Filter "*.class"            | ForEach-Object { "widget/$($_.Name)" }
$allClasses = $rmClasses + $wgClasses

Push-Location $outDir

# Coba jar uf dulu
$ufResult = & "$jarExe" uf "$jarFile" @allClasses 2>&1
$ufError  = $ufResult | Where-Object { $_ -match "IOException|Error in writing" }

if ($ufError) {
    Write-Host "jar uf gagal (JAR mungkin sedang dipakai) - coba salin dulu..." -ForegroundColor Yellow
    # Salin ke temp, update temp, ganti
    Copy-Item $jarFile $jarTemp -Force
    & "$jarExe" uf "$jarTemp" @allClasses 2>&1 | Out-Null
    if(Test-Path $jarTemp){
        Move-Item $jarTemp $jarFile -Force -ErrorAction SilentlyContinue
        if(!(Test-Path $jarFile)){ Copy-Item $jarTemp $jarFile -Force }
    }
}

Pop-Location

$ts = (Get-Item $jarFile).LastWriteTime
Write-Host "JAR: $ts  |  RMTriaseIGD: $($rmClasses.Count) classes  |  widget: $($wgClasses.Count) classes" -ForegroundColor Green
Write-Host ""
Write-Host "Jangan lupa salin report jika ada perubahan JRXML:" -ForegroundColor Gray
Write-Host "  .\compile-jrxml.ps1 report\rptLembarTriaseSkala3.jrxml" -ForegroundColor Gray
