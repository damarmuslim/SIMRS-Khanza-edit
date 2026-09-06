$base = "d:\Damar\SIMRS\SIMRS-Khanza-edit"
$javac = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\javac.exe"
$jarExe = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\jar.exe"
$jarFile = "$base\dist\SIMRS_Khanza_Edit.jar"
$jarTemp = "$base\dist\SIMRS_Khanza_Edit.tmp.jar"
$outDir = "$base\build\classes"

Write-Host "Mengumpulkan library dari C:\Users\Damar\Lib..." -ForegroundColor Cyan
$allJars = (Get-ChildItem "C:\Users\Damar\Lib" -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"
$cp = "$allJars;$outDir"

Write-Host "Compiling RMPenilaianAwalKeperawatanIGD.java..." -ForegroundColor Cyan
$srcFile = "$base\src\rekammedis\RMPenilaianAwalKeperawatanIGD.java"
& $javac -encoding UTF-8 -cp $cp -d $outDir $srcFile
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compile GAGAL!" -ForegroundColor Red
    exit 1
}
Write-Host "Compile OK!" -ForegroundColor Green

Write-Host "Updating JAR dist/SIMRS_Khanza_Edit.jar..." -ForegroundColor Cyan

# Coba langsung update folder rekammedis ke dalam jar
& $jarExe uf $jarFile -C $outDir rekammedis
if ($LASTEXITCODE -ne 0) {
    Write-Host "jar uf langsung gagal, mencoba lewat file temp..." -ForegroundColor Yellow
    Copy-Item $jarFile $jarTemp -Force
    & $jarExe uf $jarTemp -C $outDir rekammedis
    if ($LASTEXITCODE -eq 0) {
        Move-Item $jarTemp $jarFile -Force
        Write-Host "Berhasil update via temp file!" -ForegroundColor Green
    } else {
        Write-Host "Gagal update jar temp!" -ForegroundColor Red
    }
} else {
    Write-Host "Berhasil update JAR langsung!" -ForegroundColor Green
}

$ts = (Get-Item $jarFile).LastWriteTime
Write-Host "Waktu file JAR: $ts" -ForegroundColor Green
