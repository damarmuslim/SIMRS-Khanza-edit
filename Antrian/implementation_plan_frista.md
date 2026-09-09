# Activate FRISTA Calling Feature

Create the queue-taking page for the FRISTA service and update the anjungan link.

## Proposed Changes

### [NEW] [antrian_frista.php](file:///c:/xampp/htdocs/MediFix/antrian_frista.php)
Create a new file based on [antrian_admisi.php](file:///c:/xampp/htdocs/MediFix/antrian_admisi.php) tailored for the FRISTA service.
- **Service Type**: `FRISTA`
- **Prefix**: `S` (Special/Service) or `FR`. I will use `S` to match the "Service" nature of Frista.
- **Theme**: Gradient from `#fa709a` to `#fee140` to match the icon in settings.

### [MODIFY] [anjungan.php](file:///c:/xampp/htdocs/MediFix/anjungan.php)
Update the FRISTA menu link to point to `antrian_frista.php` instead of the non-existent `panggil_frista.php`.

```php
<a 
    href="<?= ($fiturAktif['frista'] ?? false) ? 'antrian_frista.php' : 'javascript:void(0)' ?>" 
    class="card-menu <?= ($fiturAktif['frista'] ?? false) ? '' : 'opacity-50' ?>" 
...
```

## Verification Plan

### Manual Verification
1. Open [anjungan.php](file:///c:/xampp/htdocs/MediFix/anjungan.php).
2. Ensure FRISTA is active in [setting_fitur.php](file:///c:/xampp/htdocs/MediFix/setting_fitur.php).
3. Click the **FRISTA** button.
4. Verify that the **antrian_frista.php** page opens and allows taking a ticket.
5. Verify that the ticket is saved to the `antrian_wira` table in the `sik` database with `jenis = 'FRISTA'`.
