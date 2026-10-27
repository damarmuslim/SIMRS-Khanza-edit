package simrskhanza;

import fungsi.AsistenAIBoy;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * JDialog Chat Modern Resmi Asisten AI Boy untuk SIMRS Khanza (RSUD Demang Sepulau Raya).
 * Memiliki antarmuka bubble chat elegan, quick prompt shortcuts, context injection, dan integrasi clipboard.
 */
public class DlgAsistenAIBoy extends JDialog {

    private static DlgAsistenAIBoy dialogInstance;

    private final JPanel pnlChatContent = new JPanel();
    private final JScrollPane spChat = new JScrollPane();
    private final JTextField txtInput = new JTextField();
    private final JButton btnKirim = new JButton("Kirim");
    private final JLabel lblStatus = new JLabel("Siap membantu pelayanan rekam medis & klinis");
    private final JPanel pnlKonteks = new JPanel(new BorderLayout());
    private final JLabel lblKonteks = new JLabel("");
    private String konteksAktif = "";

    public static synchronized DlgAsistenAIBoy getInstance(java.awt.Frame parent) {
        if (dialogInstance == null) {
            dialogInstance = new DlgAsistenAIBoy(null);
        }
        return dialogInstance;
    }

    public DlgAsistenAIBoy(java.awt.Frame parent) {
        super((java.awt.Frame) null, false); // Berinduk null agar tidak mengganggu Z-order form lain
        setTitle("Boy - Asisten AI SIMRS Khanza");
        setSize(480, 620);
        setMinimumSize(new Dimension(420, 500));
        setAlwaysOnTop(true);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(248, 250, 252));

        // 1. HEADER MODERN (Deep Medical Teal)
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setBackground(new Color(15, 61, 62)); // #0F3D3E
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Avatar Robot Boy + Judul
        JPanel pnlBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBrand.setOpaque(false);

        JLabel lblAvatar = new JLabel("🤖") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 184, 166)); // Teal-500
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(36, 36));
        lblAvatar.setHorizontalAlignment(JLabel.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        pnlBrand.add(lblAvatar);

        JPanel pnlText = new JPanel();
        pnlText.setOpaque(false);
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));

        JLabel lblNama = new JLabel("Boy • Asisten AI");
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNama.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("RSUD Demang Sepulau Raya • Online");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(153, 246, 228)); // Teal-200

        pnlText.add(lblNama);
        pnlText.add(Box.createVerticalStrut(2));
        pnlText.add(lblSub);
        pnlBrand.add(pnlText);
        pnlHeader.add(pnlBrand, BorderLayout.WEST);

        // Tombol Pengaturan & Bersihkan di Kanan Header
        JPanel pnlActionHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        pnlActionHeader.setOpaque(false);

        JButton btnSetting = buatTombolHeader("⚙ Pengaturan");
        btnSetting.setVisible(fungsi.akses.getadmin());
        btnSetting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!fungsi.akses.getadmin()) {
                    JOptionPane.showMessageDialog(DlgAsistenAIBoy.this,
                        "Menu Pengaturan Asisten AI hanya dapat diakses oleh Administrator Sistem.",
                        "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                setting.DlgSetAI dlg = new setting.DlgSetAI(null, true);
                dlg.setVisible(true);
            }
        });
        pnlActionHeader.add(btnSetting);

        JButton btnClear = buatTombolHeader("🗑 Bersihkan");
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlChatContent.removeAll();
                pnlChatContent.revalidate();
                pnlChatContent.repaint();
                tampilkanPesanSambutan();
            }
        });
        pnlActionHeader.add(btnClear);

        JButton btnMinimize = buatTombolHeader("➖ Minimalkan");
        btnMinimize.setToolTipText("Kecilkan ke gelembung chat melayang di pojok kanan bawah");
        btnMinimize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        pnlActionHeader.add(btnMinimize);

        pnlHeader.add(pnlActionHeader, BorderLayout.EAST);
        root.add(pnlHeader, BorderLayout.NORTH);

        // 2. BANNER KONTEKS PASIEN (DYNAMIC)
        pnlKonteks.setBackground(new Color(254, 243, 199)); // Amber-100
        pnlKonteks.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(251, 191, 36)));
        lblKonteks.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblKonteks.setForeground(new Color(146, 64, 14)); // Amber-800
        lblKonteks.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        pnlKonteks.add(lblKonteks, BorderLayout.CENTER);

        JButton btnHapusKonteks = new JButton("✕");
        btnHapusKonteks.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnHapusKonteks.setContentAreaFilled(false);
        btnHapusKonteks.setBorderPainted(false);
        btnHapusKonteks.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapusKonteks.setToolTipText("Lepas Konteks Pasien");
        btnHapusKonteks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setKonteksPasien("");
            }
        });
        pnlKonteks.add(btnHapusKonteks, BorderLayout.EAST);
        pnlKonteks.setVisible(false);

        // 3. CHAT AREA CONTAINER
        pnlChatContent.setLayout(new BoxLayout(pnlChatContent, BoxLayout.Y_AXIS));
        pnlChatContent.setBackground(new Color(248, 250, 252));
        pnlChatContent.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        spChat.setViewportView(pnlChatContent);
        spChat.setBorder(null);
        spChat.getVerticalScrollBar().setUnitIncrement(16);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.add(pnlKonteks, BorderLayout.NORTH);
        pnlCenter.add(spChat, BorderLayout.CENTER);
        root.add(pnlCenter, BorderLayout.CENTER);

        // 4. FOOTER INPUT & QUICK SHORTCUTS
        JPanel pnlFooter = new JPanel();
        pnlFooter.setLayout(new BoxLayout(pnlFooter, BoxLayout.Y_AXIS));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        // Quick Shortcuts Row
        JPanel pnlShortcuts = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pnlShortcuts.setBackground(Color.WHITE);
        pnlShortcuts.add(buatPillShortcut("🩺 Draf SOAP", "Tolong buatkan draf SOAP lengkap untuk pasien dengan keluhan: "));
        pnlShortcuts.add(buatPillShortcut("💊 Cek Obat", "Apakah ada interaksi berbahaya atau penyesuaian dosis untuk kombinasi obat berikut: "));
        pnlShortcuts.add(buatPillShortcut("📋 Saran ICD-10", "Apa kode ICD-10 dan ICD-9-CM yang paling sesuai untuk diagnosis: "));
        pnlShortcuts.add(buatPillShortcut("🔄 Alur SIMRS", "Bagaimana langkah operasional di SIMRS Khanza untuk: "));
        pnlFooter.add(pnlShortcuts);

        // Input Box + Send Button
        JPanel pnlInputBox = new JPanel(new BorderLayout(8, 0));
        pnlInputBox.setBackground(Color.WHITE);
        pnlInputBox.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    kirimPesan();
                }
            }
        });
        pnlInputBox.add(txtInput, BorderLayout.CENTER);

        btnKirim.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnKirim.setBackground(new Color(15, 61, 62));
        btnKirim.setForeground(Color.WHITE);
        btnKirim.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKirim.setPreferredSize(new Dimension(80, 32));
        btnKirim.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kirimPesan();
            }
        });
        pnlInputBox.add(btnKirim, BorderLayout.EAST);
        pnlFooter.add(pnlInputBox);

        // Status Label
        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        pnlStatus.setBackground(new Color(248, 250, 252));
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblStatus.setForeground(new Color(100, 116, 139));
        pnlStatus.add(lblStatus);
        pnlFooter.add(pnlStatus);

        root.add(pnlFooter, BorderLayout.SOUTH);
        getContentPane().add(root);

        tampilkanPesanSambutan();
    }

    private JButton buatTombolHeader(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 1, true));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 24));
        return b;
    }

    private JButton buatPillShortcut(final String label, final String templateText) {
        JButton b = new JButton(label);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(new Color(15, 118, 110)); // Teal-700
        b.setBackground(new Color(240, 253, 250)); // Teal-50
        b.setBorder(BorderFactory.createLineBorder(new Color(204, 251, 241), 1, true));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtInput.setText(templateText);
                txtInput.requestFocus();
                txtInput.setCaretPosition(txtInput.getText().length());
            }
        });
        return b;
    }

    public void setKonteksPasien(String konteks) {
        this.konteksAktif = (konteks != null) ? konteks.trim() : "";
        if (!this.konteksAktif.isEmpty()) {
            lblKonteks.setText("📌 Konteks Terlampir: " + (this.konteksAktif.length() > 65 ? this.konteksAktif.substring(0, 65) + "..." : this.konteksAktif));
            pnlKonteks.setVisible(true);
        } else {
            lblKonteks.setText("");
            pnlKonteks.setVisible(false);
        }
        revalidate();
        repaint();
    }

    private void tampilkanPesanSambutan() {
        String sambutan = "Halo Dok/Ners! Saya **Boy**, Asisten AI SIMRS Khanza RSUD Demang Sepulau Raya.\n\n"
                + "Saya siap membantu Anda dalam:\n"
                + "• Menyusun dan merapikan draf **SOAP / CPPT rekam medis**\n"
                + "• Mengecek **interaksi obat** dan rekomendasi dosis farmakologi\n"
                + "• Menyarankan kode diagnosis **ICD-10 & ICD-9-CM**\n"
                + "• Panduan menu & alur operasional SIMRS Khanza\n\n"
                + "Ada yang bisa saya bantu hari ini?";
        tambahBubblePesan("Boy", sambutan, false);
    }

    private void kirimPesan() {
        final String pesan = txtInput.getText().trim();
        if (pesan.isEmpty()) return;

        txtInput.setText("");
        tambahBubblePesan("Anda", pesan, true);

        btnKirim.setEnabled(false);
        txtInput.setEnabled(false);
        lblStatus.setText("Boy sedang menganalisis & menyusun jawaban...");
        lblStatus.setForeground(new Color(13, 148, 136));

        AsistenAIBoy.getInstance().kirimPesanAsync(pesan, konteksAktif, new AsistenAIBoy.Callback() {
            @Override
            public void onSukses(String jawaban) {
                btnKirim.setEnabled(true);
                txtInput.setEnabled(true);
                txtInput.requestFocus();
                lblStatus.setText("Siap membantu pelayanan rekam medis & klinis");
                lblStatus.setForeground(new Color(100, 116, 139));
                tambahBubblePesan("Boy", jawaban, false);
            }

            @Override
            public void onGagal(String pesanError) {
                btnKirim.setEnabled(true);
                txtInput.setEnabled(true);
                txtInput.requestFocus();
                lblStatus.setText("Gagal mendapatkan respons dari Google Gemini.");
                lblStatus.setForeground(new Color(239, 68, 68));
                tambahBubblePesan("Boy", "⚠️ " + pesanError, false);
            }
        });
    }

    private void tambahBubblePesan(final String pengirim, final String teks, final boolean isUser) {
        JPanel pnlRow = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 4, 4));
        pnlRow.setOpaque(false);

        JPanel bubble = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isUser) {
                    g2.setColor(new Color(15, 61, 62)); // Teal tua
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                if (!isUser) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 12, 12));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Format waktu & pengirim
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String waktu = sdf.format(new Date());

        JLabel lblMeta = new JLabel((isUser ? "Anda • " : "Boy • ") + waktu);
        lblMeta.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMeta.setForeground(isUser ? new Color(153, 246, 228) : new Color(13, 148, 136));
        bubble.add(lblMeta, BorderLayout.NORTH);

        // Teks Isi (HTML Format agar rapi)
        String htmlFormatted = formatMarkdownToHtml(teks, isUser);
        JLabel lblBody = new JLabel(htmlFormatted);
        lblBody.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bubble.add(lblBody, BorderLayout.CENTER);

        // Tombol Salin untuk jawaban Boy
        if (!isUser) {
            JButton btnCopy = new JButton("Salin");
            btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btnCopy.setForeground(new Color(100, 116, 139));
            btnCopy.setContentAreaFilled(false);
            btnCopy.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            btnCopy.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCopy.setToolTipText("Salin Teks ke Clipboard");
            btnCopy.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(teks), null);
                    JOptionPane.showMessageDialog(DlgAsistenAIBoy.this, "Teks berhasil disalin ke clipboard!", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            bubble.add(btnCopy, BorderLayout.SOUTH);
        }

        pnlRow.add(bubble);
        pnlChatContent.add(pnlRow);
        pnlChatContent.add(Box.createVerticalStrut(4));
        pnlChatContent.revalidate();
        pnlChatContent.repaint();

        // Auto-scroll ke paling bawah
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JScrollBar vBar = spChat.getVerticalScrollBar();
                vBar.setValue(vBar.getMaximum());
            }
        });
    }

    private String formatMarkdownToHtml(String text, boolean isUser) {
        if (text == null) return "";
        String textColor = isUser ? "#FFFFFF" : "#1E293B";
        String linkColor = isUser ? "#99F6E4" : "#0F766E";

        String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // Bold **teks**
        escaped = escaped.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        // Italic *teks*
        escaped = escaped.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
        // Newline to <br>
        escaped = escaped.replace("\n", "<br>");

        return "<html><div style='width: 340px; color: " + textColor + "; line-height: 1.4;'>" + escaped + "</div></html>";
    }
}
