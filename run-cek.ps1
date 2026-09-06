$base = "d:\Damar\SIMRS\SIMRS-Khanza-edit"
$javac = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\javac.exe"
$java = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\java.exe"
$jarFile = "$base\dist\SIMRS_Khanza_Edit.jar"
$tmpDir = "$base\tmp-cek"

$cp = "$jarFile;$tmpDir"

Write-Host "Compiling test scripts with JAR classpath..." -ForegroundColor Cyan
& $javac -encoding UTF-8 -cp $cp -d $tmpDir "$tmpDir\PotretHalaman8sd11.java" "$tmpDir\PotongGambar.java"
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Taking screenshots from project root..." -ForegroundColor Cyan
& $java -cp $cp PotretHalaman8sd11
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Running CekNotNull..." -ForegroundColor Cyan
& $javac -encoding UTF-8 -cp $cp -d $tmpDir "$tmpDir\CekNotNull.java"
& $java -cp $cp CekNotNull

Write-Host "Running CekInsert..." -ForegroundColor Cyan
& $javac -encoding UTF-8 -cp $cp -d $tmpDir "$tmpDir\CekInsert.java"
& $java -cp $cp CekInsert

Write-Host "Selesai!" -ForegroundColor Green
