$base = "d:\Damar\SIMRS\SIMRS-Khanza-edit"
$javac = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\javac.exe"
$jarExe = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\jar.exe"
$jarFile = "$base\dist\SIMRS_Khanza_Edit.jar"
$jarTemp = "$base\dist\SIMRS_Khanza_Edit.tmp.jar"
$outDir = "$base\build\classes"

Write-Host "Mengumpulkan library dari C:\Users\Damar\Lib..." -ForegroundColor Cyan
$allJars = (Get-ChildItem "C:\Users\Damar\Lib" -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"
$cp = "$allJars;$outDir"

Write-Host "Compiling PanelTopBar, PanelLandingHospital, frmUtama, and DlgSetAplikasi..." -ForegroundColor Cyan
& $javac -encoding UTF-8 -cp $cp -d $outDir "$base\src\simrskhanza\PanelTopBar.java" "$base\src\simrskhanza\PanelLandingHospital.java" "$base\src\simrskhanza\frmUtama.java" "$base\src\setting\DlgSetAplikasi.java"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compile GAGAL!" -ForegroundColor Red
    exit 1
}

Write-Host "Copying icons to build..." -ForegroundColor Cyan
Copy-Item "$base\src\picture\icons\*" "$outDir\picture\icons\" -Recurse -Force

Write-Host "Compile OK! Updating JAR..." -ForegroundColor Green
& $jarExe uf $jarFile -C $outDir simrskhanza
& $jarExe uf $jarFile -C $outDir setting
& $jarExe uf $jarFile -C $outDir picture
if ($LASTEXITCODE -ne 0) {
    Write-Host "jar uf langsung gagal, mencoba lewat file temp..." -ForegroundColor Yellow
    Copy-Item $jarFile $jarTemp -Force
    & $jarExe uf $jarTemp -C $outDir simrskhanza
    & $jarExe uf $jarTemp -C $outDir setting
    if ($LASTEXITCODE -eq 0) {
        Copy-Item $jarTemp $jarFile -Force
        Remove-Item $jarTemp -Force -ErrorAction SilentlyContinue
        Write-Host "Berhasil update via temp file!" -ForegroundColor Green
    } else {
        Write-Host "Gagal update jar temp!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Berhasil update JAR langsung!" -ForegroundColor Green
}

$ts = (Get-Item $jarFile).LastWriteTime
Write-Host "Waktu file JAR: $ts" -ForegroundColor Green
