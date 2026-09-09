<?php
include 'koneksi.php';
$stmt = $pdo->query("SELECT * FROM setting_simrs LIMIT 1");
$setting = $stmt->fetch();
?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiosk Mode - <?= htmlspecialchars($setting['nama_instansi']) ?></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&family=Outfit:wght@700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-gradient: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
            --glass-bg: rgba(255, 255, 255, 0.1);
            --glass-border: rgba(255, 255, 255, 0.2);
        }

        body, html {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            overflow: hidden;
            background: #0f172a;
            font-family: 'Inter', sans-serif;
        }

        #kioskWrapper {
            width: 100%;
            height: 100%;
            position: relative;
        }

        #kioskIframe {
            width: 100%;
            height: 100%;
            border: none;
            display: none;
            background: #fff;
        }

        /* Return to Fullscreen Button (Hidden by default) */
        #returnFsBtn {
            position: fixed;
            bottom: 30px;
            right: 30px;
            background: var(--primary-gradient);
            color: white;
            border: none;
            padding: 15px 25px;
            border-radius: 15px;
            z-index: 10000;
            display: none;
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
            font-weight: 700;
            cursor: pointer;
            animation: fadeIn 0.5s;
        }

        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

        /* Start Overlay */
        #startOverlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: radial-gradient(circle at top right, #1e1b4b, #0f172a);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .start-card {
            background: var(--glass-bg);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border: 1px solid var(--glass-border);
            border-radius: 40px;
            padding: 60px;
            text-align: center;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
            max-width: 600px;
            width: 90%;
            animation: cardEntrance 1.2s ease-out;
        }

        @keyframes cardEntrance {
            from { opacity: 0; transform: translateY(40px) scale(0.95); }
            to { opacity: 1; transform: translateY(0) scale(1); }
        }

        .start-icon {
            font-size: 80px;
            background: var(--primary-gradient);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 20px;
            display: inline-block;
        }

        .hospital-name {
            font-family: 'Outfit', sans-serif;
            font-weight: 900;
            font-size: 2.5rem;
            color: #fff;
            margin-bottom: 10px;
            text-transform: uppercase;
            letter-spacing: -0.5px;
        }

        .kiosk-title {
            color: #94a3b8;
            font-size: 1.2rem;
            margin-bottom: 40px;
            font-weight: 600;
        }

        .btn-start {
            background: var(--primary-gradient);
            color: white;
            border: none;
            padding: 20px 60px;
            font-size: 1.5rem;
            font-weight: 800;
            border-radius: 20px;
            cursor: pointer;
            transition: all 0.3s;
            box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
            display: flex;
            align-items: center;
            gap: 15px;
            margin: 0 auto;
        }

        .btn-start:hover {
            transform: translateY(-5px) scale(1.05);
            box-shadow: 0 15px 40px rgba(99, 102, 241, 0.6);
            color: #fff;
        }

        .btn-start:active {
            transform: scale(0.98);
        }

        .status-badge {
            margin-top: 40px;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: rgba(16, 185, 129, 0.1);
            color: #10b981;
            padding: 8px 20px;
            border-radius: 100px;
            font-size: 0.9rem;
            font-weight: 700;
            border: 1px solid rgba(16, 185, 129, 0.2);
        }

        /* Particle effect */
        .particles {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: -1;
        }

        .particle {
            position: absolute;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 50%;
            animation: float 20s infinite linear;
        }

        @keyframes float {
            0% { transform: translateY(0) rotate(0deg); opacity: 0; }
            10% { opacity: 1; }
            90% { opacity: 1; }
            100% { transform: translateY(-1000px) rotate(720deg); opacity: 0; }
        }
    </style>
</head>
<body>

    <div id="startOverlay">
        <div class="particles" id="particles"></div>
        
        <div class="start-card">
            <div class="start-icon">
                <i class="bi bi-hospital"></i>
            </div>
            <h1 class="hospital-name"><?= htmlspecialchars($setting['nama_instansi']) ?></h1>
            <p class="kiosk-title">SELF-SERVICE KIOSK SYSTEM</p>
            
            <button class="btn-start" id="startBtn">
                <i class="bi bi-play-circle-fill"></i>
                MULAI KIOSK
            </button>
            
            <div class="status-badge">
                <i class="bi bi-check-circle-fill"></i>
                Ready to Initialize Fullscreen
            </div>
        </div>
    </div>

    <div id="kioskWrapper">
        <iframe id="kioskIframe" src="anjungan.php" allow="fullscreen"></iframe>
    </div>

    <button id="returnFsBtn">
        <i class="bi bi-fullscreen"></i> Kembali ke Layar Penuh
    </button>

    <script>
        // Generate Particles
        const particlesContainer = document.getElementById('particles');
        for (let i = 0; i < 30; i++) {
            const particle = document.createElement('div');
            particle.className = 'particle';
            const size = Math.random() * 5 + 2;
            particle.style.width = size + 'px';
            particle.style.height = size + 'px';
            particle.style.left = Math.random() * 100 + '%';
            particle.style.top = Math.random() * 100 + 100 + '%';
            particle.style.animationDelay = Math.random() * 20 + 's';
            particle.style.animationDuration = Math.random() * 10 + 15 + 's';
            particlesContainer.appendChild(particle);
        }

        const returnFsBtn = document.getElementById('returnFsBtn');

        async function enterFullscreen() {
            try {
                if (document.documentElement.requestFullscreen) {
                    await document.documentElement.requestFullscreen();
                } else if (document.documentElement.webkitRequestFullscreen) {
                    await document.documentElement.webkitRequestFullscreen();
                }
                
                startOverlay.style.opacity = '0';
                startOverlay.style.transform = 'scale(1.1)';
                
                setTimeout(() => {
                    startOverlay.style.display = 'none';
                    kioskIframe.style.display = 'block';
                    returnFsBtn.style.display = 'none'; // Hide if we were showing it
                }, 800);

            } catch (err) {
                console.error("Error:", err);
            }
        }

        startBtn.addEventListener('click', enterFullscreen);
        returnFsBtn.addEventListener('click', enterFullscreen);

        // Prevention: If user exits fullscreen manually, show a small button
        document.addEventListener('fullscreenchange', () => {
            if (!document.fullscreenElement && startOverlay.style.display === 'none') {
                returnFsBtn.style.display = 'flex';
            } else {
                returnFsBtn.style.display = 'none';
            }
        });

        // Keep iframe focused for keyboard inputs
        kioskIframe.onload = () => {
            kioskIframe.contentWindow.focus();
        };
    </script>
</body>
</html>
