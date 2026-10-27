package simrskhanza;

import fungsi.AsistenAIBoy;
import fungsi.akses;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Gelembung Chat Melayang (Floating Action AI Bubble) resmi Asisten AI Boy.
 * Melayang di pojok kanan bawah khusus saat aplikasi SIMRS Khanza sedang aktif.
 * Otomatis sembunyi saat user beralih ke aplikasi luar (IDE, Chrome, dsb).
 * Bersifat independen (parent null) sehingga TIDAK menutup atau mengganggu Z-order form lain.
 */
public class GelembungChatAIBoy extends JWindow {

    private static GelembungChatAIBoy instance;
    private static final int DIAMETER = 60;
    private static final int WINDOW_SIZE = DIAMETER + 16; // Margin untuk bayangan halus (drop shadow)

    private Point dragOffset;
    private boolean isHovered = false;
    private boolean isDragging = false;
    private boolean isAktifDiSimrs = false;
    private boolean wasChatOpenBeforeSwitch = false;
    private final JPopupMenu popupMenu = new JPopupMenu();

    public static synchronized GelembungChatAIBoy getInstance() {
        if (instance == null) {
            instance = new GelembungChatAIBoy();
        }
        return instance;
    }

    private GelembungChatAIBoy() {
        super((java.awt.Frame) null); // Independen agar tidak memicu windowActivated pada frmUtama
        initWindow();
        initPopupMenu();
        initKomponen();
        posisikanDefault();
        initFocusTracking();
    }

    private void initWindow() {
        setAlwaysOnTop(true);
        setSize(WINDOW_SIZE, WINDOW_SIZE);
        try {
            setBackground(new Color(0, 0, 0, 0)); // Transparan per-pixel
        } catch (Exception ignored) {
            setBackground(new Color(240, 240, 240));
        }
    }

    /**
     * Pelacak fokus aplikasi SIMRS Khanza.
     * Gelembung hanya muncul saat SIMRS Khanza sedang digunakan.
     * Jika pengguna berpindah ke aplikasi lain (seperti Antigravity IDE, Browser, Word),
     * gelembung dan jendela chat otomatis bersembunyi sehingga tidak mengganggu layar luar!
     */
    private void initFocusTracking() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Window active = (Window) evt.getNewValue();
                if (active != null) {
                    // Pengguna sedang aktif di dalam salah satu jendela SIMRS Khanza
                    if (isAktifDiSimrs && AsistenAIBoy.getInstance().isAktif()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setVisible(true);
                                toFront();
                                if (wasChatOpenBeforeSwitch) {
                                    DlgAsistenAIBoy dlg = DlgAsistenAIBoy.getInstance(null);
                                    dlg.setVisible(true);
                                    dlg.toFront();
                                    wasChatOpenBeforeSwitch = false;
                                }
                            }
                        });
                    }
                } else {
                    // Pengguna sedang beralih ke aplikasi di luar SIMRS Khanza (misal IDE, Chrome, dll)
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setVisible(false);
                            DlgAsistenAIBoy dlg = DlgAsistenAIBoy.getInstance(null);
                            if (dlg.isVisible()) {
                                wasChatOpenBeforeSwitch = true;
                                dlg.setVisible(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void initPopupMenu() {
        JMenuItem mnBuka = new JMenuItem("🤖 Buka Chat Boy (AI)");
        mnBuka.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mnBuka.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaChat();
            }
        });
        popupMenu.add(mnBuka);

        JMenuItem mnSetting = new JMenuItem("⚙ Pengaturan AI");
        mnSetting.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mnSetting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!akses.getadmin()) {
                    JOptionPane.showMessageDialog(GelembungChatAIBoy.this,
                        "Pengaturan Asisten AI hanya dapat diakses oleh Administrator Sistem.",
                        "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                setting.DlgSetAI dlg = new setting.DlgSetAI(null, true);
                dlg.setVisible(true);
            }
        });
        popupMenu.add(mnSetting);

        JMenuItem mnReset = new JMenuItem("🔄 Reset Posisi (Pojok Kanan)");
        mnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                posisikanDefault();
            }
        });
        popupMenu.add(mnReset);

        popupMenu.addSeparator();

        JMenuItem mnTutup = new JMenuItem("✖ Sembunyikan Gelembung");
        mnTutup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mnTutup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sembunyikan();
            }
        });
        popupMenu.add(mnTutup);
    }

    private void initKomponen() {
        JPanel pnlBubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int offset = 8;
                int d = DIAMETER;

                // 1. Gambar Bayangan Halus (Drop Shadow)
                g2.setColor(new Color(15, 23, 42, isHovered ? 60 : 38));
                g2.fill(new Ellipse2D.Float(offset + 1, offset + 3, d, d));
                g2.setColor(new Color(15, 23, 42, isHovered ? 35 : 20));
                g2.fill(new Ellipse2D.Float(offset - 1, offset + 1, d + 2, d + 4));

                // 2. Lingkaran Dasar Gradien Medis Teal
                Color topBg = isHovered ? new Color(20, 184, 166) : new Color(13, 148, 136); // Teal-500 / Teal-600
                Color botBg = isHovered ? new Color(15, 118, 110) : new Color(17, 94, 89);   // Teal-700 / Teal-800
                java.awt.GradientPaint gp = new java.awt.GradientPaint(offset, offset, topBg, offset + d, offset + d, botBg);
                g2.setPaint(gp);
                g2.fill(new Ellipse2D.Float(offset, offset, d, d));

                // 3. Ring Halo Lingkaran Luar (Aura Glowing)
                g2.setColor(isHovered ? new Color(204, 251, 241, 220) : new Color(153, 246, 228, 140));
                g2.setStroke(new BasicStroke(isHovered ? 2.2f : 1.5f));
                g2.draw(new Ellipse2D.Float(offset + 1f, offset + 1f, d - 2f, d - 2f));

                // 4. Ikon Robot Cerdas Boy (Vektor Halus)
                int cx = offset + d / 2;
                int cy = offset + d / 2 - 2;

                // Kepala Robot (Rounded Rect)
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(cx - 14, cy - 11, 28, 22, 8, 8));

                // Antena Robot
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, cy - 11, cx, cy - 17);
                g2.setColor(new Color(254, 240, 138)); // Antena emas
                g2.fill(new Ellipse2D.Float(cx - 3, cy - 21, 6, 6));

                // Telinga Robot Kiri & Kanan
                g2.setColor(new Color(241, 245, 249));
                g2.fill(new RoundRectangle2D.Float(cx - 17, cy - 6, 3, 11, 2, 2));
                g2.fill(new RoundRectangle2D.Float(cx + 14, cy - 6, 3, 11, 2, 2));

                // Mata Robot (Biru Cerah Bersahabat)
                g2.setColor(new Color(13, 148, 136));
                g2.fill(new Ellipse2D.Float(cx - 9, cy - 5, 5, 5));
                g2.fill(new Ellipse2D.Float(cx + 4, cy - 5, 5, 5));

                // Senyum Robot
                g2.setColor(new Color(15, 118, 110));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cx - 6, cy - 1, 12, 7, 200, 140);

                // 5. Badge Status Hijau Aktif (Pulsing Dot) di Kanan Atas Lingkaran
                int dotX = offset + d - 14;
                int dotY = offset + 2;
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Float(dotX - 2, dotY - 2, 16, 16));
                g2.setColor(new Color(34, 197, 94)); // Emerald-500
                g2.fill(new Ellipse2D.Float(dotX, dotY, 12, 12));

                // 6. Teks Label "BOY" Kecil di Bawah Kepala
                g2.setColor(new Color(204, 251, 241));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                String tag = "BOY";
                int tagW = g2.getFontMetrics().stringWidth(tag);
                g2.drawString(tag, cx - (tagW / 2), cy + 18);

                g2.dispose();
            }
        };

        pnlBubble.setOpaque(false);
        pnlBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlBubble.setToolTipText("Asisten AI Boy - Klik untuk membuka tanya jawab klinis & rekam medis");

        pnlBubble.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(GelembungChatAIBoy.this, e.getX(), e.getY());
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    dragOffset = e.getPoint();
                    isDragging = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !isDragging) {
                    bukaChat();
                }
                isDragging = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });

        pnlBubble.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset != null && SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true;
                    Point current = getLocation();
                    int newX = current.x + e.getX() - dragOffset.x;
                    int newY = current.y + e.getY() - dragOffset.y;

                    // Batasi agar tidak terseret keluar layar monitor
                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                    newX = Math.max(0, Math.min(newX, screen.width - getWidth()));
                    newY = Math.max(0, Math.min(newY, screen.height - getHeight() - 40));

                    setLocation(newX, newY);
                }
            }
        });

        setContentPane(pnlBubble);
    }

    /**
     * Memposisikan gelembung secara presisi di pojok kanan bawah monitor utama.
     */
    public void posisikanDefault() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());

            int screenWidth = screenSize.width - insets.right;
            int screenHeight = screenSize.height - insets.bottom;

            int posX = screenWidth - getWidth() - 25;
            int posY = screenHeight - getHeight() - 55;

            setLocation(posX, posY);
        } catch (Exception ex) {
            setLocation(800, 600);
        }
    }

    /**
     * Membuka atau menutup dialog chat Asisten AI Boy tepat di dekat gelembung.
     * Tidak memicu penataan ulang Z-order pada form lain yang sedang aktif.
     */
    public void bukaChat() {
        DlgAsistenAIBoy dlg = DlgAsistenAIBoy.getInstance(null);
        if (dlg.isVisible()) {
            dlg.setVisible(false);
        } else {
            // Posisikan jendela chat tepat di atas pojok kanan gelembung
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
            int maxW = screenSize.width - insets.right;
            int maxH = screenSize.height - insets.bottom;

            int chatW = dlg.getWidth();
            int chatH = dlg.getHeight();

            int targetX = maxW - chatW - 20;
            int targetY = maxH - chatH - 80;

            dlg.setLocation(Math.max(10, targetX), Math.max(10, targetY));
            dlg.setAlwaysOnTop(true);
            dlg.setVisible(true);
            dlg.toFront();
        }
    }

    /**
     * Tampilkan gelembung chat jika status AI aktif di sistem dan pengguna sedang di SIMRS.
     * (Saat ini dinonaktifkan / disembunyikan sementara atas arahan user).
     */
    public void tampilkan() {
        this.isAktifDiSimrs = false;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(false);
            }
        });
    }

    /**
     * Sembunyikan gelembung chat sementara.
     */
    public void sembunyikan() {
        this.isAktifDiSimrs = false;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(false);
            }
        });
    }
}
