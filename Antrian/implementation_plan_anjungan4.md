# Add Missing Features to Anjungan

Reflect all features from [setting_fitur.php](file:///c:/xampp/htdocs/MediFix/setting_fitur.php) in the [anjungan.php](file:///c:/xampp/htdocs/MediFix/anjungan.php) display.

## Proposed Changes

### [MODIFY] [anjungan.php](file:///c:/xampp/htdocs/MediFix/anjungan.php)

#### Add `frista` Menu Item
Insert the `frista` menu item into the `.menu-grid`.
```html
<!-- FRISTA -->
<a 
    href="<?= ($fiturAktif['frista'] ?? false) ? 'panggil_frista.php' : 'javascript:void(0)' ?>" 
    class="card-menu <?= ($fiturAktif['frista'] ?? false) ? '' : 'opacity-50' ?>" 
    <?= ($fiturAktif['frista'] ?? false) ? '' : 'data-bs-toggle="modal" data-bs-target="#notAvailableModal"' ?>
>
    <div class="icon-wrapper" style="background: linear-gradient(135deg, #fa709a, #fee140);">
        <i class="bi bi-heart-fill"></i>
    </div>
    <div class="menu-label">FRISTA</div>
    <div class="menu-subtitle">Layanan pendukung</div>
</a>
```

#### Fix `cari_ranap` Display Logic
Update the existing `cari_ranap` link to correctly handle the missing key in `$fiturAktif` (using `?? false`).

#### Code Cleanup
Remove the duplicate `visitorFormModal` definition (lines 876-910) and the duplicate `nextToForm` event listener (lines 937-946).

## Verification Plan

### Manual Verification
1. Open [anjungan.php](file:///c:/xampp/htdocs/MediFix/anjungan.php).
2. Verify that the **FRISTA** menu item appears.
3. Verify that **FRISTA** and **CARI PASIEN R. INAP** respond correctly to their status in [setting_fitur.php](file:///c:/xampp/htdocs/MediFix/setting_fitur.php) (active vs. inactive).
4. Verify that the virtual keyboard and visitor form still function correctly after the cleanup.
