# =======================================================
# Script Compile JRXML -> JASPER untuk SIMRS Khanza
# Cara pakai:
#   Compile satu file  : .\compile-jrxml.ps1 report\rptLembarTriaseSkala3.jrxml
#   Compile semua      : .\compile-jrxml.ps1
# =======================================================
param([string]$InputFile = "")

$JDK11    = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\java.exe"
$JAVAC11  = "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\javac.exe"
$LIB      = "C:\Users\Damar\Lib"
$DISTLIB  = "$PSScriptRoot\dist\lib"
$REPORTDIR= "$PSScriptRoot\report"
$TMPDIR   = $env:TEMP

$JARS = (Get-ChildItem $LIB -Filter "*.jar" | ForEach-Object { $_.FullName })
$CP = ($JARS -join ";")

# Build helper jika belum ada
if (-not (Test-Path "$TMPDIR\CompileJrxml.class")) {
    Write-Host "Building compile helper..." -ForegroundColor Gray
    $src = @"
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import java.io.FileInputStream;
import javax.xml.parsers.SAXParserFactory;
public class CompileJrxml {
    public static void main(String[] args) throws Exception {
        System.setProperty("jasper.reports.compile.xml.validation","false");
        JasperDesign design = JRXmlLoader.load(new FileInputStream(args[0]));
        JasperReport report = JasperCompileManager.compileReport(design);
        JRSaver.saveObject(report, args[1]);
        System.out.println("OK: " + args[1]);
    }
}
"@
    [System.IO.File]::WriteAllText("$TMPDIR\CompileJrxml.java", $src, (New-Object System.Text.UTF8Encoding $false))
    & "$JAVAC11" -cp "$CP" -d "$TMPDIR" "$TMPDIR\CompileJrxml.java" 2>&1 | Where-Object { $_ -match "error" }
}

function Compile-One {
    param([string]$jrxmlPath)
    $jasperPath = [System.IO.Path]::ChangeExtension($jrxmlPath, ".jasper")
    $name = Split-Path $jrxmlPath -Leaf
    Write-Host -NoNewline "  Compiling $name ... "
    if (Test-Path $jasperPath) { Remove-Item $jasperPath -Force }

    $p = Start-Process "$JDK11" `
         -ArgumentList "-cp `"$TMPDIR;$CP`" CompileJrxml `"$jrxmlPath`" `"$jasperPath`"" `
         -Wait -PassThru -NoNewWindow `
         -RedirectStandardOutput "$TMPDIR\jrxml_out.txt" `
         -RedirectStandardError  "$TMPDIR\jrxml_err.txt"

    if (Test-Path $jasperPath) {
        $sz = (Get-Item $jasperPath).Length
        Write-Host "OK ($sz bytes)" -ForegroundColor Green
        # Sinkron ke build/report dan dist/report
        $buildReport = "$PSScriptRoot\build\report"
        $distReport  = "$PSScriptRoot\dist\report"
        if (Test-Path $buildReport) { Copy-Item $jasperPath "$buildReport\$(Split-Path $jasperPath -Leaf)" -Force }
        if (Test-Path $distReport)  { Copy-Item $jasperPath "$distReport\$(Split-Path $jasperPath -Leaf)"  -Force }
    } else {
        Write-Host "GAGAL" -ForegroundColor Red
        Get-Content "$TMPDIR\jrxml_err.txt" | Where-Object { $_ -notmatch "^log4j" } | Select-Object -First 5 |
            ForEach-Object { Write-Host "    $_" -ForegroundColor Yellow }
    }
}

if ($InputFile -ne "") {
    $full = if ([System.IO.Path]::IsPathRooted($InputFile)) { $InputFile } else { "$PSScriptRoot\$InputFile" }
    if (Test-Path $full) { Compile-One $full } else { Write-Host "File tidak ditemukan: $full" -ForegroundColor Red }
} else {
    $files = Get-ChildItem $REPORTDIR -Filter "*.jrxml"
    Write-Host "Compiling $($files.Count) file JRXML..."
    foreach ($f in $files) { Compile-One $f.FullName }
    Write-Host "`nSelesai." -ForegroundColor Cyan
}
