$base = "d:\Damar\SIMRS\SIMRS-Khanza-edit"
$javac = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\javac.exe"
$java = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\java.exe"
$jarFile = "$base\dist\SIMRS_Khanza_Edit.jar"
$tmpDir = "$base\tmp-cek"

$allJars = (Get-ChildItem "C:\Users\Damar\Lib" -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"
$cp = "$jarFile;$allJars;$tmpDir"

Write-Host "Compiling PotretLandingScreen.java..." -ForegroundColor Cyan
& $javac -encoding UTF-8 -cp $cp -d $tmpDir "$tmpDir\PotretLandingScreen.java"
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Running PotretLandingScreen..." -ForegroundColor Cyan
& $java -cp $cp PotretLandingScreen
