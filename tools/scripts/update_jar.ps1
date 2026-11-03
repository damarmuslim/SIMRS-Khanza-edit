$jar = if (Test-Path "C:\Program Files\BellSoft\LibericaJDK-11\bin\jar.exe") { "C:\Program Files\BellSoft\LibericaJDK-11\bin\jar.exe" } else { "C:\Program Files\BellSoft\LibericaJDK-8-Full\bin\jar.exe" }
$jarPath = (Resolve-Path "dist\SIMRS_Khanza_Edit.jar").Path

Write-Host "Updating dist\SIMRS_Khanza_Edit.jar from build\classes..."
Push-Location build\classes
& $jar uf $jarPath rekammedis\PanelPenunjangMedis*.class rekammedis\RMPenilaianAwalMedisIGD*.class rekammedis\RMPenilaianAwalKeperawatanIGD*.class rekammedis\RMTriaseIGD*.class rekammedis\DlgMasterTemplate*.class kepegawaian\DlgCariDokter*.class simrskhanza\DlgIGD*.class simrskhanza\frmUtama*.class widget\Button*.class widget\IconStyle*.class rekammedis\DlgEdukasiPasienWebView*.class rekammedis\RMEdukasiPasienRM021*.class simrskhanza\DlgEdukasiPasienWebView*.class rekammedis\RMRekonsiliasiObat*.class rekammedis\RMPenilaianAwalKeperawatanRanap*.class rekammedis\RMAsesmenMedisRanap20_1*.class rekammedis\RMTransferPasienAntarRuang*.class
Pop-Location
Copy-Item -Path "dist\SIMRS_Khanza_Edit.jar" -Destination "webapps\update\SIMRS_Khanza_Edit.jar" -Force
if (Test-Path "C:\xampp\htdocs\webapps\update") {
    Copy-Item -Path "dist\SIMRS_Khanza_Edit.jar" -Destination "C:\xampp\htdocs\webapps\update\SIMRS_Khanza_Edit.jar" -Force
    Copy-Item -Path "webapps\update\version.json" -Destination "C:\xampp\htdocs\webapps\update\version.json" -Force
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "Copying report files to dist/report and build/report..."
    @("rptCetakPenilaianAwalKeperawatanIGD", "rptCetakPenilaianAwalMedisIGD", "rptLembarTriasePacs", "rptRM19", "rptCetakTransferPasienAntarRuang") | ForEach-Object {
        Copy-Item -Path "report\$_.jrxml" -Destination "dist\report\" -Force -ErrorAction SilentlyContinue
        Copy-Item -Path "report\$_.jasper" -Destination "dist\report\" -Force -ErrorAction SilentlyContinue
        Copy-Item -Path "report\$_.jrxml" -Destination "build\report\" -Force -ErrorAction SilentlyContinue
        Copy-Item -Path "report\$_.jasper" -Destination "build\report\" -Force -ErrorAction SilentlyContinue
    }
    Write-Host "JAR UPDATE AND REPORT SYNC SUCCESSFUL!"
}
else {
    Write-Host "JAR update failed (aplikasi mungkin sedang berjalan). Tutup aplikasi SIMRS terlebih dahulu lalu jalankan kembali script ini."
}
