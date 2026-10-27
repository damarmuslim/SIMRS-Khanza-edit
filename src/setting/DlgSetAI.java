package setting;

import fungsi.AsistenAIBoy;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Dialog Pengaturan Asisten AI Boy untuk SIMRS Khanza (RSUD Demang Sepulau Raya).
 * Memungkinkan Admin Utama mengatur API Key Google Gemini, model AI, status,
 * serta mendeteksi dan menguji koneksi model AI secara langsung.
 */
public class DlgSetAI extends JDialog {

    private final JTextField txtNama = new JTextField("Boy");
    private final JPasswordField txtApiKey = new JPasswordField();
    private final JButton btnToggleShow = new JButton("Lihat");
    private final JComboBox<String> cmbModel = new JComboBox<>(new String[]{
        "gemini-flash-latest",
        "gemini-3.5-flash",
        "gemini-3.6-flash",
        "gemini-3.7-flash",
        "gemini-pro-latest",
        "gemini-1.5-flash-latest",
        "gemini-2.0-flash",
        "gemini-2.5-flash"
    });
    private final JButton btnDeteksiModel = new JButton("Cek Model Aktif");
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Aktif", "Nonaktif"});
    private final JTextField txtTemp = new JTextField("0.3");
    private final JTextArea txtPrompt = new JTextArea();
    private final JLabel lblStatusTes = new JLabel("Status: Siap diuji");

    private final JButton btnTes = new JButton("Tes Koneksi API");
    private final JButton btnSimpan = new JButton("Simpan Perubahan");
    private final JButton btnKeluar = new JButton("Tutup");

    private boolean isPasswordShown = false;

    public DlgSetAI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Pengaturan Asisten AI");
        setSize(680, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        muatData();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(248, 250, 252));

        // HEADER
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(15, 61, 62)); // Deep Medical Teal
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("PENGATURAN ASISTEN AI (BOY)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Konfigurasi Google Gemini API & Basis Pengetahuan SIMRS RSUD Demang Sepulau Raya");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(204, 251, 241)); // Mint

        JPanel pnlTitleBox = new JPanel();
        pnlTitleBox.setOpaque(false);
        pnlTitleBox.setLayout(new javax.swing.BoxLayout(pnlTitleBox, javax.swing.BoxLayout.Y_AXIS));
        pnlTitleBox.add(lblTitle);
        pnlTitleBox.add(javax.swing.Box.createVerticalStrut(4));
        pnlTitleBox.add(lblSub);

        pnlHeader.add(pnlTitleBox, BorderLayout.CENTER);
        root.add(pnlHeader, BorderLayout.NORTH);

        // FORM FIELDS
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 1. Nama Asisten
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        pnlForm.add(buatLabel("Nama Asisten AI:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.75;
        txtNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtNama.setPreferredSize(new Dimension(280, 28));
        pnlForm.add(txtNama, gbc);

        // 2. API Key Gemini
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        pnlForm.add(buatLabel("Google Gemini API Key:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.75;

        JPanel pnlKey = new JPanel(new BorderLayout(4, 0));
        pnlKey.setOpaque(false);
        txtApiKey.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtApiKey.setPreferredSize(new Dimension(320, 28));
        pnlKey.add(txtApiKey, BorderLayout.CENTER);

        btnToggleShow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnToggleShow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPasswordShown = !isPasswordShown;
                if (isPasswordShown) {
                    txtApiKey.setEchoChar((char) 0);
                    btnToggleShow.setText("Tutup");
                } else {
                    txtApiKey.setEchoChar('•');
                    btnToggleShow.setText("Lihat");
                }
            }
        });
        pnlKey.add(btnToggleShow, BorderLayout.EAST);
        pnlForm.add(pnlKey, gbc);

        // 3. Pilihan Model + Tombol Deteksi
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        pnlForm.add(buatLabel("Model AI:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.75;

        JPanel pnlModelRow = new JPanel(new BorderLayout(6, 0));
        pnlModelRow.setOpaque(false);

        cmbModel.setEditable(true);
        cmbModel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbModel.setPreferredSize(new Dimension(260, 28));
        pnlModelRow.add(cmbModel, BorderLayout.CENTER);

        btnDeteksiModel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDeteksiModel.setForeground(new Color(15, 118, 110));
        btnDeteksiModel.setBackground(new Color(240, 253, 250));
        btnDeteksiModel.setBorder(BorderFactory.createLineBorder(new Color(204, 251, 241), 1, true));
        btnDeteksiModel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeteksiModel.setPreferredSize(new Dimension(135, 28));
        btnDeteksiModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lakukanDeteksiModel();
            }
        });
        pnlModelRow.add(btnDeteksiModel, BorderLayout.EAST);
        pnlForm.add(pnlModelRow, gbc);

        // 4. Status
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        pnlForm.add(buatLabel("Status Asisten:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.75;
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbStatus.setPreferredSize(new Dimension(140, 28));
        pnlForm.add(cmbStatus, gbc);

        // 5. Suhu / Temperature
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        pnlForm.add(buatLabel("Suhu / Kreativitas (0.1 - 1.0):"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.75;
        txtTemp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTemp.setPreferredSize(new Dimension(90, 28));
        pnlForm.add(txtTemp, gbc);

        // 6. System Prompt
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        pnlForm.add(buatLabel("Instruksi Sistem (System Prompt):"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtPrompt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPrompt.setLineWrap(true);
        txtPrompt.setWrapStyleWord(true);
        txtPrompt.setRows(4);
        JScrollPane spPrompt = new JScrollPane(txtPrompt);
        spPrompt.setPreferredSize(new Dimension(380, 95));
        pnlForm.add(spPrompt, gbc);

        // 7. Status Tes
        row++;
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.75;
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        lblStatusTes.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatusTes.setForeground(new Color(71, 85, 105));
        pnlForm.add(lblStatusTes, gbc);

        root.add(pnlForm, BorderLayout.CENTER);

        // FOOTER ACTIONS
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlFooter.setBackground(new Color(241, 245, 249));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        // Tombol Tes
        btnTes.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTes.setBackground(new Color(13, 148, 136));
        btnTes.setForeground(Color.WHITE);
        btnTes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTes.setPreferredSize(new Dimension(140, 32));
        btnTes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lakukanTesKoneksi();
            }
        });
        pnlFooter.add(btnTes);

        // Tombol Simpan
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimpan.setBackground(new Color(15, 61, 62));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.setPreferredSize(new Dimension(150, 32));
        btnSimpan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lakukanSimpan();
            }
        });
        pnlFooter.add(btnSimpan);

        // Tombol Keluar
        btnKeluar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnKeluar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKeluar.setPreferredSize(new Dimension(90, 32));
        btnKeluar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pnlFooter.add(btnKeluar);

        root.add(pnlFooter, BorderLayout.SOUTH);
        getContentPane().add(root);
    }

    private JLabel buatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(30, 41, 59));
        return l;
    }

    private void muatData() {
        AsistenAIBoy ai = AsistenAIBoy.getInstance();
        ai.muatKonfigurasi();

        txtNama.setText(ai.getNamaAsisten());
        txtApiKey.setText(ai.getApiKey());
        cmbModel.setSelectedItem(ai.getModel());
        cmbStatus.setSelectedItem(ai.getStatus());
        txtTemp.setText(String.valueOf(ai.getTemperature()));
        txtPrompt.setText(ai.getSystemPrompt());
    }

    private void lakukanDeteksiModel() {
        final String key = new String(txtApiKey.getPassword()).trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan masukkan API Key Google Gemini terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnDeteksiModel.setEnabled(false);
        lblStatusTes.setForeground(new Color(13, 148, 136));
        lblStatusTes.setText("Mendeteksi daftar model aktif dari Google AI Studio...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> list = AsistenAIBoy.getInstance().ambilDaftarModelTersedia(key);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        btnDeteksiModel.setEnabled(true);
                        if (!list.isEmpty()) {
                            cmbModel.removeAllItems();
                            for (String m : list) {
                                cmbModel.addItem(m);
                            }
                            // Prioritaskan model flash stabil
                            if (list.contains("gemini-flash-latest")) {
                                cmbModel.setSelectedItem("gemini-flash-latest");
                            } else if (list.contains("gemini-3.5-flash")) {
                                cmbModel.setSelectedItem("gemini-3.5-flash");
                            } else if (list.contains("gemini-2.0-flash")) {
                                cmbModel.setSelectedItem("gemini-2.0-flash");
                            } else {
                                for (String m : list) {
                                    if (m.contains("flash") && !m.contains("image") && !m.contains("tts") && !m.contains("transcribe")) {
                                        cmbModel.setSelectedItem(m);
                                        break;
                                    }
                                }
                            }
                            lblStatusTes.setForeground(new Color(16, 185, 129));
                            lblStatusTes.setText("Berhasil menemukan " + list.size() + " model aktif untuk API Key ini.");
                            JOptionPane.showMessageDialog(DlgSetAI.this,
                                "Ditemukan " + list.size() + " model aktif dari Google AI Studio!\nModel otomatis diperbarui ke: " + cmbModel.getSelectedItem(),
                                "Deteksi Model Sukses", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            lblStatusTes.setForeground(new Color(239, 68, 68));
                            lblStatusTes.setText("Gagal mengambil model. Periksa koneksi internet atau validitas API Key.");
                        }
                    }
                });
            }
        }).start();
    }

    private void lakukanTesKoneksi() {
        final String key = new String(txtApiKey.getPassword()).trim();
        final String mdl = cmbModel.getSelectedItem() != null ? cmbModel.getSelectedItem().toString() : "gemini-flash-latest";

        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan masukkan API Key Google Gemini terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnTes.setEnabled(false);
        lblStatusTes.setForeground(new Color(13, 148, 136));
        lblStatusTes.setText("Menguji koneksi ke Google Gemini API, mohon tunggu...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String resp = AsistenAIBoy.getInstance().tesKoneksi(key, mdl);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            btnTes.setEnabled(true);
                            String activeMdl = AsistenAIBoy.getInstance().getModel();
                            if (activeMdl != null && !activeMdl.isEmpty()) {
                                cmbModel.setSelectedItem(activeMdl);
                            }
                            lblStatusTes.setForeground(new Color(16, 185, 129)); // Hijau emerald
                            lblStatusTes.setText("Sukses: " + resp);
                            JOptionPane.showMessageDialog(DlgSetAI.this,
                                "Koneksi ke Google Gemini Berhasil!\n" + resp,
                                "Tes Koneksi Sukses", JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            btnTes.setEnabled(true);
                            lblStatusTes.setForeground(new Color(239, 68, 68)); // Merah
                            lblStatusTes.setText("Gagal: " + ex.getMessage());
                            JOptionPane.showMessageDialog(DlgSetAI.this,
                                "Koneksi Gagal!\nPesan: " + ex.getMessage(),
                                "Tes Koneksi Gagal", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            }
        }).start();
    }

    private void lakukanSimpan() {
        String nama = txtNama.getText().trim();
        String key = new String(txtApiKey.getPassword()).trim();
        String mdl = cmbModel.getSelectedItem() != null ? cmbModel.getSelectedItem().toString() : "gemini-flash-latest";
        String stts = cmbStatus.getSelectedItem() != null ? cmbStatus.getSelectedItem().toString() : "Aktif";
        String prompt = txtPrompt.getText().trim();

        if (nama.isEmpty()) nama = "Boy";

        double temp = 0.3;
        try {
            temp = Double.parseDouble(txtTemp.getText().trim());
        } catch (Exception e) {
            temp = 0.3;
        }

        boolean sukses = AsistenAIBoy.getInstance().simpanKonfigurasi(nama, key, mdl, stts, temp, prompt);
        if (sukses) {
            JOptionPane.showMessageDialog(this,
                "Konfigurasi Asisten AI " + nama + " berhasil disimpan ke database.\nSeluruh komputer pelayanan otomatis aktif dengan pengaturan ini.",
                "Simpan Berhasil", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal menyimpan ke database. Pastikan koneksi database aktif.",
                "Simpan Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
