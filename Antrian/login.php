<?php
session_start();
include 'koneksi.php';
include 'koneksi2.php';

if (isset($_SESSION['user_id'])) {
    header('Location: dashboard.php');
    exit;
}

try {
    $stmt = $pdo_simrs->query("SELECT * FROM setting LIMIT 1");
    $rs = $stmt->fetch(PDO::FETCH_ASSOC);
    $namaRS      = $rs['nama_instansi'] ?? 'Nama Rumah Sakit';
    $alamatRS    = $rs['alamat_instansi'] ?? '';
    $kabupatenRS = $rs['kabupaten'] ?? '';
    $propinsiRS  = $rs['propinsi'] ?? '';
} catch (Exception $e) {
    $namaRS = 'Nama Rumah Sakit';
    $alamatRS = $kabupatenRS = $propinsiRS = '';
}

$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['login_submit'])) {
    $login    = trim($_POST['login']);
    $password = trim($_POST['password']);
    if ($login === '' || $password === '') {
        $error = "Email/NIK dan Password wajib diisi.";
    } else {
        $stmt = filter_var($login, FILTER_VALIDATE_EMAIL)
            ? $pdo->prepare("SELECT * FROM users WHERE email = ? LIMIT 1")
            : $pdo->prepare("SELECT * FROM users WHERE nik = ? LIMIT 1");
        $stmt->execute([$login]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($user && password_verify($password, $user['password'])) {
            $_SESSION['user_id'] = $user['id'];
            $_SESSION['nama']    = $user['nama'];
            $_SESSION['email']   = $user['email'];
            header("Location: dashboard.php");
            exit;
        } else {
            $error = "Email/NIK atau Password salah.";
        }
    }
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['register_submit'])) {
    $nik      = trim($_POST['nik']);
    $nama     = trim($_POST['nama']);
    $email    = trim($_POST['email']);
    $hp       = trim($_POST['hp']);
    $password = trim($_POST['password']);
    $confirm  = trim($_POST['confirm']);
    if (!$nik || !$nama || !$email || !$password || !$confirm) {
        $error = 'Semua field wajib diisi.';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = 'Format email tidak valid.';
    } elseif ($password !== $confirm) {
        $error = 'Konfirmasi password tidak sama.';
    } else {
        $cek = $pdo->prepare("SELECT COUNT(*) FROM users WHERE nik = ? OR email = ?");
        $cek->execute([$nik, $email]);
        if ($cek->fetchColumn() > 0) {
            $error = 'NIK atau Email sudah terdaftar.';
        } else {
            $hash = password_hash($password, PASSWORD_DEFAULT);
            $pdo->prepare("INSERT INTO users (nik, nama, email, hp, password) VALUES (?, ?, ?, ?, ?)")
                ->execute([$nik, $nama, $email, $hp, $hash]);
            $success = 'Pendaftaran berhasil! Silakan login.';
        }
    }
}
?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - <?= htmlspecialchars($namaRS) ?></title>
    
    <!-- Load Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- Load FontAwesome for Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Load Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Plus Jakarta Sans', 'sans-serif'],
                    },
                    colors: {
                        brand: {
                            blue: '#2b7fc1',
                            blueDark: '#1a5f96',
                            bg: '#1e324a'
                        }
                    }
                }
            }
        }
    </script>
    <style>
        body {
            background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 60%, #163352 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0;
            padding: 1rem;
        }
        
        /* Animasi masuk */
        .card-animate {
            animation: cardIn 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
        }
        @keyframes cardIn {
            from { opacity: 0; transform: translateY(20px) scale(0.98); }
            to { opacity: 1; transform: translateY(0) scale(1); }
        }
    </style>
</head>
<body>

    <!-- Main Card Container -->
    <div class="card-animate bg-white rounded-3xl shadow-[0_24px_64px_rgba(0,0,0,0.45)] flex flex-col md:flex-row w-full max-w-5xl overflow-hidden relative z-10">
        
        <!-- Panel Kiri - Branding Rumah Sakit -->
        <div class="bg-gradient-to-br from-brand-blue to-brand-blueDark text-white p-8 md:p-12 w-full md:w-5/12 flex flex-col justify-center relative overflow-hidden">
            <!-- Decorative circles -->
            <div class="absolute -top-16 -right-16 w-64 h-64 bg-white opacity-5 rounded-full"></div>
            <div class="absolute -bottom-16 -left-16 w-48 h-48 bg-green-400 opacity-10 rounded-full"></div>
            
            <div class="relative z-10">
                <div class="flex items-center mb-5">
                    <!-- Logo RS -->
                    <div class="bg-white/20 p-2 rounded-2xl shadow-sm border border-white/30 inline-flex items-center justify-center w-[70px] h-[70px]">
                        <img src="image/logo.png" alt="Logo RS" class="max-w-full max-h-full object-contain">
                    </div>
                </div>
                
                <h1 class="text-3xl font-extrabold leading-tight mb-2">RSUD<span class="block font-normal text-2xl mt-1 opacity-90">Demang Sepulau Raya</span></h1>
                <p class="text-blue-100 text-sm font-medium mb-8 opacity-80">Anjungan Pasien Mandiri & Sistem Antrian</p>
                
                <!-- Info Box dari Database -->
                <div class="bg-white/10 backdrop-blur-sm rounded-xl p-4 mb-8 border border-white/20">
                    <ul class="space-y-3 text-sm">
                        <li class="flex items-start">
                            <i class="fas fa-hospital mt-1 mr-3 text-blue-200 w-4 text-center"></i>
                            <span class="font-bold"><?= htmlspecialchars($namaRS) ?></span>
                        </li>
                        <?php if (!empty($alamatRS)): ?>
                        <li class="flex items-start">
                            <i class="fas fa-map-marker-alt mt-1 mr-3 text-blue-200 w-4 text-center"></i>
                            <span class="opacity-90"><?= htmlspecialchars($alamatRS) ?></span>
                        </li>
                        <?php endif; ?>
                        <?php if (!empty($kabupatenRS) || !empty($propinsiRS)): ?>
                        <li class="flex items-start">
                            <i class="fas fa-map mt-1 mr-3 text-blue-200 w-4 text-center"></i>
                            <span class="opacity-90"><?= htmlspecialchars($kabupatenRS) ?><?= !empty($propinsiRS) ? ' — ' . htmlspecialchars($propinsiRS) : '' ?></span>
                        </li>
                        <?php endif; ?>
                    </ul>
                </div>
                
                <!-- Features Grid -->
                <div class="hidden md:grid grid-cols-2 gap-3 text-xs font-semibold">
                    <div class="bg-white/5 rounded-lg p-3 flex items-center border border-white/10">
                        <i class="far fa-clock mr-2 text-sky-300 text-base"></i> Antrian Real-time
                    </div>
                    <div class="bg-white/5 rounded-lg p-3 flex items-center border border-white/10">
                        <i class="fas fa-mobile-alt mr-2 text-sky-300 text-base"></i> Akses Mudah
                    </div>
                    <div class="bg-white/5 rounded-lg p-3 flex items-center border border-white/10">
                        <i class="fas fa-shield-alt mr-2 text-sky-300 text-base"></i> Data Aman
                    </div>
                    <div class="bg-white/5 rounded-lg p-3 flex items-center border border-white/10">
                        <i class="far fa-check-circle mr-2 text-sky-300 text-base"></i> Mudah Digunakan
                    </div>
                </div>
            </div>
        </div>

        <!-- Panel Kanan - Form Area -->
        <div class="p-8 md:p-12 w-full md:w-7/12 flex flex-col justify-center bg-[#fafbfc] relative max-h-[95vh] overflow-y-auto custom-scrollbar">
            
            <div class="max-w-md mx-auto w-full">
                
                <!-- Alert Messages -->
                <?php if ($error): ?>
                <div class="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 rounded-md mb-6 flex justify-between items-center shadow-sm">
                    <div class="flex items-center">
                        <i class="fas fa-exclamation-triangle mr-3"></i>
                        <p class="text-sm font-semibold"><?= htmlspecialchars($error) ?></p>
                    </div>
                    <button onclick="this.parentElement.remove()" class="text-red-500 hover:text-red-700">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <?php endif; ?>

                <?php if ($success): ?>
                <div class="bg-green-50 border-l-4 border-green-500 text-green-800 p-4 rounded-md mb-6 flex justify-between items-center shadow-sm">
                    <div class="flex items-center">
                        <i class="fas fa-check-circle mr-3"></i>
                        <p class="text-sm font-semibold"><?= htmlspecialchars($success) ?></p>
                    </div>
                    <button onclick="this.parentElement.remove()" class="text-green-500 hover:text-green-700">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <?php endif; ?>

                <!-- FORM LOGIN -->
                <div id="section-login" class="<?= isset($_POST['register_submit']) ? 'hidden' : 'block' ?>">
                    <div class="mb-8">
                        <h2 class="text-2xl font-extrabold text-gray-800 mb-2">Selamat Datang <span class="inline-block animate-bounce">👋</span></h2>
                        <p class="text-slate-500 text-sm font-medium">Masuk untuk melanjutkan ke dashboard</p>
                    </div>

                    <form action="" method="POST" class="space-y-5">
                        <!-- Input Email/NIK -->
                        <div>
                            <label class="block text-sm font-bold text-slate-600 mb-1.5">Email atau NIK</label>
                            <div class="relative">
                                <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                                    <i class="fas fa-user text-slate-400"></i>
                                </div>
                                <input type="text" name="login" class="block w-full pl-10 pr-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-blue focus:border-brand-blue bg-white text-slate-800 placeholder-slate-400 transition-shadow text-sm" placeholder="Masukkan email atau NIK" required>
                            </div>
                        </div>

                        <!-- Input Password -->
                        <div>
                            <label class="block text-sm font-bold text-slate-600 mb-1.5">Password</label>
                            <div class="relative">
                                <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                                    <i class="fas fa-lock text-slate-400"></i>
                                </div>
                                <input type="password" name="password" class="block w-full pl-10 pr-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-blue focus:border-brand-blue bg-white text-slate-800 placeholder-slate-400 transition-shadow text-sm" placeholder="Masukkan password" required>
                            </div>
                        </div>

                        <!-- Submit Button -->
                        <button type="submit" name="login_submit" class="w-full flex justify-center items-center py-2.5 px-4 border border-transparent rounded-lg shadow-md text-sm font-bold text-white bg-brand-blue hover:bg-brand-blueDark hover:-translate-y-0.5 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-blue transition-all mt-6">
                            <i class="fas fa-sign-in-alt mr-2"></i> Masuk Sekarang
                        </button>
                    </form>

                    <div class="mt-8 pt-6 border-t border-slate-200 text-center text-sm font-medium text-slate-500">
                        <p>Belum punya akun? <a href="#" onclick="toggleForm('register')" class="text-brand-blue hover:text-brand-blueDark font-bold hover:underline transition">Daftar di sini</a></p>
                    </div>
                </div>

                <!-- FORM REGISTER -->
                <div id="section-register" class="<?= isset($_POST['register_submit']) ? 'block' : 'hidden' ?>">
                    <div class="mb-6">
                        <h2 class="text-2xl font-extrabold text-gray-800 mb-2">Daftar Akun Baru</h2>
                        <p class="text-slate-500 text-sm font-medium">Lengkapi data berikut untuk membuat akun</p>
                    </div>

                    <form action="" method="POST" class="space-y-4">
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label class="block text-xs font-bold text-slate-600 mb-1.5">NIK</label>
                                <div class="relative">
                                    <i class="fas fa-id-card absolute left-3 top-3 text-slate-400 text-xs"></i>
                                    <input type="text" name="nik" class="block w-full pl-8 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-blue text-sm" placeholder="NIK" required>
                                </div>
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-slate-600 mb-1.5">Nama Lengkap</label>
                                <div class="relative">
                                    <i class="fas fa-user absolute left-3 top-3 text-slate-400 text-xs"></i>
                                    <input type="text" name="nama" class="block w-full pl-8 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-blue text-sm" placeholder="Nama lengkap" required>
                                </div>
                            </div>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label class="block text-xs font-bold text-slate-600 mb-1.5">Email</label>
                                <div class="relative">
                                    <i class="fas fa-envelope absolute left-3 top-3 text-slate-400 text-xs"></i>
                                    <input type="email" name="email" class="block w-full pl-8 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-blue text-sm" placeholder="email@contoh.com" required>
                                </div>
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-slate-600 mb-1.5">Nomor HP</label>
                                <div class="relative">
                                    <i class="fas fa-phone-alt absolute left-3 top-3 text-slate-400 text-xs"></i>
                                    <input type="text" name="hp" class="block w-full pl-8 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-blue text-sm" placeholder="08xxxxxxxxxx">
                                </div>
                            </div>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label class="block text-xs font-bold text-slate-600 mb-1.5">Password</label>
                                <div class="relative">
                                    <i class="fas fa-lock absolute left-3 top-3 text-slate-400 text-xs"></i>
                                    <input type="password" name="password" class="block w-full pl-8 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-blue text-sm" placeholder="Buat password" required>
                                </div>
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-slate-600 mb-1.5">Konfirmasi Password</label>
                                <div class="relative">
                                    <i class="fas fa-lock absolute left-3 top-3 text-slate-400 text-xs"></i>
                                    <input type="password" name="confirm" class="block w-full pl-8 pr-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-blue text-sm" placeholder="Ulangi password" required>
                                </div>
                            </div>
                        </div>

                        <button type="submit" name="register_submit" class="w-full flex justify-center items-center py-2.5 px-4 border border-transparent rounded-lg shadow-md text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 hover:-translate-y-0.5 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 transition-all mt-4">
                            <i class="fas fa-user-plus mr-2"></i> Daftar Sekarang
                        </button>
                    </form>

                    <div class="mt-6 pt-5 border-t border-slate-200 text-center text-sm font-medium text-slate-500">
                        <p>Sudah punya akun? <a href="#" onclick="toggleForm('login')" class="text-emerald-600 hover:text-emerald-700 font-bold hover:underline transition">Login di sini</a></p>
                    </div>
                </div>

            </div>
        </div>
        
    </div>

    <!-- Script to toggle between forms -->
    <script>
        function toggleForm(formType) {
            const loginForm = document.getElementById('section-login');
            const registerForm = document.getElementById('section-register');
            
            if (formType === 'register') {
                loginForm.classList.add('hidden');
                loginForm.classList.remove('block');
                registerForm.classList.remove('hidden');
                registerForm.classList.add('block');
            } else {
                registerForm.classList.add('hidden');
                registerForm.classList.remove('block');
                loginForm.classList.remove('hidden');
                loginForm.classList.add('block');
            }
        }
    </script>
</body>
</html>