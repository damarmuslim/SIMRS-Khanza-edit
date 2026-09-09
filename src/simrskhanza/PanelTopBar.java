package simrskhanza;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Top App Bar for SIMRS Khanza.
 * Matches RSUD Demang Sepulau Raya design specification:
 * - Dark pine/teal green background (#0b332c)
 * - Hospital Title ("SIM " + nama_instansi)
 * - Navigation links ("Program", "Presensi Pegawai")
 * - User status indicator & 3 connection/auth status dots
 */
public class PanelTopBar extends JPanel {

    private final JLabel lblHospitalTitle;
    private final JLabel lblUserStatus;
    private final StatusDotsPanel pnlDots;
    private final JPanel pnlLeft;
    private javax.swing.JMenuBar menuBar;
    private boolean isLoggedIn = false;

    public PanelTopBar() {
        setPreferredSize(new Dimension(800, 32));
        setBackground(new Color(11, 51, 44)); // Dark forest teal #0b332c
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(7, 36, 30)));

        // -------------------------------------------------------------
        // LEFT SIDE: Icon + SIM [RS] + MenuBar
        // -------------------------------------------------------------
        pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        pnlLeft.setOpaque(false);

        // Small medical emblem icon
        JPanel pnlMedIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Hospital green rounded square with white cross
                g2.setColor(new Color(16, 185, 129)); // Emerald #10b981
                g2.fillRoundRect(0, 1, 18, 18, 5, 5);
                g2.setColor(Color.WHITE);
                g2.fillRect(7, 4, 4, 12);
                g2.fillRect(3, 8, 12, 4);
                g2.dispose();
            }
        };
        pnlMedIcon.setOpaque(false);
        pnlMedIcon.setPreferredSize(new Dimension(20, 20));
        pnlLeft.add(pnlMedIcon);

        lblHospitalTitle = new JLabel("SIM RSUD DEMANG SEPULAU RAYA");
        lblHospitalTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHospitalTitle.setForeground(Color.WHITE);
        pnlLeft.add(lblHospitalTitle);

        JLabel lblSep = new JLabel("|");
        lblSep.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSep.setForeground(new Color(30, 90, 78));
        pnlLeft.add(lblSep);

        add(pnlLeft, BorderLayout.WEST);

        // -------------------------------------------------------------
        // RIGHT SIDE: User Status + 3 Dots
        // -------------------------------------------------------------
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        pnlRight.setOpaque(false);

        lblUserStatus = new JLabel("Belum masuk");
        lblUserStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUserStatus.setForeground(new Color(167, 193, 186)); // Muted teal-grey
        pnlRight.add(lblUserStatus);

        pnlDots = new StatusDotsPanel();
        pnlRight.add(pnlDots);

        add(pnlRight, BorderLayout.EAST);
    }

    public void setMenuBar(javax.swing.JMenuBar mb) {
        if (this.menuBar != null) {
            pnlLeft.remove(this.menuBar);
        }
        this.menuBar = mb;
        if (mb != null) {
            mb.setOpaque(false);
            mb.setBackground(new Color(11, 51, 44));
            mb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            mb.setPreferredSize(null);

            for (int i = 0; i < mb.getMenuCount(); i++) {
                javax.swing.JMenu m = mb.getMenu(i);
                if (m != null) {
                    m.setOpaque(false);
                    m.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    m.setForeground(new Color(220, 235, 230));
                    m.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    m.putClientProperty("FlatLaf.style", "selectionBackground: #144e43; selectionForeground: #ffffff");
                }
            }
            pnlLeft.add(mb);
        }
        pnlLeft.revalidate();
        pnlLeft.repaint();
    }

    public javax.swing.JMenuBar getMenuBar() {
        return this.menuBar;
    }

    public void updateHospitalName(String nama) {
        if (nama != null && !nama.trim().isEmpty()) {
            lblHospitalTitle.setText("SIM " + nama.trim().toUpperCase());
        }
    }

    public void setUserStatus(String userText, boolean loggedIn) {
        this.isLoggedIn = loggedIn;
        if (!loggedIn || userText == null || userText.equalsIgnoreCase("Log Out") || userText.trim().isEmpty()) {
            lblUserStatus.setText("Belum masuk");
            pnlDots.setAuthActive(false);
        } else {
            lblUserStatus.setText(userText);
            pnlDots.setAuthActive(true);
        }
        repaint();
    }

    // -------------------------------------------------------------
    // SUBCOMPONENT: 3 Status Dots
    // -------------------------------------------------------------
    private static class StatusDotsPanel extends JPanel {
        private boolean authActive = false;

        public StatusDotsPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(36, 18));
        }

        public void setAuthActive(boolean active) {
            this.authActive = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int y = 5;
            int d = 7;

            // Dot 1: Database status (Green)
            g2.setColor(new Color(34, 197, 94));
            g2.fillOval(0, y, d, d);

            // Dot 2: Server status (Green)
            g2.setColor(new Color(34, 197, 94));
            g2.fillOval(12, y, d, d);

            // Dot 3: Auth status (Green if logged in, Red/Orange if logged out)
            if (authActive) {
                g2.setColor(new Color(34, 197, 94)); // Green
            } else {
                g2.setColor(new Color(239, 68, 68)); // Red #ef4444
            }
            g2.fillOval(24, y, d, d);

            g2.dispose();
        }
    }
}
