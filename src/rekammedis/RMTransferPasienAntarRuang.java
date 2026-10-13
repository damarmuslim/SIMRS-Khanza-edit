package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;
import widget.ThemeConfig;

/**
 * TRANSFER PASIEN ANTAR RUANGAN (RM 04-1 / RM 04-2)
 * RSUD Demang Sepulau Raya.
 *
 * <p>Dibangun ulang dengan arsitektur modern Swing high-fidelity sesuai panduan
 * desain design_handoff_simrs_rm_forms/README.md dan prototipe
 * Transfer Pasien Antar Ruang RM04 Redesign.dc.html.</p>
 */
public final class RMTransferPasienAntarRuang extends javax.swing.JDialog {

    // Palet warna resmi Design System RSUD Demang Sepulau Raya
    private static final Color TEAL_HEADER     = new Color(0x12, 0x3c, 0x3a);
    private static final Color TEAL_ACCENT     = new Color(0x0d, 0x7a, 0x70);
    private static final Color TEAL_HOVER      = new Color(0x0a, 0x5d, 0x55);
    private static final Color TEAL_LIGHT      = new Color(0xf1, 0xf7, 0xf6);
    private static final Color CHIP_ON_BG      = new Color(0xe9, 0xf5, 0xf2);
    private static final Color BG_PAGE         = new Color(0xe8, 0xe9, 0xe6);
    private static final Color CARD_BG         = Color.WHITE;
    private static final Color CARD_BORDER     = new Color(0xdc, 0xdf, 0xda);
    private static final Color CARD_HEADER_BG  = new Color(0xfa, 0xfb, 0xf9);
    private static final Color PATIENT_CARD_BG = new Color(0xf7, 0xf8, 0xf5);
    private static final Color TEXT_DARK       = new Color(0x1c, 0x1f, 0x1d);
    private static final Color TEXT_MUTED      = new Color(0x9a, 0xa1, 0x9c);
    private static final Color TEXT_LABEL      = new Color(0x3d, 0x44, 0x41);
    private static final Color TEXT_LABEL_MUTED= new Color(0x8b, 0x92, 0x8d);
    private static final Color BORDER_GRAY     = new Color(0xcf, 0xd3, 0xce);
    private static final Color GREEN_BG        = new Color(0xe8, 0xf3, 0xed);
    private static final Color GREEN_FG        = new Color(0x1a, 0x6b, 0x4c);
    private static final Color ORANGE_BG       = new Color(0xfd, 0xf3, 0xe7);
    private static final Color ORANGE_FG       = new Color(0x8a, 0x5a, 0x12);
    private static final Color GRAY_TALLY_BG   = new Color(0xf2, 0xf3, 0xf0);
    private static final Color GRAY_TALLY_FG   = new Color(0x9a, 0xa1, 0x9c);

    /**
     * Kolom aktif tabel `transfer_pasien_antar_ruang` yang ditulis oleh simpan()/ganti().
     * Tetap 100% sinkron dengan migrasi SQL sik_transfer_pasien_antar_ruang_rm04.sql.
     */
    private static final String[] KOLOM_TRANSFER = {
        "no_rawat","tanggal_masuk","tanggal_pindah","asal_ruang","ruang_selanjutnya","diagnosa_utama","diagnosa_sekunder",
        "indikasi_pindah_ruang","keterangan_indikasi_pindah_ruang","prosedur_yang_sudah_dilakukan","obat_yang_telah_diberikan",
        "metode_pemindahan_pasien","pasien_keluarga_menyetujui","nama_menyetujui","hubungan_menyetujui","nip_menyerahkan","nip_menerima",
        "no_tlp_menyetujui","sudah_dijelaskan_diagnosa","kelas_asal_ruang","kelas_ruang_selanjutnya",
        "kd_dokter_merawat_asal_1","kd_dokter_merawat_asal_2","kd_dokter_merawat_asal_3",
        "kd_dokter_merawat_tujuan_1","kd_dokter_merawat_tujuan_2","kd_dokter_merawat_tujuan_3",

        "kesadaran_transfer","td_transfer","hr_transfer","rr_transfer","suhu_transfer","gcs_transfer","spo2_transfer",
        "tingkat_nyeri_transfer","observasi_terakhir_pukul",

        "oksigen_status","oksigen_nasal_kanule","oksigen_nasal_kateter","oksigen_face_mask","oksigen_sungkup_rebreathing",
        "oksigen_sungkup_non_rebreathing","oksigen_tracheostomy","oksigen_ett",

        "line_invasif","line_invasif_tanggal",
        "kewaspadaan_standar","kewaspadaan_kontak","kewaspadaan_airborne","kewaspadaan_droplet",

        "nutrisi_puasa","nutrisi_oral","nutrisi_ngt","nutrisi_batasan_cairan_cc","nutrisi_diet_khusus","nutrisi_diet_khusus_jelaskan",

        "eliminasi_bab","eliminasi_bak","eliminasi_bak_kateter_no","eliminasi_bak_kateter_tanggal_pasang",

        "aktifitas","mobilisasi","drainase_wsd","drainase_drain_luka","drainase_stoma","drainase_lain","drainase_lain_ket",

        "luka_tidak_ada",
        "luka_operasi_cek","luka_operasi_ukuran","luka_operasi_lokasi","luka_operasi_kondisi",
        "luka_dekubitus_cek","luka_dekubitus_ukuran","luka_dekubitus_lokasi","luka_dekubitus_kondisi",
        "luka_skin_lost_cek","luka_skin_lost_ukuran","luka_skin_lost_lokasi","luka_skin_lost_kondisi",
        "luka_gangren_cek","luka_gangren_ukuran","luka_gangren_lokasi","luka_gangren_kondisi",
        "luka_lain_cek","luka_lain_ket",

        "hasil_investigasi_abnormal",

        "tindakan_resiko_jatuh","tindakan_pemasangan_restrain","tindakan_perawatan_luka","tindakan_hygiene",
        "riwayat_alergi","jenis_alergi","reaksi_alergi",

        "alat_bantu_tanpa","alat_bantu_dengar","alat_bantu_kaca_mata","alat_bantu_gigi_palsu","alat_bantu_tongkat_kruk",
        "alat_bantu_lain","alat_bantu_lain_ket",

        "tindakan_yang_sudah_dilakukan",

        "penunjang_laboratorium_cek","penunjang_laboratorium_ket","penunjang_radiologi_cek","penunjang_radiologi_ket",
        "penunjang_ct_scan_cek","penunjang_ct_scan_ket","penunjang_lain_cek","penunjang_lain_ket",

        "rencana_tindak_lanjut","masalah_utama_keperawatan","dokumen_yang_diserahterimakan"
    };

    private static final String[] SECTION_TITLES = {
        "Ruangan, Dokter, dan Persetujuan",
        "Kondisi Pasien Saat Transfer",
        "Data Serah Terima",
        "Kebutuhan Khusus, Alergi, dan Alat Bantu",
        "Tindakan, Penunjang, dan Rencana"
    };

    private static final String[] SECTION_SUBTITLES = {
        "Ruang asal & tujuan, dokter penanggung jawab, cara pindah, dan persetujuan keluarga",
        "Satu kali observasi saat transfer (kesadaran, tanda vital, dan terapi oksigen)",
        "Line invasif, prosedur, kewaspadaan, nutrisi, eliminasi, aktifitas, mobilisasi, drainase, dan luka",
        "Kebutuhan restrain, resiko jatuh, alergi, dan alat bantu fungsional",
        "Tindakan terlaksana, penunjang penegakan, terapi, dan rencana lanjutan"
    };

    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private DlgCariPetugas petugas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private StringBuilder htmlContent;

    // ---------- Kerangka Utama ----------
    private widget.InternalFrame internalFrame1;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ScrollPane scrollInput;
    private JPanel FormInputContainer;
    private JPanel SectionsWrapper;
    private widget.Button BtnSimpan, BtnBatal, BtnHapus, BtnEdit, BtnPrint, BtnKeluar;
    private JLabel LblStatusBar;

    // ---------- Header Pasien & Input Header ----------
    private widget.TextBox TNoRw, TNoRM, TPasien, TglLahir, Jk;
    private PanelTanggalDanJam TanggalMasuk, TanggalPindah;
    private widget.TextBox DiagnosaUtama, DiagnosaSekunder;
    private widget.Button BtnDiagnosaUtama;
    private widget.ComboBox IndikasiPindah;
    private widget.TextBox KeteranganIndikasiPindahRuang;
    private widget.TextBox KdPetugasMenyerahkan, NmPetugasMenyerahkan, KdPetugasMenerima, NmPetugasMenerima;
    private widget.Button BtnPetugasMenyerahkan, BtnPetugasMenerima;

    // ---------- Sidebar Navigasi & Identitas ----------
    private JLabel LblAvatar, LblNmPasien, LblInfoPasien, LblNoRM, LblNoRw, LblLokasi;
    private JLabel LblProgressPct, LblProgressNote;
    private JPanel ProgressBarFill;
    private int activeSectionIndex = 0;
    private boolean isProgrammaticScroll = false;
    private boolean satuBagianMode = true;
    private ModeToggleButton rbSatuBagian, rbSemuaBagian;
    private ButtonGroup bgModeBagian;
    private final JPanel[] navItems = new JPanel[5];
    private final JPanel[] navAccents = new JPanel[5];
    private final JLabel[] navNomor = new JLabel[5];
    private final JLabel[] navTitles = new JLabel[5];
    private final JLabel[] navChecks = new JLabel[5];
    private final JPanel[] sectionCards = new JPanel[5];
    private final Component[] sectionStruts = new Component[4];

    // Alur Transfer Card in Sidebar
    private JLabel LblAlurAsal, LblAlurKelasAsal, LblAlurTujuan, LblAlurKelasTujuan, LblAlurCara;

    // ---------- Seksi A ----------
    private widget.ComboBox PilihRuangAsal, PilihRuangTujuan;
    private widget.TextBox AsalRuang, RuangSelanjutnya;
    private widget.TextBox KelasAsalRuang, KelasRuangSelanjutnya;
    private final ButtonGroup bgKelasAsal = new ButtonGroup();
    private final ButtonGroup bgKelasTujuan = new ButtonGroup();
    private widget.TextBox KdDokterMerawatAsal1, NmDokterMerawatAsal1, KdDokterMerawatAsal2, NmDokterMerawatAsal2, KdDokterMerawatAsal3, NmDokterMerawatAsal3;
    private widget.Button BtnDokterMerawatAsal1, BtnDokterMerawatAsal2, BtnDokterMerawatAsal3;
    private widget.TextBox KdDokterMerawatTujuan1, NmDokterMerawatTujuan1, KdDokterMerawatTujuan2, NmDokterMerawatTujuan2, KdDokterMerawatTujuan3, NmDokterMerawatTujuan3;
    private widget.Button BtnDokterMerawatTujuan1, BtnDokterMerawatTujuan2, BtnDokterMerawatTujuan3;
    private widget.ComboBox MetodePemindahan;
    private final ButtonGroup bgCaraPindah = new ButtonGroup();
    private widget.TextBox NamaMenyetujui, TNoTlpMenyetujui;
    private widget.ComboBox HubunganMenyetujui, MenyetujuiPemindahan, SudahDijelaskanDiagnosa;
    private final ButtonGroup bgDijelaskan = new ButtonGroup();

    // ---------- Seksi B ----------
    private PanelTanggalDanJam ObservasiTerakhirPukul;
    private widget.ComboBox KesadaranTransfer;
    private final ButtonGroup bgKesadaran = new ButtonGroup();
    private widget.TextBox TDTransfer, HRTransfer, RRTransfer, SuhuTransfer, GCSTransfer, SpO2Transfer;
    private widget.ComboBox TingkatNyeriTransfer;
    private final ButtonGroup bgNyeri = new ButtonGroup();
    private JLabel LblNyeriKet;

    // ---------- Seksi C ----------
    private widget.ComboBox OksigenStatus;
    private final ButtonGroup bgOksigenStatus = new ButtonGroup();
    private ChipCheckBox CekOksigenNasalKanule, CekOksigenNasalKateter, CekOksigenFaceMask, CekOksigenSungkupRebreathing,
        CekOksigenSungkupNonRebreathing, CekOksigenTracheostomy, CekOksigenEtt;

    // ---------- Seksi D ----------
    private widget.RadioButton RbLineTidakAda, RbLineCvpPicc, RbLineIvLine;
    private final ButtonGroup bgLineInvasif = new ButtonGroup();
    private PanelTanggalDanJam LineInvasifTanggal;
    private widget.TextArea ProsedurDilakukan;
    private ChipCheckBox CekKewaspadaanStandar, CekKewaspadaanKontak, CekKewaspadaanAirborne, CekKewaspadaanDroplet;
    private ChipCheckBox CekNutrisiPuasa, CekNutrisiOral, CekNutrisiNgt, CekNutrisiBatasanCairan, CekNutrisiDietKhusus;
    private widget.TextBox TNutrisiBatasanCairanCc, TNutrisiDietKhususJelaskan;
    private widget.RadioButton RbEliminasiBabNormal, RbEliminasiBabDiareKonstipasi, RbEliminasiBabIleustomyColostomy, RbEliminasiBabInkontinensiaAlvi;
    private final ButtonGroup bgEliminasiBab = new ButtonGroup();
    private widget.RadioButton RbEliminasiBakNormal, RbEliminasiBakInkontinensiaUrine, RbEliminasiBakKateterUrine;
    private final ButtonGroup bgEliminasiBak = new ButtonGroup();
    private widget.TextBox TEliminasiBakKateterNo;
    private PanelTanggalDanJam DTPEliminasiBakKateterTanggalPasang;

    // ---------- Seksi E ----------
    private widget.RadioButton RbAktifitasMandiri, RbAktifitasDibantuSebagian, RbAktifitasDibantuPenuh;
    private final ButtonGroup bgAktifitas = new ButtonGroup();
    private widget.RadioButton RbMobilisasiTirahBaring, RbMobilisasiDuduk, RbMobilisasiJalan;
    private final ButtonGroup bgMobilisasi = new ButtonGroup();
    private ChipCheckBox CekDrainaseWsd, CekDrainaseDrainLuka, CekDrainaseStoma, CekDrainaseLain;
    private widget.TextBox TDrainaseLainKet;

    // ---------- Seksi F ----------
    private ChipCheckBox CekLukaTidakAda;
    private ChipCheckBox CekLukaOperasi, CekLukaDekubitus, CekLukaSkinLost, CekLukaGangren, CekLukaLain;
    private widget.TextBox TLukaOperasiUkuran, TLukaOperasiLokasi, TLukaOperasiKondisi;
    private widget.TextBox TLukaDekubitusUkuran, TLukaDekubitusLokasi, TLukaDekubitusKondisi;
    private widget.TextBox TLukaSkinLostUkuran, TLukaSkinLostLokasi, TLukaSkinLostKondisi;
    private widget.TextBox TLukaGangrenUkuran, TLukaGangrenLokasi, TLukaGangrenKondisi;
    private widget.TextBox TLukaLainKet;

    // ---------- Seksi G ----------
    private ChipCheckBox CekTindakanResikoJatuh, CekTindakanPemasanganRestrain, CekTindakanPerawatanLuka, CekTindakanHygiene;
    private widget.ComboBox RiwayatAlergi;
    private final ButtonGroup bgAlergi = new ButtonGroup();
    private widget.TextBox TJenisAlergi, TReaksiAlergi;
    private ChipCheckBox CekAlatBantuTanpa, CekAlatBantuDengar, CekAlatBantuKacaMata, CekAlatBantuGigiPalsu, CekAlatBantuTongkatKruk, CekAlatBantuLain;
    private widget.TextBox TAlatBantuLainKet;

    // ---------- Seksi H ----------
    private widget.TextArea TindakanYangSudahDilakukan, ObatYangDiberikan, HasilInvestigasiAbnormal, RencanaTindakLanjut, MasalahUtamaKeperawatan, DokumenYangDiserahterimakan;
    private ChipCheckBox CekPenunjangLaboratorium, CekPenunjangRadiologi, CekPenunjangCtScan, CekPenunjangLain;
    private widget.TextBox TPenunjangLaboratoriumKet, TPenunjangRadiologiKet, TPenunjangCtScanKet, TPenunjangLainKet;

    // ---------- Tab Data Tersimpan & Bukti TTD ----------
    private widget.Table tbObat;
    private widget.Tanggal DTPCari1, DTPCari2;
    private widget.TextBox TCari;
    private widget.Button BtnCari, BtnAll, BtnPrint1, btnAmbil, BtnRefreshPhoto1;
    private widget.Label LCount;
    private widget.ScrollPane ScrollHTML;
    private widget.editorpane LoadHTML, LoadHTML2;
    private javax.swing.JTabbedPane TabRingkasanTtd;
    private widget.PanelBiasa PanelAccor, FormPhoto;
    private widget.CekBox ChkAccor;

    // Items Kamar
    private static final class KamarItem {
        final String label, kdKamar, kelas;
        KamarItem(String label, String kdKamar, String kelas){ this.label=label; this.kdKamar=kdKamar; this.kelas=kelas; }
        @Override public String toString(){ return label; }
    }

    public RMTransferPasienAntarRuang(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat","No.RM","Nama Pasien","Tgl.Lahir","J.K.","Tanggal Masuk","Tanggal Pindah","Diagnosa Utama",
            "NIP Menyerahkan","Petugas Yang Menyerahkan","NIP Menerima","Petugas Yang Menerima"
        }){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };

        initComponents();
        buatKotak(internalFrame1);
        widget.IconStyle.pasang(this);

        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] colWidths = {105, 70, 150, 65, 55, 115, 115, 160, 95, 150, 95, 150};
        for (int c = 0; c < colWidths.length; c++) {
            TableColumn col = tbObat.getColumnModel().getColumn(c);
            col.setPreferredWidth(colWidths[c]);
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        initDocumentLimits();
        initHtmlEditorKits();

        ChkAccor.setSelected(false);
        isPhoto();

        muatPilihanKamar(PilihRuangAsal);
        muatPilihanKamar(PilihRuangTujuan);

        setSize(1340, 780);
        setLocationRelativeTo(parent);

        pasangListeners();
        updateProgress();
        updateAlurDisplay();
    }

    private void initDocumentLimits() {
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        AsalRuang.setDocument(new batasInput((byte)30).getKata(AsalRuang));
        RuangSelanjutnya.setDocument(new batasInput((byte)30).getKata(RuangSelanjutnya));
        DiagnosaUtama.setDocument(new batasInput((int)100).getKata(DiagnosaUtama));
        DiagnosaSekunder.setDocument(new batasInput((int)100).getKata(DiagnosaSekunder));
        KeteranganIndikasiPindahRuang.setDocument(new batasInput((int)50).getKata(KeteranganIndikasiPindahRuang));
        ProsedurDilakukan.setDocument(new batasInput((int)800).getKata(ProsedurDilakukan));
        ObatYangDiberikan.setDocument(new batasInput((int)800).getKata(ObatYangDiberikan));
        NamaMenyetujui.setDocument(new batasInput((int)50).getKata(NamaMenyetujui));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
    }

    private void initHtmlEditorKits() {
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(false);
        LoadHTML.setEditorKit(kit);
        LoadHTML2.setEditable(false);
        LoadHTML2.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
            ".isi td{border-right: 1px solid #e2e7dd;font: 9px tahoma;height:14px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
            ".isi2 td{font: 9px tahoma;border:none;height:14px;background: #ffffff;color:#323232;}"+
            ".isi3 td{border-right: 1px solid #e2e7dd;font: 9px tahoma;height:14px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
            ".isi4 td{font: 11px tahoma;height:14px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
        );
        LoadHTML.setDocument(kit.createDefaultDocument());
        LoadHTML2.setDocument(kit.createDefaultDocument());
    }

    // =================================================================
    //  KOMPONEN KUSTOM: PANEL TANGGAL & JAM, RADIO PILL, CHIP CHECKBOX
    // =================================================================

    /**
     * Komponen pemisah tanggal dan jam standar IGD SIMRS Khanza.
     * Menggabungkan date picker kalender (dd-MM-yyyy) dengan 3 combo box
     * waktu (jam, menit, detik) dan checkbox timer real-time otomatis.
     */
    public static class PanelTanggalDanJam extends widget.PanelBiasa {
        public final widget.Tanggal tanggal;
        public final widget.ComboBox cmbJam;
        public final widget.ComboBox cmbMenit;
        public final widget.ComboBox cmbDetik;
        public final widget.CekBox chkJln;
        private javax.swing.Timer timer;

        public PanelTanggalDanJam(boolean adaTimer, int tinggi) {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
            setBorder(null);

            tanggal = new widget.Tanggal();
            tanggal.setDisplayFormat("dd-MM-yyyy");
            tanggal.setPreferredSize(new Dimension(102, tinggi));
            tanggal.setBackground(Color.WHITE);
            tanggal.setOpaque(true);
            tanggal.setBorder(BorderFactory.createLineBorder(new Color(0xD8, 0xDB, 0xD6)));
            tanggal.putClientProperty("Component.arc", 0);
            tanggal.putClientProperty("TextComponent.arc", 0);
            tanggal.putClientProperty("JComponent.roundRect", false);

            String[] arrJam = new String[24];
            for (int i = 0; i < 24; i++) {
                arrJam[i] = (i < 10 ? "0" : "") + i;
            }
            cmbJam = new widget.ComboBox();
            cmbJam.setModel(new javax.swing.DefaultComboBoxModel(arrJam));
            cmbJam.setPreferredSize(new Dimension(62, tinggi));
            cmbJam.setBackground(Color.WHITE);
            cmbJam.setBorder(BorderFactory.createLineBorder(new Color(0xD8, 0xDB, 0xD6)));
            cmbJam.putClientProperty("Component.arc", 0);

            String[] arrMnt = new String[60];
            for (int i = 0; i < 60; i++) {
                arrMnt[i] = (i < 10 ? "0" : "") + i;
            }
            cmbMenit = new widget.ComboBox();
            cmbMenit.setModel(new javax.swing.DefaultComboBoxModel(arrMnt));
            cmbMenit.setPreferredSize(new Dimension(62, tinggi));
            cmbMenit.setBackground(Color.WHITE);
            cmbMenit.setBorder(BorderFactory.createLineBorder(new Color(0xD8, 0xDB, 0xD6)));
            cmbMenit.putClientProperty("Component.arc", 0);

            cmbDetik = new widget.ComboBox();
            cmbDetik.setModel(new javax.swing.DefaultComboBoxModel(arrMnt));
            cmbDetik.setPreferredSize(new Dimension(62, tinggi));
            cmbDetik.setBackground(Color.WHITE);
            cmbDetik.setBorder(BorderFactory.createLineBorder(new Color(0xD8, 0xDB, 0xD6)));
            cmbDetik.putClientProperty("Component.arc", 0);

            add(tanggal);
            add(cmbJam);
            JLabel sep1 = new JLabel(":");
            sep1.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));
            sep1.setPreferredSize(new Dimension(4, tinggi));
            sep1.setHorizontalAlignment(SwingConstants.CENTER);
            add(sep1);
            add(cmbMenit);
            JLabel sep2 = new JLabel(":");
            sep2.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));
            sep2.setPreferredSize(new Dimension(4, tinggi));
            sep2.setHorizontalAlignment(SwingConstants.CENTER);
            add(sep2);
            add(cmbDetik);

            if (adaTimer) {
                chkJln = new widget.CekBox();
                chkJln.setSelected(true);
                chkJln.setToolTipText("Centang agar tanggal & jam berjalan otomatis. Hapus centang untuk ubah tanggal/jam manual");
                chkJln.setPreferredSize(new Dimension(28, tinggi));
                add(chkJln);

                timer = new javax.swing.Timer(1000, e -> {
                    if (chkJln.isSelected()) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        tanggal.setDate(cal.getTime());
                        cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
                        cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
                        cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
                    }
                });
                timer.start();

                chkJln.addItemListener(e -> {
                    if (chkJln.isSelected()) {
                        setSekarang();
                    }
                });
            } else {
                chkJln = null;
            }

            setSekarang();
            setMinimumSize(getPreferredSize());
        }

        public void setSekarang() {
            tanggal.setDate(new Date());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
            cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
            cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
            if (chkJln != null) {
                chkJln.setSelected(true);
            }
        }

        public void setDate(Date d) {
            if (d == null) {
                setSekarang();
                return;
            }
            tanggal.setDate(d);
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(d);
            cmbJam.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY)));
            cmbMenit.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.MINUTE)));
            cmbDetik.setSelectedItem(String.format("%02d", cal.get(java.util.Calendar.SECOND)));
            if (chkJln != null) {
                chkJln.setSelected(false);
            }
        }

        public Date getDate() {
            return tanggal.getDate();
        }

        public void setNilaiSql(String tglJamSql) {
            if (tglJamSql == null || tglJamSql.trim().isEmpty()) {
                setSekarang();
                return;
            }
            try {
                String bersih = tglJamSql.replaceAll("'", "").trim();
                if (bersih.length() >= 10) {
                    java.text.SimpleDateFormat sdfTgl = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    tanggal.setDate(sdfTgl.parse(bersih.substring(0, 10)));
                }
                if (bersih.length() >= 19) {
                    cmbJam.setSelectedItem(bersih.substring(11, 13));
                    cmbMenit.setSelectedItem(bersih.substring(14, 16));
                    cmbDetik.setSelectedItem(bersih.substring(17, 19));
                } else if (bersih.length() >= 16) {
                    cmbJam.setSelectedItem(bersih.substring(11, 13));
                    cmbMenit.setSelectedItem(bersih.substring(14, 16));
                    cmbDetik.setSelectedItem("00");
                }
                if (chkJln != null) {
                    chkJln.setSelected(false);
                }
            } catch (Exception ex) {
                setSekarang();
            }
        }

        public String getTanggalSql(fungsi.validasi valid) {
            return valid.SetTgl(tanggal.getSelectedItem() + "");
        }

        public String getJamSql() {
            return cmbJam.getSelectedItem() + ":" + cmbMenit.getSelectedItem() + ":" + cmbDetik.getSelectedItem();
        }

        public String getTanggalDanJamSql(fungsi.validasi valid) {
            return getTanggalSql(valid) + " " + getJamSql();
        }

        public Object getSelectedItem() {
            return (tanggal.getSelectedItem() != null ? tanggal.getSelectedItem().toString() : "") + " " + getJamSql();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (tanggal != null) tanggal.setEnabled(enabled);
            if (cmbJam != null) cmbJam.setEnabled(enabled);
            if (cmbMenit != null) cmbMenit.setEnabled(enabled);
            if (cmbDetik != null) cmbDetik.setEnabled(enabled);
            if (chkJln != null) chkJln.setEnabled(enabled);
        }

        public void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
        }
    }

    public static class RadioPill extends widget.RadioButton {
        public RadioPill(String text, ButtonGroup group) {
            super();
            setText(text);
            if (group != null) group.add(this);
            setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(5, 10, 5, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (isSelected()) {
                g2.setColor(TEAL_LIGHT);
                g2.fillRect(1, 1, w - 2, h - 2);
                g2.setColor(TEAL_ACCENT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRect(1, 1, w - 2, h - 2);
            } else {
                g2.setColor(CARD_BG);
                g2.fillRect(1, 1, w - 2, h - 2);
                g2.setColor(BORDER_GRAY);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRect(1, 1, w - 2, h - 2);
            }

            int dotSize = 13;
            int dotX = 8;
            int dotY = (h - dotSize) / 2;

            if (isSelected()) {
                g2.setColor(TEAL_ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(dotX, dotY, dotSize, dotSize);
                g2.fillOval(dotX + 3, dotY + 3, dotSize - 6, dotSize - 6);
            } else {
                g2.setColor(new Color(0xb3, 0xb8, 0xb3));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(dotX, dotY, dotSize, dotSize);
            }

            g2.setColor(TEXT_DARK);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = dotX + dotSize + 6;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = 8 + 13 + 6 + fm.stringWidth(getText()) + 10;
            return new Dimension(w, 28);
        }
    }

    public static class ChipCheckBox extends widget.CekBox {
        private boolean plainMode = false;

        public ChipCheckBox(String text) {
            super();
            setText(text);
            setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(5, 9, 5, 9));
        }

        public void setPlainMode(boolean plain) {
            this.plainMode = plain;
            setBorder(plain ? new EmptyBorder(2, 4, 2, 4) : new EmptyBorder(5, 9, 5, 9));
            repaint();
        }

        public boolean isPlainMode() {
            return plainMode;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (!plainMode) {
                if (isSelected()) {
                    g2.setColor(CHIP_ON_BG);
                    g2.fillRect(1, 1, w - 2, h - 2);
                    g2.setColor(TEAL_ACCENT);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRect(1, 1, w - 2, h - 2);
                } else {
                    g2.setColor(CARD_BG);
                    g2.fillRect(1, 1, w - 2, h - 2);
                    g2.setColor(BORDER_GRAY);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRect(1, 1, w - 2, h - 2);
                }
            }

            int iconSize = 13;
            int iconX = plainMode ? 4 : 8;
            int iconY = (h - iconSize) / 2;

            if (isSelected()) {
                g2.setColor(isEnabled() ? TEAL_ACCENT : new Color(0x94, 0xa3, 0xb8));
                g2.fillRect(iconX, iconY, iconSize, iconSize);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(iconX + 3, iconY + 6, iconX + 5, iconY + 9);
                g2.drawLine(iconX + 5, iconY + 9, iconX + 10, iconY + 4);
                g2.setColor(isEnabled() ? TEAL_ACCENT : new Color(0x94, 0xa3, 0xb8));
            } else {
                g2.setColor(isEnabled() ? new Color(0xb3, 0xb8, 0xb3) : new Color(0xcc, 0xd2, 0xd9));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRect(iconX, iconY, iconSize, iconSize);
                g2.setColor(isEnabled() ? TEXT_LABEL : new Color(0x94, 0xa3, 0xb8));
            }

            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = iconX + iconSize + 6;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.setColor(isEnabled() ? (isSelected() && !plainMode ? TEAL_ACCENT : TEXT_LABEL) : new Color(0x94, 0xa3, 0xb8));
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int pad = plainMode ? 4 : 8;
            int w = pad + 13 + 6 + fm.stringWidth(getText()) + (plainMode ? 6 : 10);
            return new Dimension(w, plainMode ? 24 : 28);
        }
    }

    // =================================================================
    //  STANDAR UI FORM KOTAK (FULL SIKU-SIKU / FLATLAF TOKEN)
    // =================================================================

    private static final Color WARNA_BORDER_KOTAK = new Color(0xD8, 0xDB, 0xD6);
    private static final javax.swing.border.Border BORDER_KOTAK_TEXTFIELD = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(WARNA_BORDER_KOTAK, 1),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)
    );
    private static final javax.swing.border.Border BORDER_KOTAK_GARIS = BorderFactory.createLineBorder(WARNA_BORDER_KOTAK, 1);

    /**
     * Memastikan seluruh field input, lookup, combo, tanggal, dan tombol berujung
     * siku-siku datar (arc=0, roundRect=false) sesuai standar form IGD/Ranap.
     */
    private static void buatKotak(Component c) {
        if (c == null) {
            return;
        }
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            jc.putClientProperty("Component.arc", 0);
            jc.putClientProperty("Button.arc", 0);
            jc.putClientProperty("TextComponent.arc", 0);
            jc.putClientProperty("JTextField.arc", 0);
            jc.putClientProperty("ComboBox.arc", 0);
            jc.putClientProperty("JComponent.roundRect", false);

            if (c instanceof javax.swing.text.JTextComponent) {
                if (!(c instanceof javax.swing.JTextArea)) {
                    if (!Boolean.TRUE.equals(jc.getClientProperty("lookupField"))) {
                        jc.setBorder(BORDER_KOTAK_TEXTFIELD);
                    }
                } else {
                    jc.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                }
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                    "background: #FFFFFF; inactiveBackground: #FFFFFF; disabledBackground: #FFFFFF;");
            } else if (c instanceof javax.swing.JComboBox) {
                jc.setBorder(BORDER_KOTAK_GARIS);
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style",
                    "background: #FFFFFF; disabledBackground: #FFFFFF; buttonBackground: #FFFFFF; editableBackground: #FFFFFF;");
            } else if (c instanceof javax.swing.JScrollPane) {
                jc.setBorder(BORDER_KOTAK_GARIS);
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
            } else if (c instanceof uz.ncipro.calendar.JDateTimePicker) {
                jc.setBorder(BORDER_KOTAK_GARIS);
                jc.setBackground(Color.WHITE);
                jc.setOpaque(true);
                jc.putClientProperty("FlatLaf.style", "background: #FFFFFF; buttonBackground: #FFFFFF;");
            } else if (c instanceof javax.swing.AbstractButton) {
                if (c instanceof widget.Button) {
                    ((widget.Button) c).setRoundRect(false);
                }
            }
        }
        if (c instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) c).getComponents()) {
                buatKotak(child);
            }
        }
    }

    // =================================================================
    //  INISIALISASI & PERAKITAN TAMPILAN
    // =================================================================

    private void initComponents() {
        // Inisialisasi widget identitas pasien standar SIMRS
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        TglLahir = new widget.TextBox();
        Jk = new widget.TextBox();

        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setLayout(new BorderLayout());

        // 1. Top Bar Teal 34px
        JPanel topBar = rakitTopBar();
        internalFrame1.add(topBar, BorderLayout.NORTH);

        // 2. Tab Rawat
        TabRawat = new javax.swing.JTabbedPane();
        TabRawat.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));

        // Tab 1: Formulir Transfer
        JPanel tabForm = new JPanel(new BorderLayout());
        JPanel headerFieldsPanel = rakitHeaderFields();
        tabForm.add(headerFieldsPanel, BorderLayout.NORTH);

        JPanel splitBody = rakitSplitBody();
        tabForm.add(splitBody, BorderLayout.CENTER);
        TabRawat.addTab("Formulir Transfer (RM 04)", tabForm);

        // Tab 2: Data Transfer Pasien
        JPanel tabData = rakitTabData();
        TabRawat.addTab("Data Transfer Pasien", tabData);

        internalFrame1.add(TabRawat, BorderLayout.CENTER);

        // 3. Action Bar Bawah 56px
        JPanel actionBar = rakitActionBar();
        internalFrame1.add(actionBar, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    private JPanel rakitTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(100, 34));
        topBar.setBackground(TEAL_HEADER);
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Transfer Pasien Antar Ruangan");
        lblTitle.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));
        lblTitle.setForeground(new Color(0xdf, 0xe9, 0xe6));
        left.add(lblTitle);

        JLabel lblBadge = new JLabel(" RM 04-1 / 04-2 ");
        lblBadge.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblBadge.setForeground(new Color(0x8f, 0xb5, 0xae));
        lblBadge.setBorder(new LineBorder(new Color(0x2c, 0x5a, 0x56), 1, false));
        left.add(lblBadge);

        topBar.add(left, BorderLayout.WEST);

        JLabel lblInstansi = new JLabel("RSUD Demang Sepulau Raya");
        lblInstansi.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblInstansi.setForeground(new Color(0x8f, 0xb5, 0xae));
        lblInstansi.setBorder(new EmptyBorder(8, 0, 0, 0));
        topBar.add(lblInstansi, BorderLayout.EAST);

        return topBar;
    }

    private JPanel rakitCardPasien() {
        JPanel cardPasien = new JPanel();
        cardPasien.setLayout(new BoxLayout(cardPasien, BoxLayout.Y_AXIS));
        cardPasien.setBackground(PATIENT_CARD_BG);
        cardPasien.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xe0, 0xe2, 0xdd), 1, false),
            new EmptyBorder(8, 12, 8, 12)
        ));
        cardPasien.setPreferredSize(new Dimension(320, 154));
        cardPasien.setMinimumSize(new Dimension(310, 150));
        cardPasien.setMaximumSize(new Dimension(350, 180));

        JPanel headPasien = new JPanel(new BorderLayout());
        headPasien.setOpaque(false);
        headPasien.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblHeadP = new JLabel("IDENTITAS PASIEN");
        lblHeadP.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblHeadP.setForeground(TEXT_LABEL_MUTED);
        headPasien.add(lblHeadP, BorderLayout.WEST);

        JLabel badgeKunci = new JLabel("TERKUNCI");
        badgeKunci.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 9));
        badgeKunci.setForeground(new Color(0x7b, 0x82, 0x7d));
        badgeKunci.setBackground(new Color(0xe8, 0xea, 0xe5));
        badgeKunci.setOpaque(true);
        badgeKunci.setBorder(new EmptyBorder(2, 6, 2, 6));
        headPasien.add(badgeKunci, BorderLayout.EAST);
        cardPasien.add(headPasien);
        cardPasien.add(Box.createVerticalStrut(5));

        JPanel pAvatar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pAvatar.setOpaque(false);
        pAvatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        LblAvatar = new JLabel("PS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xe3, 0xec, 0xe9));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(TEAL_ACCENT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        LblAvatar.setPreferredSize(new Dimension(38, 38));
        LblAvatar.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 14));
        LblAvatar.setForeground(TEAL_ACCENT);
        pAvatar.add(LblAvatar);

        JPanel pNama = new JPanel();
        pNama.setLayout(new BoxLayout(pNama, BoxLayout.Y_AXIS));
        pNama.setOpaque(false);
        LblNmPasien = new JLabel("-");
        LblNmPasien.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));
        LblNmPasien.setForeground(TEXT_DARK);
        pNama.add(LblNmPasien);

        LblInfoPasien = new JLabel("- \u00B7 -");
        LblInfoPasien.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        LblInfoPasien.setForeground(new Color(0x6c, 0x73, 0x6e));
        pNama.add(LblInfoPasien);

        pAvatar.add(pNama);
        cardPasien.add(pAvatar);
        cardPasien.add(Box.createVerticalStrut(5));

        JPanel pDet = new JPanel(new GridLayout(3, 1, 0, 2));
        pDet.setOpaque(false);
        pDet.setAlignmentX(Component.LEFT_ALIGNMENT);
        pDet.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));
        pDet.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(0xe4, 0xe6, 0xe2)),
            new EmptyBorder(4, 0, 2, 0)
        ));

        LblNoRM = new JLabel("No. RM: -");
        LblNoRM.setFont(new Font("Monospaced", Font.PLAIN, 11));
        LblNoRM.setForeground(new Color(0x5d, 0x64, 0x5f));
        pDet.add(LblNoRM);

        LblNoRw = new JLabel("No. Rawat: -");
        LblNoRw.setFont(new Font("Monospaced", Font.PLAIN, 11));
        LblNoRw.setForeground(new Color(0x5d, 0x64, 0x5f));
        pDet.add(LblNoRw);

        LblLokasi = new JLabel("Lokasi: -");
        LblLokasi.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        LblLokasi.setForeground(new Color(0x5d, 0x64, 0x5f));
        pDet.add(LblLokasi);

        cardPasien.add(pDet);
        cardPasien.add(Box.createVerticalGlue());
        return cardPasien;
    }

    private JPanel hdrCell(String label, Component field, boolean wajib) {
        JPanel sel = new JPanel(new BorderLayout(0, 3));
        sel.setOpaque(false);
        sel.setBorder(null);

        JLabel l = new JLabel(wajib ? "<html>" + label + " <font color='#B3261E'>*</font></html>" : label);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        l.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        l.setForeground(TEXT_LABEL_MUTED);
        sel.add(l, BorderLayout.NORTH);

        if (!(field instanceof PanelTanggalDanJam)) {
            int lebarField = (field.getPreferredSize() != null && field.getPreferredSize().width > 0)
                    ? field.getPreferredSize().width : 0;
            field.setPreferredSize(new Dimension(lebarField, 26));
        }
        sel.add(field, BorderLayout.CENTER);
        return sel;
    }

    private JPanel hdrLookup(widget.TextBox tf, widget.Button btn, String tooltip) {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(true);
        p.setBackground(Color.WHITE);
        p.setBorder(BORDER_KOTAK_GARIS);
        p.setPreferredSize(new Dimension(160, 26));

        tf.setPreferredSize(new Dimension(130, 26));
        tf.setBackground(Color.WHITE);
        tf.setOpaque(true);
        tf.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 4));
        tf.putClientProperty("lookupField", Boolean.TRUE);
        tf.putClientProperty("Component.arc", 0);
        tf.putClientProperty("TextComponent.arc", 0);
        tf.putClientProperty("JTextField.arc", 0);
        tf.putClientProperty("JComponent.roundRect", false);

        btn.setPreferredSize(new Dimension(28, 26));
        btn.setBackground(Color.WHITE);
        btn.setGlassColor(Color.WHITE);
        btn.setRoundRect(false);
        btn.putClientProperty("Component.arc", 0);
        Icon iconLookup = widget.IconStyle.muat("search.svg", 14, widget.IconStyle.ABU);
        btn.setIcon(iconLookup != null ? iconLookup : new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, WARNA_BORDER_KOTAK));

        p.add(tf, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    private JPanel rakitHeaderFields() {
        JPanel banner = new JPanel(new BorderLayout(16, 0));
        banner.setBackground(CARD_BG);
        banner.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_GRAY),
            new EmptyBorder(8, 16, 8, 16)
        ));

        // 1. Kartu Identitas Pasien (Kiri)
        JPanel cardPasien = rakitCardPasien();
        banner.add(cardPasien, BorderLayout.WEST);

        // 2. Grid Fields (Kanan)
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        // Inisialisasi widget header (tanggal dan jam terpisah standar IGD)
        TanggalMasuk = new PanelTanggalDanJam(true, 26);
        TanggalPindah = new PanelTanggalDanJam(true, 26);

        DiagnosaUtama = new widget.TextBox();
        DiagnosaUtama.setEditable(false);

        BtnDiagnosaUtama = new widget.Button();
        BtnDiagnosaUtama.addActionListener(e -> {
            laporan.DlgCariPenyakit penyakit = new laporan.DlgCariPenyakit(null, false);
            penyakit.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent ev) {
                    if (penyakit.getTable().getSelectedRow() != -1) {
                        DiagnosaUtama.setText(
                            penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(), 0).toString() + " - " +
                            penyakit.getTable().getValueAt(penyakit.getTable().getSelectedRow(), 1).toString());
                        updateProgress();
                    }
                }
            });
            penyakit.isCek();
            penyakit.emptTeks();
            penyakit.setSize(internalFrame1.getWidth() - 40, internalFrame1.getHeight() - 40);
            penyakit.setLocationRelativeTo(internalFrame1);
            penyakit.setVisible(true);
        });

        DiagnosaSekunder = new widget.TextBox();
        DiagnosaSekunder.setPreferredSize(new Dimension(140, 26));

        KdPetugasMenyerahkan = new widget.TextBox();
        KdPetugasMenyerahkan.setVisible(false);
        NmPetugasMenyerahkan = new widget.TextBox();
        NmPetugasMenyerahkan.setEditable(false);
        BtnPetugasMenyerahkan = new widget.Button();
        BtnPetugasMenyerahkan.addActionListener(e -> {
            if (petugas == null) petugas = new DlgCariPetugas(null, false);
            petugas.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent ev) {
                    if (petugas.getTable().getSelectedRow() != -1) {
                        KdPetugasMenyerahkan.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NmPetugasMenyerahkan.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                        NmPetugasMenyerahkan.setToolTipText(KdPetugasMenyerahkan.getText() + " - " + NmPetugasMenyerahkan.getText());
                        updateProgress();
                    }
                }
            });
            petugas.isCek();
            petugas.emptTeks();
            petugas.setSize(internalFrame1.getWidth() - 40, internalFrame1.getHeight() - 40);
            petugas.setLocationRelativeTo(internalFrame1);
            petugas.setVisible(true);
        });

        KdPetugasMenerima = new widget.TextBox();
        KdPetugasMenerima.setVisible(false);
        NmPetugasMenerima = new widget.TextBox();
        NmPetugasMenerima.setEditable(false);
        BtnPetugasMenerima = new widget.Button();
        BtnPetugasMenerima.addActionListener(e -> {
            if (petugas == null) petugas = new DlgCariPetugas(null, false);
            petugas.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent ev) {
                    if (petugas.getTable().getSelectedRow() != -1) {
                        KdPetugasMenerima.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                        NmPetugasMenerima.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                        NmPetugasMenerima.setToolTipText(KdPetugasMenerima.getText() + " - " + NmPetugasMenerima.getText());
                        updateProgress();
                    }
                }
            });
            petugas.isCek();
            petugas.emptTeks();
            petugas.setSize(internalFrame1.getWidth() - 40, internalFrame1.getHeight() - 40);
            petugas.setLocationRelativeTo(internalFrame1);
            petugas.setVisible(true);
        });

        IndikasiPindah = new widget.ComboBox();
        IndikasiPindah.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Kondisi Pasien Memburuk","Kondisi Pasien Membaik","Fasilitas Kurang Memadai","Lain-lain","-"
        }));
        IndikasiPindah.setPreferredSize(new Dimension(140, 26));

        KeteranganIndikasiPindahRuang = new widget.TextBox();
        KeteranganIndikasiPindahRuang.setPreferredSize(new Dimension(140, 26));

        // Layout GridBag 4 kolom x 2 baris (sesuai standar form IGD)
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        g.weightx = 0.25;
        g.weighty = 0;

        // Baris 1: Tgl Masuk, Petugas Pengirim, Alasan Pindah, Diagnosa Utama
        g.gridy = 0;
        g.gridx = 0; g.insets = new Insets(0, 0, 8, 12); p.add(hdrCell("TGL. MASUK", TanggalMasuk, true), g);
        g.gridx = 1; g.insets = new Insets(0, 0, 8, 12); p.add(hdrCell("PETUGAS PENGIRIM", hdrLookup(NmPetugasMenyerahkan, BtnPetugasMenyerahkan, "Cari Petugas Pengirim"), true), g);
        g.gridx = 2; g.insets = new Insets(0, 0, 8, 12); p.add(hdrCell("ALASAN PINDAH", IndikasiPindah, true), g);
        g.gridx = 3; g.insets = new Insets(0, 0, 8, 0);  p.add(hdrCell("DIAGNOSA UTAMA (ICD)", hdrLookup(DiagnosaUtama, BtnDiagnosaUtama, "Cari ICD-9/10"), false), g);

        // Baris 2: Tgl Pindah, Petugas Penerima, Keterangan Alasan, Diagnosa Sekunder
        g.gridy = 1;
        g.gridx = 0; g.insets = new Insets(0, 0, 0, 12); p.add(hdrCell("TGL. PINDAH", TanggalPindah, true), g);
        g.gridx = 1; g.insets = new Insets(0, 0, 0, 12); p.add(hdrCell("PETUGAS PENERIMA", hdrLookup(NmPetugasMenerima, BtnPetugasMenerima, "Cari Petugas Penerima"), true), g);
        g.gridx = 2; g.insets = new Insets(0, 0, 0, 12); p.add(hdrCell("KETERANGAN ALASAN", KeteranganIndikasiPindahRuang, false), g);
        g.gridx = 3; g.insets = new Insets(0, 0, 0, 0);  p.add(hdrCell("DIAGNOSA SEKUNDER", DiagnosaSekunder, false), g);

        g.gridy = 2; g.gridx = 0; g.gridwidth = 4; g.weighty = 1.0; g.fill = GridBagConstraints.VERTICAL;
        p.add(Box.createVerticalGlue(), g);

        banner.add(p, BorderLayout.CENTER);
        return banner;
    }

    private JLabel labelHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lbl.setForeground(TEXT_LABEL_MUTED);
        return lbl;
    }

    private JPanel buatHeaderKolomTabel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0xF1, 0xF5, 0xF9));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xCB, 0xD5, 0xE1)),
            new EmptyBorder(5, 8, 5, 8)
        ));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lbl.setForeground(new Color(0x47, 0x55, 0x69));
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    private JPanel rakitSplitBody() {
        JPanel split = new JPanel(new BorderLayout(16, 0));
        split.setBackground(BG_PAGE);
        split.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Sidebar Kiri (300px)
        JPanel sidebar = rakitSidebar();
        split.add(sidebar, BorderLayout.WEST);

        // Konten Utama Scrollable (8 Kartu A s/d H)
        SectionsWrapper = new JPanel();
        SectionsWrapper.setLayout(new BoxLayout(SectionsWrapper, BoxLayout.Y_AXIS));
        SectionsWrapper.setBackground(BG_PAGE);

        rakitSeksiA();
        rakitSeksiB();
        rakitSeksiC();
        rakitSeksiD();
        rakitSeksiE();

        scrollInput = new widget.ScrollPane();
        scrollInput.setBorder(null);
        scrollInput.setViewportView(SectionsWrapper);
        scrollInput.getVerticalScrollBar().setUnitIncrement(18);
        scrollInput.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!satuBagianMode && !isProgrammaticScroll && sectionCards[0] != null) {
                int scrollY = scrollInput.getVerticalScrollBar().getValue() + 80;
                int current = 0;
                for (int i = 0; i < sectionCards.length; i++) {
                    if (sectionCards[i] != null && sectionCards[i].isVisible() && sectionCards[i].getY() <= scrollY) {
                        current = i;
                    }
                }
                if (current != activeSectionIndex) {
                    activeSectionIndex = current;
                    updateNavHighlight();
                }
            }
        });

        split.add(scrollInput, BorderLayout.CENTER);

        // Pasang scroll tembus ke semua komponen (textarea, textbox, combobox) agar scroll halaman utama tidak pernah macet
        pasangScrollTembusSemuaKomponen(SectionsWrapper);

        // Terapkan mode awal (Satu bagian per layar sesuai Form IGD)
        terapkanModeHalaman();
        scrollToSection(0);

        return split;
    }

    private JPanel rakitSidebar() {
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setPreferredSize(new Dimension(300, 600));
        sidebar.setMinimumSize(new Dimension(300, 550));
        sidebar.setMaximumSize(new Dimension(300, Short.MAX_VALUE));
        sidebar.setBackground(BG_PAGE);

        GridBagConstraints gs = new GridBagConstraints();
        gs.gridx = 0;
        gs.weightx = 1.0;
        gs.fill = GridBagConstraints.HORIZONTAL;
        gs.anchor = GridBagConstraints.NORTH;

        // 1. Kelengkapan Formulir Card
        JPanel cardProgress = new JPanel();
        cardProgress.setLayout(new BoxLayout(cardProgress, BoxLayout.Y_AXIS));
        cardProgress.setBackground(CARD_BG);
        cardProgress.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CARD_BORDER, 1, false),
            new EmptyBorder(12, 14, 12, 14)
        ));
        cardProgress.setPreferredSize(new Dimension(300, 95));
        cardProgress.setMinimumSize(new Dimension(300, 95));
        cardProgress.setMaximumSize(new Dimension(300, 95));

        JPanel headProg = new JPanel(new BorderLayout());
        headProg.setOpaque(false);
        headProg.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        JLabel lblProgT = new JLabel("KELENGKAPAN FORMULIR");
        lblProgT.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblProgT.setForeground(TEXT_LABEL_MUTED);
        headProg.add(lblProgT, BorderLayout.WEST);

        LblProgressPct = new JLabel("0%");
        LblProgressPct.setFont(new Font("Monospaced", Font.BOLD, 14));
        LblProgressPct.setForeground(TEAL_ACCENT);
        headProg.add(LblProgressPct, BorderLayout.EAST);
        cardProgress.add(headProg);
        cardProgress.add(Box.createVerticalStrut(6));

        JPanel track = new JPanel(null);
        track.setPreferredSize(new Dimension(272, 6));
        track.setMaximumSize(new Dimension(Short.MAX_VALUE, 6));
        track.setBackground(new Color(0xe6, 0xe8, 0xe3));

        ProgressBarFill = new JPanel();
        ProgressBarFill.setBackground(TEAL_ACCENT);
        ProgressBarFill.setBounds(0, 0, 0, 6);
        track.add(ProgressBarFill);
        cardProgress.add(track);
        cardProgress.add(Box.createVerticalStrut(6));

        LblProgressNote = new JLabel("0 dari 12 isian penting terisi");
        LblProgressNote.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        LblProgressNote.setForeground(TEXT_MUTED);
        cardProgress.add(LblProgressNote);

        gs.gridy = 0; gs.insets = new Insets(0, 0, 10, 0);
        sidebar.add(cardProgress, gs);

        // 2. Daftar Bagian Navigasi Card (01 s/d 08) - Format & Gaya Konsisten Form IGD
        JPanel cardNav = new JPanel();
        cardNav.setLayout(new BoxLayout(cardNav, BoxLayout.Y_AXIS));
        cardNav.setBackground(CARD_BG);
        cardNav.setBorder(new LineBorder(CARD_BORDER, 1, false));
        cardNav.setPreferredSize(new Dimension(300, 280));
        cardNav.setMinimumSize(new Dimension(300, 280));
        cardNav.setMaximumSize(new Dimension(300, 280));

        JPanel headNav = new JPanel(new BorderLayout());
        headNav.setBackground(CARD_HEADER_BG);
        headNav.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(0xec, 0xee, 0xea)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        headNav.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        JLabel lblNavHead = new JLabel("DAFTAR BAGIAN");
        lblNavHead.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblNavHead.setForeground(TEXT_LABEL_MUTED);
        headNav.add(lblNavHead, BorderLayout.WEST);
        cardNav.add(headNav);

        for (int idx = 0; idx < SECTION_TITLES.length; idx++) {
            final int fIdx = idx;
            JPanel baris = new JPanel(new BorderLayout(0, 0));
            baris.setBackground(idx == 0 ? TEAL_LIGHT : CARD_BG);
            baris.setBorder(BorderFactory.createMatteBorder(0, 0, idx < SECTION_TITLES.length - 1 ? 1 : 0, 0, new Color(0xf2, 0xf4, 0xf0)));
            baris.setCursor(new Cursor(Cursor.HAND_CURSOR));
            baris.setPreferredSize(new Dimension(300, 29));
            baris.setMaximumSize(new Dimension(Short.MAX_VALUE, 29));

            // Batang aksen kiri 3px konsisten Form IGD
            JPanel aksen = new JPanel();
            aksen.setBackground(idx == 0 ? TEAL_ACCENT : CARD_BG);
            aksen.setPreferredSize(new Dimension(3, 10));
            navAccents[idx] = aksen;
            baris.add(aksen, BorderLayout.WEST);

            JPanel isi = new JPanel(new BorderLayout(8, 0));
            isi.setOpaque(false);
            isi.setBorder(new EmptyBorder(5, 10, 5, 12));

            // Penomoran 2-digit "01", "02", dst konsisten Form IGD
            String noStr = String.format("%02d", idx + 1);
            JLabel lblNomor = new JLabel(noStr);
            lblNomor.setFont(new Font("Monospaced", Font.BOLD, 11));
            lblNomor.setForeground(idx == 0 ? TEAL_ACCENT : TEXT_LABEL_MUTED);
            lblNomor.setPreferredSize(new Dimension(24, 18));
            navNomor[idx] = lblNomor;
            isi.add(lblNomor, BorderLayout.WEST);

            JLabel lblT = new JLabel(SECTION_TITLES[idx]);
            lblT.setFont(new Font(ThemeConfig.FONT_FAMILY, idx == 0 ? Font.BOLD : Font.PLAIN, 11));
            lblT.setForeground(idx == 0 ? TEAL_ACCENT : TEXT_DARK);
            navTitles[idx] = lblT;
            isi.add(lblT, BorderLayout.CENTER);

            JLabel lblCheck = new JLabel("\u2022");
            lblCheck.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 14));
            lblCheck.setForeground(BORDER_GRAY);
            navChecks[idx] = lblCheck;
            isi.add(lblCheck, BorderLayout.EAST);

            baris.add(isi, BorderLayout.CENTER);

            MouseAdapter ma = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    scrollToSection(fIdx);
                }
                @Override public void mouseEntered(MouseEvent e) {
                    if (fIdx != activeSectionIndex) {
                        baris.setBackground(new Color(0xf7, 0xf8, 0xf5));
                        aksen.setBackground(new Color(0xf7, 0xf8, 0xf5));
                    }
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (fIdx != activeSectionIndex) {
                        baris.setBackground(CARD_BG);
                        aksen.setBackground(CARD_BG);
                    }
                }
            };

            // Pasang MouseListener ke semua komponen anak agar klik pada nomor, judul, titik, atau area kosong sama-sama merespons
            Component[] areaKlik = {baris, aksen, isi, lblNomor, lblT, lblCheck};
            for (Component k : areaKlik) {
                k.setCursor(new Cursor(Cursor.HAND_CURSOR));
                k.addMouseListener(ma);
            }

            navItems[idx] = baris;
            cardNav.add(baris);
        }

        // Sakelar Mode: Satu Bagian vs Semua Bagian diletakkan DI BAWAH (setelah item 08)
        JPanel footNav = new JPanel(new BorderLayout());
        footNav.setBackground(CARD_HEADER_BG);
        footNav.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(0xec, 0xee, 0xea)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        footNav.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        footNav.add(buatToggleMode(), BorderLayout.CENTER);
        cardNav.add(footNav);

        gs.gridy = 1; gs.insets = new Insets(0, 0, 10, 0);
        sidebar.add(cardNav, gs);

        // 3. Alur Transfer Card
        JPanel cardAlur = new JPanel();
        cardAlur.setLayout(new BoxLayout(cardAlur, BoxLayout.Y_AXIS));
        cardAlur.setBackground(CARD_BG);
        cardAlur.setBorder(new LineBorder(CARD_BORDER, 1, false));
        cardAlur.setPreferredSize(new Dimension(300, 155));
        cardAlur.setMinimumSize(new Dimension(300, 155));
        cardAlur.setMaximumSize(new Dimension(300, 155));

        JPanel headAlur = new JPanel(new BorderLayout());
        headAlur.setBackground(CARD_HEADER_BG);
        headAlur.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(0xec, 0xee, 0xea)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        headAlur.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        JLabel lblAlurH = new JLabel("ALUR TRANSFER");
        lblAlurH.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lblAlurH.setForeground(TEXT_LABEL_MUTED);
        headAlur.add(lblAlurH, BorderLayout.WEST);
        cardAlur.add(headAlur);

        JPanel pAlurBody = new JPanel();
        pAlurBody.setLayout(new BoxLayout(pAlurBody, BoxLayout.Y_AXIS));
        pAlurBody.setBackground(CARD_BG);
        pAlurBody.setBorder(new EmptyBorder(10, 12, 10, 12));

        LblAlurAsal = new JLabel("Ruang Asal: -");
        LblAlurAsal.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));
        LblAlurAsal.setForeground(TEXT_DARK);
        LblAlurAsal.setAlignmentX(Component.LEFT_ALIGNMENT);
        pAlurBody.add(LblAlurAsal);

        LblAlurKelasAsal = new JLabel("Kelas Asal: -");
        LblAlurKelasAsal.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        LblAlurKelasAsal.setForeground(TEXT_MUTED);
        LblAlurKelasAsal.setAlignmentX(Component.LEFT_ALIGNMENT);
        pAlurBody.add(LblAlurKelasAsal);

        JLabel arrow = new JLabel("\u2193 Ke Ruangan");
        arrow.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));
        arrow.setForeground(TEAL_ACCENT);
        arrow.setBorder(new EmptyBorder(4, 0, 4, 0));
        arrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pAlurBody.add(arrow);

        LblAlurTujuan = new JLabel("Ruang Tujuan: -");
        LblAlurTujuan.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 12));
        LblAlurTujuan.setForeground(TEAL_ACCENT);
        LblAlurTujuan.setAlignmentX(Component.LEFT_ALIGNMENT);
        pAlurBody.add(LblAlurTujuan);

        LblAlurKelasTujuan = new JLabel("Kelas Tujuan: -");
        LblAlurKelasTujuan.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        LblAlurKelasTujuan.setForeground(TEXT_MUTED);
        LblAlurKelasTujuan.setAlignmentX(Component.LEFT_ALIGNMENT);
        pAlurBody.add(LblAlurKelasTujuan);

        pAlurBody.add(Box.createVerticalStrut(4));
        LblAlurCara = new JLabel("Cara Pindah: -");
        LblAlurCara.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.ITALIC, 11));
        LblAlurCara.setForeground(new Color(0x6c, 0x73, 0x6e));
        LblAlurCara.setAlignmentX(Component.LEFT_ALIGNMENT);
        pAlurBody.add(LblAlurCara);

        cardAlur.add(pAlurBody);

        gs.gridy = 2; gs.insets = new Insets(0, 0, 0, 0);
        sidebar.add(cardAlur, gs);

        gs.gridy = 3; gs.weighty = 1.0; gs.fill = GridBagConstraints.BOTH;
        sidebar.add(Box.createGlue(), gs);

        return sidebar;
    }

    private void scrollToSection(int index) {
        if (index >= 0 && index < sectionCards.length && sectionCards[index] != null && scrollInput != null) {
            activeSectionIndex = index;
            updateNavHighlight();
            if (satuBagianMode) {
                terapkanModeHalaman();
                scrollInput.getVerticalScrollBar().setValue(0);
                SwingUtilities.invokeLater(() -> {
                    if (scrollInput != null && scrollInput.getVerticalScrollBar() != null) {
                        scrollInput.getVerticalScrollBar().setValue(0);
                    }
                });
            } else {
                isProgrammaticScroll = true;
                int targetY = sectionCards[index].getY();
                scrollInput.getVerticalScrollBar().setValue(targetY);
                Rectangle r = new Rectangle(0, targetY, sectionCards[index].getWidth(), scrollInput.getViewport().getHeight());
                SectionsWrapper.scrollRectToVisible(r);
                SwingUtilities.invokeLater(() -> isProgrammaticScroll = false);
            }
        }
    }

    /**
     * Mengatur visibilitas kartu section sesuai mode aktif konsisten Form IGD.
     * Dalam mode "Satu bagian": hanya kartu aktif yang terlihat, ruang strut dinonaktifkan.
     * Dalam mode "Semua bagian": seluruh kartu dan strut pemisah ditampilkan.
     */
    private void terapkanModeHalaman() {
        int aktif = Math.max(0, activeSectionIndex);
        for (int i = 0; i < sectionCards.length; i++) {
            if (sectionCards[i] != null) {
                sectionCards[i].setVisible(!satuBagianMode || i == aktif);
            }
        }
        for (int i = 0; i < sectionStruts.length; i++) {
            if (sectionStruts[i] != null) {
                sectionStruts[i].setVisible(!satuBagianMode);
            }
        }
        if (SectionsWrapper != null) {
            SectionsWrapper.revalidate();
            SectionsWrapper.repaint();
        }
    }

    /**
     * Sakelar mode "Satu bagian" vs "Semua bagian" di bawah daftar bagian konsisten Form IGD.
     */
    private JPanel buatToggleMode() {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setOpaque(false);

        bgModeBagian = new ButtonGroup();
        rbSatuBagian = new ModeToggleButton("Satu bagian", bgModeBagian);
        rbSemuaBagian = new ModeToggleButton("Semua bagian", bgModeBagian);

        rbSatuBagian.setSelected(satuBagianMode);
        rbSemuaBagian.setSelected(!satuBagianMode);

        ActionListener ubah = e -> {
            satuBagianMode = rbSatuBagian.isSelected();
            terapkanModeHalaman();
            scrollToSection(Math.max(0, activeSectionIndex));
            rbSatuBagian.repaint();
            rbSemuaBagian.repaint();
        };
        rbSatuBagian.addActionListener(ubah);
        rbSemuaBagian.addActionListener(ubah);

        p.add(rbSatuBagian);
        p.add(rbSemuaBagian);
        return p;
    }

    public static class ModeToggleButton extends JToggleButton {
        private boolean isHover = false;

        public ModeToggleButton(String text, ButtonGroup group) {
            super(text);
            if (group != null) group.add(this);
            setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setOpaque(false);
            putClientProperty("Component.arc", 0);
            putClientProperty("Button.arc", 0);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (isSelected()) {
                g2.setColor(TEAL_LIGHT);
                g2.fillRect(0, 0, w, h);
                g2.setColor(TEAL_ACCENT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRect(0, 0, w - 1, h - 1);
                g2.setColor(TEAL_ACCENT);
                g2.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
            } else if (isHover) {
                g2.setColor(new Color(0xf7, 0xf8, 0xf5));
                g2.fillRect(0, 0, w, h);
                g2.setColor(TEAL_ACCENT);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRect(0, 0, w - 1, h - 1);
                g2.setColor(TEXT_DARK);
                g2.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, w, h);
                g2.setColor(WARNA_BORDER_KOTAK);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRect(0, 0, w - 1, h - 1);
                g2.setColor(TEXT_DARK);
                g2.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
            }

            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(getText())) / 2;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(135, 26);
        }
    }

    /**
     * Footer navigasi bawah tiap kartu section (Prev + Counter + Next) konsisten Form IGD.
     */
    private JPanel buatNavigasiBawah(final int idx) {
        JPanel nav = new JPanel(new BorderLayout(0, 0));
        nav.setBackground(CARD_BG);
        nav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xec, 0xee, 0xea)));
        nav.setPreferredSize(new Dimension(10, 48));

        JPanel pnlKiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlKiri.setOpaque(false);

        if (idx > 0) {
            pnlKiri.add(buatKartuTombolNavigasi(SECTION_TITLES[idx - 1], "‹", false, true, () -> {
                scrollToSection(idx - 1);
            }));
        } else {
            pnlKiri.add(buatKartuTombolNavigasi("Awal", "‹", false, false, null));
        }

        JLabel lblPos = new JLabel("Bagian " + (idx + 1) + " dari " + sectionCards.length);
        lblPos.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblPos.setForeground(TEXT_MUTED);
        lblPos.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel pnlKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        pnlKanan.setOpaque(false);

        if (idx < sectionCards.length - 1) {
            pnlKanan.add(buatKartuTombolNavigasi(SECTION_TITLES[idx + 1], "›", true, true, () -> {
                scrollToSection(idx + 1);
            }));
        } else {
            pnlKanan.add(buatKartuTombolNavigasi("Akhir", "›", true, false, null));
        }

        nav.add(pnlKiri, BorderLayout.WEST);
        nav.add(lblPos, BorderLayout.CENTER);
        nav.add(pnlKanan, BorderLayout.EAST);
        return nav;
    }

    private JPanel buatKartuTombolNavigasi(String teks, String chevron, boolean arahKanan, boolean enabled, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xD8, 0xDB, 0xD6), 1, false),
            new EmptyBorder(0, 12, 0, 12)
        ));

        JLabel lblTeks = new JLabel(teks == null ? "" : teks.trim());
        lblTeks.setHorizontalAlignment(arahKanan ? SwingConstants.LEFT : SwingConstants.RIGHT);
        lblTeks.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblTeks.setForeground(enabled ? TEXT_DARK : new Color(0x94, 0xA3, 0xB8));

        JLabel lblChevron = new JLabel(chevron);
        lblChevron.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));
        lblChevron.setForeground(enabled ? TEAL_ACCENT : new Color(0xCB, 0xD5, 0xE1));

        if (arahKanan) {
            card.add(lblTeks, BorderLayout.CENTER);
            card.add(lblChevron, BorderLayout.EAST);
        } else {
            card.add(lblChevron, BorderLayout.WEST);
            card.add(lblTeks, BorderLayout.CENTER);
        }

        FontMetrics fmTeks = lblTeks.getFontMetrics(lblTeks.getFont());
        FontMetrics fmChev = lblChevron.getFontMetrics(lblChevron.getFont());
        int pTeks = fmTeks != null ? fmTeks.stringWidth(lblTeks.getText()) : (lblTeks.getText().length() * 7);
        int pChev = fmChev != null ? fmChev.stringWidth(chevron) : 10;
        int lebarCard = pTeks + pChev + 8 + 24 + 10;
        card.setPreferredSize(new Dimension(lebarCard, 32));

        if (enabled) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblTeks.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblChevron.setCursor(new Cursor(Cursor.HAND_CURSOR));

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(new Color(0xF8, 0xFA, 0xFC));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(TEAL_ACCENT, 1, false),
                        new EmptyBorder(0, 12, 0, 12)
                    ));
                    card.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(0xD8, 0xDB, 0xD6), 1, false),
                        new EmptyBorder(0, 12, 0, 12)
                    ));
                    card.repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onClick != null) {
                        onClick.run();
                    }
                }
            };
            card.addMouseListener(ma);
            lblTeks.addMouseListener(ma);
            lblChevron.addMouseListener(ma);
        }

        return card;
    }

    private void updateNavHighlight() {
        for (int i = 0; i < navItems.length; i++) {
            boolean active = (i == activeSectionIndex);
            if (navItems[i] != null) {
                navItems[i].setBackground(active ? TEAL_LIGHT : CARD_BG);
            }
            if (navAccents[i] != null) {
                navAccents[i].setBackground(active ? TEAL_ACCENT : CARD_BG);
            }
            if (navNomor[i] != null) {
                navNomor[i].setForeground(active ? TEAL_ACCENT : TEXT_LABEL_MUTED);
            }
            if (navTitles[i] != null) {
                navTitles[i].setFont(new Font(ThemeConfig.FONT_FAMILY, active ? Font.BOLD : Font.PLAIN, 11));
                navTitles[i].setForeground(active ? TEAL_ACCENT : TEXT_DARK);
            }
        }
    }

    private JPanel createSectionCard(int index, String letter, String title, String subtitle, JComponent headerRight, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CARD_BORDER, 1, false),
            new EmptyBorder(0, 0, 0, 0)
        ));

        // Header Card
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(CARD_HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(0xec, 0xee, 0xea)),
            new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        String numStr = String.format("%02d", index + 1);
        JLabel badge = new JLabel(numStr, SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(26, 22));
        badge.setBackground(index == 0 ? TEAL_HEADER : TEAL_ACCENT);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Monospaced", Font.BOLD, 11));
        badge.setOpaque(true);
        left.add(badge);

        JPanel pTitle = new JPanel();
        pTitle.setLayout(new BoxLayout(pTitle, BoxLayout.Y_AXIS));
        pTitle.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 13));
        lblTitle.setForeground(TEXT_DARK);
        pTitle.add(lblTitle);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblSub.setForeground(TEXT_MUTED);
        pTitle.add(lblSub);

        left.add(pTitle);
        header.add(left, BorderLayout.WEST);

        if (headerRight != null) {
            header.add(headerRight, BorderLayout.EAST);
        }

        card.add(header, BorderLayout.NORTH);

        // Body Content
        content.setBackground(CARD_BG);
        content.setBorder(new EmptyBorder(16, 20, 18, 20));
        card.add(content, BorderLayout.CENTER);

        // Footer Navigasi Bawah Kartu konsisten Form IGD
        card.add(buatNavigasiBawah(index), BorderLayout.SOUTH);

        sectionCards[index] = card;
        return card;
    }

    // =================================================================
    //  SEKSI A S/D H
    // =================================================================

    private void rakitSeksiA() {
        JPanel body = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // Sub-panel Asal Pasien
        JPanel pAsal = new JPanel(new GridBagLayout());
        pAsal.setBackground(CARD_BG);
        pAsal.setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDER_GRAY, 1, false), " RUANGAN ASAL ", 0, 0,
            new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11), TEAL_ACCENT));

        PilihRuangAsal = new widget.ComboBox();
        PilihRuangAsal.setPreferredSize(new Dimension(240, 24));

        AsalRuang = new widget.TextBox();
        AsalRuang.setPreferredSize(new Dimension(200, 24));
        AsalRuang.setEditable(false);

        KelasAsalRuang = new widget.TextBox();
        KelasAsalRuang.setPreferredSize(new Dimension(100, 24));
        KelasAsalRuang.setEditable(false);

        PilihRuangAsal.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && PilihRuangAsal.getSelectedItem() instanceof KamarItem) {
                KamarItem item = (KamarItem) PilihRuangAsal.getSelectedItem();
                AsalRuang.setText(item.kdKamar.isEmpty() ? "" : item.label);
                KelasAsalRuang.setText(item.kelas == null ? "" : item.kelas);
                syncRadioPill(bgKelasAsal, KelasAsalRuang.getText());
                updateAlurDisplay();
                updateProgress();
            }
        });

        KdDokterMerawatAsal1 = new widget.TextBox(); NmDokterMerawatAsal1 = new widget.TextBox(); BtnDokterMerawatAsal1 = new widget.Button();
        KdDokterMerawatAsal2 = new widget.TextBox(); NmDokterMerawatAsal2 = new widget.TextBox(); BtnDokterMerawatAsal2 = new widget.Button();
        KdDokterMerawatAsal3 = new widget.TextBox(); NmDokterMerawatAsal3 = new widget.TextBox(); BtnDokterMerawatAsal3 = new widget.Button();

        Dimension dimLabelKiri = new Dimension(150, 26);

        GridBagConstraints ga = new GridBagConstraints();
        ga.insets = new Insets(5, 10, 5, 10);
        ga.anchor = GridBagConstraints.WEST;

        ga.gridx = 0; ga.gridy = 0; ga.weightx = 0.0; ga.fill = GridBagConstraints.NONE;
        JLabel lblAsal0 = labelField("PILIH KAMAR/BANGSAL");
        lblAsal0.setPreferredSize(dimLabelKiri);
        pAsal.add(lblAsal0, ga);
        ga.gridx = 1; ga.gridy = 0; ga.weightx = 1.0; ga.fill = GridBagConstraints.HORIZONTAL;
        PilihRuangAsal.setPreferredSize(new Dimension(240, 26));
        pAsal.add(PilihRuangAsal, ga);

        ga.gridx = 0; ga.gridy = 1; ga.weightx = 0.0; ga.fill = GridBagConstraints.NONE;
        JLabel lblAsal1 = labelField("DOKTER MERAWAT ASAL 1");
        lblAsal1.setPreferredSize(dimLabelKiri);
        pAsal.add(lblAsal1, ga);
        ga.gridx = 1; ga.gridy = 1; ga.weightx = 1.0; ga.fill = GridBagConstraints.HORIZONTAL;
        pAsal.add(panelPickerDokter(KdDokterMerawatAsal1, NmDokterMerawatAsal1, BtnDokterMerawatAsal1), ga);

        ga.gridx = 0; ga.gridy = 2; ga.weightx = 0.0; ga.fill = GridBagConstraints.NONE;
        JLabel lblAsal2 = labelField("DOKTER MERAWAT ASAL 2");
        lblAsal2.setPreferredSize(dimLabelKiri);
        pAsal.add(lblAsal2, ga);
        ga.gridx = 1; ga.gridy = 2; ga.weightx = 1.0; ga.fill = GridBagConstraints.HORIZONTAL;
        pAsal.add(panelPickerDokter(KdDokterMerawatAsal2, NmDokterMerawatAsal2, BtnDokterMerawatAsal2), ga);

        ga.gridx = 0; ga.gridy = 3; ga.weightx = 0.0; ga.fill = GridBagConstraints.NONE;
        JLabel lblAsal3 = labelField("DOKTER MERAWAT ASAL 3");
        lblAsal3.setPreferredSize(dimLabelKiri);
        pAsal.add(lblAsal3, ga);
        ga.gridx = 1; ga.gridy = 3; ga.weightx = 1.0; ga.fill = GridBagConstraints.HORIZONTAL;
        pAsal.add(panelPickerDokter(KdDokterMerawatAsal3, NmDokterMerawatAsal3, BtnDokterMerawatAsal3), ga);

        ga.gridx = 0; ga.gridy = 4; ga.gridwidth = 2; ga.weighty = 1.0; ga.fill = GridBagConstraints.BOTH;
        pAsal.add(Box.createVerticalGlue(), ga);

        // Sub-panel Pindah Ke (Tujuan)
        JPanel pTujuan = new JPanel(new GridBagLayout());
        pTujuan.setBackground(CARD_BG);
        pTujuan.setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDER_GRAY, 1, false), " PINDAH KE (TUJUAN) ", 0, 0,
            new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11), TEAL_ACCENT));

        PilihRuangTujuan = new widget.ComboBox();
        PilihRuangTujuan.setPreferredSize(new Dimension(240, 26));

        RuangSelanjutnya = new widget.TextBox();
        RuangSelanjutnya.setPreferredSize(new Dimension(200, 26));
        RuangSelanjutnya.setEditable(false);

        KelasRuangSelanjutnya = new widget.TextBox();
        KelasRuangSelanjutnya.setPreferredSize(new Dimension(100, 26));
        KelasRuangSelanjutnya.setEditable(false);

        PilihRuangTujuan.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && PilihRuangTujuan.getSelectedItem() instanceof KamarItem) {
                KamarItem item = (KamarItem) PilihRuangTujuan.getSelectedItem();
                RuangSelanjutnya.setText(item.kdKamar.isEmpty() ? "" : item.label);
                KelasRuangSelanjutnya.setText(item.kelas == null ? "" : item.kelas);
                syncRadioPill(bgKelasTujuan, KelasRuangSelanjutnya.getText());
                updateAlurDisplay();
                updateProgress();
            }
        });

        KdDokterMerawatTujuan1 = new widget.TextBox(); NmDokterMerawatTujuan1 = new widget.TextBox(); BtnDokterMerawatTujuan1 = new widget.Button();
        KdDokterMerawatTujuan2 = new widget.TextBox(); NmDokterMerawatTujuan2 = new widget.TextBox(); BtnDokterMerawatTujuan2 = new widget.Button();
        KdDokterMerawatTujuan3 = new widget.TextBox(); NmDokterMerawatTujuan3 = new widget.TextBox(); BtnDokterMerawatTujuan3 = new widget.Button();

        GridBagConstraints gt = new GridBagConstraints();
        gt.insets = new Insets(5, 10, 5, 10);
        gt.anchor = GridBagConstraints.WEST;

        gt.gridx = 0; gt.gridy = 0; gt.weightx = 0.0; gt.fill = GridBagConstraints.NONE;
        JLabel lblTujuan0 = labelField("PILIH RUANG TUJUAN");
        lblTujuan0.setPreferredSize(dimLabelKiri);
        pTujuan.add(lblTujuan0, gt);
        gt.gridx = 1; gt.gridy = 0; gt.weightx = 1.0; gt.fill = GridBagConstraints.HORIZONTAL;
        pTujuan.add(PilihRuangTujuan, gt);

        gt.gridx = 0; gt.gridy = 1; gt.weightx = 0.0; gt.fill = GridBagConstraints.NONE;
        JLabel lblTujuan1 = labelField("DOKTER MERAWAT TUJUAN 1");
        lblTujuan1.setPreferredSize(dimLabelKiri);
        pTujuan.add(lblTujuan1, gt);
        gt.gridx = 1; gt.gridy = 1; gt.weightx = 1.0; gt.fill = GridBagConstraints.HORIZONTAL;
        pTujuan.add(panelPickerDokter(KdDokterMerawatTujuan1, NmDokterMerawatTujuan1, BtnDokterMerawatTujuan1), gt);

        gt.gridx = 0; gt.gridy = 2; gt.weightx = 0.0; gt.fill = GridBagConstraints.NONE;
        JLabel lblTujuan2 = labelField("DOKTER MERAWAT TUJUAN 2");
        lblTujuan2.setPreferredSize(dimLabelKiri);
        pTujuan.add(lblTujuan2, gt);
        gt.gridx = 1; gt.gridy = 2; gt.weightx = 1.0; gt.fill = GridBagConstraints.HORIZONTAL;
        pTujuan.add(panelPickerDokter(KdDokterMerawatTujuan2, NmDokterMerawatTujuan2, BtnDokterMerawatTujuan2), gt);

        gt.gridx = 0; gt.gridy = 3; gt.weightx = 0.0; gt.fill = GridBagConstraints.NONE;
        JLabel lblTujuan3 = labelField("DOKTER MERAWAT TUJUAN 3");
        lblTujuan3.setPreferredSize(dimLabelKiri);
        pTujuan.add(lblTujuan3, gt);
        gt.gridx = 1; gt.gridy = 3; gt.weightx = 1.0; gt.fill = GridBagConstraints.HORIZONTAL;
        pTujuan.add(panelPickerDokter(KdDokterMerawatTujuan3, NmDokterMerawatTujuan3, BtnDokterMerawatTujuan3), gt);

        gt.gridx = 0; gt.gridy = 4; gt.gridwidth = 2; gt.weighty = 1.0; gt.fill = GridBagConstraints.BOTH;
        pTujuan.add(Box.createVerticalGlue(), gt);

        // Cara Pindah
        MetodePemindahan = new widget.ComboBox();
        MetodePemindahan.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Jalan","Kursi Roda","Strecher","Lain-lain","Tempat Tidur","Brankar","Jalan Sendiri","-"
        }));
        MetodePemindahan.setPreferredSize(new Dimension(160, 26));

        JPanel pCaraPindahPills = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pCaraPindahPills.setOpaque(false);
        String[] caraList = {"Jalan", "Kursi Roda", "Strecher", "Tempat Tidur", "Brankar", "Lain-lain"};
        for (String c : caraList) {
            RadioPill pill = new RadioPill(c, bgCaraPindah);
            pill.addActionListener(ev -> {
                MetodePemindahan.setSelectedItem(c);
                updateAlurDisplay();
            });
            pCaraPindahPills.add(pill);
        }

        // Keluarga
        NamaMenyetujui = new widget.TextBox();
        NamaMenyetujui.setPreferredSize(new Dimension(170, 26));
        TNoTlpMenyetujui = new widget.TextBox();
        TNoTlpMenyetujui.setPreferredSize(new Dimension(130, 26));
        HubunganMenyetujui = new widget.ComboBox();
        HubunganMenyetujui.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "Suami","Istri","Anak","Orang Tua","Saudara","Diri Sendiri","Lain-lain","-"
        }));
        HubunganMenyetujui.setPreferredSize(new Dimension(120, 26));

        MenyetujuiPemindahan = new widget.ComboBox();
        MenyetujuiPemindahan.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Ya", "Tidak"}));
        MenyetujuiPemindahan.setSelectedItem("Ya");

        SudahDijelaskanDiagnosa = new widget.ComboBox();
        SudahDijelaskanDiagnosa.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Ya", "Tidak"}));
        SudahDijelaskanDiagnosa.setPreferredSize(new Dimension(80, 26));

        JPanel pDijelaskanPills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pDijelaskanPills.setOpaque(false);
        RadioPill pillYa = new RadioPill("Ya", bgDijelaskan);
        pillYa.addActionListener(ev -> SudahDijelaskanDiagnosa.setSelectedItem("Ya"));
        RadioPill pillTidak = new RadioPill("Tidak", bgDijelaskan);
        pillTidak.addActionListener(ev -> SudahDijelaskanDiagnosa.setSelectedItem("Tidak"));
        pDijelaskanPills.add(pillYa);
        pDijelaskanPills.add(pillTidak);

        JPanel pKeluargaGrid = new JPanel(new GridBagLayout());
        pKeluargaGrid.setOpaque(false);
        GridBagConstraints gk = new GridBagConstraints();
        gk.insets = new Insets(2, 4, 2, 8);
        gk.anchor = GridBagConstraints.WEST;

        gk.gridx = 0; gk.gridy = 0; gk.weightx = 0.0; gk.fill = GridBagConstraints.NONE;
        pKeluargaGrid.add(labelField("NAMA:"), gk);
        gk.gridx = 1; gk.gridy = 0; gk.weightx = 0.50; gk.fill = GridBagConstraints.HORIZONTAL;
        NamaMenyetujui.setPreferredSize(new Dimension(200, 26));
        pKeluargaGrid.add(NamaMenyetujui, gk);

        gk.gridx = 2; gk.gridy = 0; gk.weightx = 0.0; gk.fill = GridBagConstraints.NONE;
        pKeluargaGrid.add(labelField("NO. TELP:"), gk);
        gk.gridx = 3; gk.gridy = 0; gk.weightx = 0.25; gk.fill = GridBagConstraints.HORIZONTAL;
        TNoTlpMenyetujui.setPreferredSize(new Dimension(140, 26));
        pKeluargaGrid.add(TNoTlpMenyetujui, gk);

        gk.gridx = 4; gk.gridy = 0; gk.weightx = 0.0; gk.fill = GridBagConstraints.NONE;
        pKeluargaGrid.add(labelField("HUBUNGAN:"), gk);
        gk.gridx = 5; gk.gridy = 0; gk.weightx = 0.25; gk.fill = GridBagConstraints.HORIZONTAL;
        HubunganMenyetujui.setPreferredSize(new Dimension(140, 26));
        pKeluargaGrid.add(HubunganMenyetujui, gk);

        // Pasang pAsal dan pTujuan ke Body secara simetris 50% - 50%
        g.gridx = 0; g.gridy = 0; g.weightx = 0.5; g.weighty = 0.0;
        g.fill = GridBagConstraints.BOTH;
        body.add(pAsal, g);

        g.gridx = 1; g.gridy = 0; g.weightx = 0.5; g.weighty = 0.0;
        g.fill = GridBagConstraints.BOTH;
        body.add(pTujuan, g);

        // Panel Bawah: Cara Pindah, Persetujuan Keluarga & Edukasi
        JPanel pBawahA = new JPanel(new GridBagLayout());
        pBawahA.setBackground(CARD_BG);
        pBawahA.setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDER_GRAY, 1, false), " CARA PEMINDAHAN & PERSETUJUAN KELUARGA ", 0, 0,
            new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11), TEAL_ACCENT));

        GridBagConstraints gba = new GridBagConstraints();
        gba.insets = new Insets(6, 10, 6, 10);
        gba.anchor = GridBagConstraints.WEST;

        gba.gridx = 0; gba.gridy = 0; gba.weightx = 0.0; gba.fill = GridBagConstraints.NONE;
        JLabel lblCara = labelField("CARA PEMINDAHAN");
        lblCara.setPreferredSize(dimLabelKiri);
        pBawahA.add(lblCara, gba);
        gba.gridx = 1; gba.gridy = 0; gba.weightx = 1.0; gba.fill = GridBagConstraints.HORIZONTAL;
        pBawahA.add(pCaraPindahPills, gba);

        gba.gridx = 0; gba.gridy = 1; gba.weightx = 0.0; gba.fill = GridBagConstraints.NONE;
        JLabel lblKeluarga = labelField("KELUARGA MENYETUJUI");
        lblKeluarga.setPreferredSize(dimLabelKiri);
        pBawahA.add(lblKeluarga, gba);
        gba.gridx = 1; gba.gridy = 1; gba.weightx = 1.0; gba.fill = GridBagConstraints.HORIZONTAL;
        pBawahA.add(pKeluargaGrid, gba);

        JPanel pKonf = new JPanel(new BorderLayout(16, 0));
        pKonf.setBackground(new Color(0xf8, 0xfa, 0xfc));
        pKonf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xe2, 0xe8, 0xf0), 1, false),
            new EmptyBorder(8, 14, 8, 14)
        ));
        JLabel lblKonf = new JLabel("Diagnosa, alasan pindah, dan risiko sudah dijelaskan kepada keluarga pasien");
        lblKonf.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
        lblKonf.setForeground(TEXT_DARK);
        pKonf.add(lblKonf, BorderLayout.CENTER);
        pKonf.add(pDijelaskanPills, BorderLayout.EAST);

        gba.gridx = 0; gba.gridy = 2; gba.gridwidth = 2; gba.weightx = 1.0; gba.fill = GridBagConstraints.HORIZONTAL;
        pBawahA.add(pKonf, gba);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; g.weightx = 1.0; g.weighty = 0.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        body.add(pBawahA, g);

        JPanel cardA = createSectionCard(0, "01", SECTION_TITLES[0], SECTION_SUBTITLES[0], null, body);
        SectionsWrapper.add(cardA);
        sectionStruts[0] = Box.createVerticalStrut(16);
        SectionsWrapper.add(sectionStruts[0]);
    }

    private JComponent panelPickerDokter(widget.TextBox kd, widget.TextBox nm, widget.Button btn) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);

        kd.setPreferredSize(new Dimension(80, 26));
        kd.setEditable(false);

        nm.setPreferredSize(new Dimension(180, 26));
        nm.setEditable(false);

        Icon iconSearch = widget.IconStyle.muat("search.svg", 14, widget.IconStyle.ABU);
        btn.setIcon(iconSearch != null ? iconSearch : new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        btn.setPreferredSize(new Dimension(28, 26));
        btn.setRoundRect(false);
        btn.putClientProperty("Component.arc", 0);
        btn.setToolTipText("Cari Dokter");
        btn.setBackground(Color.WHITE);
        btn.setBorder(BORDER_KOTAK_GARIS);
        btn.addActionListener(e -> {
            DlgCariDokter dokter = new DlgCariDokter(null, false);
            dokter.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent ev) {
                    if (dokter.getTable().getSelectedRow() != -1) {
                        kd.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                        nm.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                        updateProgress();
                    }
                }
            });
            dokter.isCek();
            dokter.emptTeks();
            dokter.setSize(internalFrame1.getWidth() - 40, internalFrame1.getHeight() - 40);
            dokter.setLocationRelativeTo(internalFrame1);
            dokter.setVisible(true);
        });

        widget.Button btnClear = new widget.Button();
        Icon iconTrash = widget.IconStyle.muat("trash.svg", 14, widget.IconStyle.MERAH);
        btnClear.setIcon(iconTrash != null ? iconTrash : new javax.swing.ImageIcon(getClass().getResource("/picture/delete-16x16.png")));
        btnClear.setPreferredSize(new Dimension(28, 26));
        btnClear.setRoundRect(false);
        btnClear.putClientProperty("Component.arc", 0);
        btnClear.setToolTipText("Hapus Dokter");
        btnClear.setBackground(Color.WHITE);
        btnClear.setBorder(BORDER_KOTAK_GARIS);
        btnClear.addActionListener(e -> {
            kd.setText("");
            nm.setText("");
            updateProgress();
        });

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        pBtn.setOpaque(false);
        pBtn.add(btn);
        pBtn.add(btnClear);

        p.add(kd, BorderLayout.WEST);
        p.add(nm, BorderLayout.CENTER);
        p.add(pBtn, BorderLayout.EAST);
        return p;
    }

    private void rakitSeksiB() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Header Right: Observasi Terakhir
        JPanel hRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        hRight.setOpaque(false);
        JLabel lblObs = new JLabel("Observasi terakhir pukul:");
        lblObs.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lblObs.setForeground(TEXT_MUTED);
        hRight.add(lblObs);

        ObservasiTerakhirPukul = new PanelTanggalDanJam(true, 24);
        hRight.add(ObservasiTerakhirPukul);

        // 1. Baris Tingkat Kesadaran (Judul di Kiri, Field di Kanan)
        KesadaranTransfer = new widget.ComboBox();
        KesadaranTransfer.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "","Compos Mentis","Gelisah","Delirium","Koma"
        }));
        KesadaranTransfer.setPreferredSize(new Dimension(140, 24));

        JPanel pKesPills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pKesPills.setOpaque(false);
        String[] kesList = {"Compos Mentis", "Gelisah", "Delirium", "Koma"};
        for (String k : kesList) {
            RadioPill pill = new RadioPill(k, bgKesadaran);
            pill.addActionListener(e -> KesadaranTransfer.setSelectedItem(k));
            pKesPills.add(pill);
        }
        body.add(buatBarisDuaKolom("Tingkat Kesadaran", pKesPills, true));

        // 2. Baris Tanda Vital (Judul di Kiri, Grid 4 Kolom di Kanan dgn Satuan)
        TDTransfer = new widget.TextBox();
        HRTransfer = new widget.TextBox();
        RRTransfer = new widget.TextBox();
        SuhuTransfer = new widget.TextBox();
        GCSTransfer = new widget.TextBox();
        SpO2Transfer = new widget.TextBox();

        JPanel panelVitalGrid = new JPanel(new GridBagLayout());
        panelVitalGrid.setOpaque(false);

        GridBagConstraints cV = new GridBagConstraints();
        cV.fill = GridBagConstraints.HORIZONTAL;
        cV.weightx = 0.25;

        // Baris 1: Tekanan Darah, Nadi, Pernapasan, Suhu
        cV.gridy = 0;
        cV.gridx = 0;
        cV.insets = new Insets(0, 0, 10, 14);
        panelVitalGrid.add(buatSelVital("Tekanan Darah", TDTransfer, "mmHg"), cV);

        cV.gridx = 1;
        panelVitalGrid.add(buatSelVital("Nadi", HRTransfer, "/mnt"), cV);

        cV.gridx = 2;
        panelVitalGrid.add(buatSelVital("Pernapasan", RRTransfer, "/mnt"), cV);

        cV.gridx = 3;
        cV.insets = new Insets(0, 0, 10, 0);
        panelVitalGrid.add(buatSelVital("Suhu", SuhuTransfer, "\u00B0C"), cV);

        // Baris 2: GCS, SpO2
        cV.gridy = 1;
        cV.gridx = 0;
        cV.insets = new Insets(0, 0, 0, 14);
        panelVitalGrid.add(buatSelVital("GCS (E,V,M)", GCSTransfer, ""), cV);

        cV.gridx = 1;
        panelVitalGrid.add(buatSelVital("SpO2", SpO2Transfer, "%"), cV);

        cV.gridx = 2;
        panelVitalGrid.add(Box.createHorizontalGlue(), cV);

        cV.gridx = 3;
        cV.insets = new Insets(0, 0, 0, 0);
        panelVitalGrid.add(Box.createHorizontalGlue(), cV);

        body.add(buatBarisDuaKolom("Tanda Vital", panelVitalGrid, true));

        // 3. Baris Tingkat Nyeri (0–10)
        TingkatNyeriTransfer = new widget.ComboBox();
        TingkatNyeriTransfer.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
            "","0","1","2","3","4","5","6","7","8","9","10"
        }));
        TingkatNyeriTransfer.setPreferredSize(new Dimension(60, 24));
        TingkatNyeriTransfer.setVisible(false);

        JPanel pNyeriPills = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        pNyeriPills.setOpaque(false);
        for (int i = 0; i <= 10; i++) {
            final String s = String.valueOf(i);
            RadioPill pill = new RadioPill(s, bgNyeri);
            pill.setPreferredSize(new Dimension(32, 26));
            pill.setHorizontalAlignment(SwingConstants.CENTER);
            pill.addActionListener(e -> {
                TingkatNyeriTransfer.setSelectedItem(s);
                updateNyeriDisplay(s);
            });
            pNyeriPills.add(pill);
        }

        LblNyeriKet = new JLabel("0 \u00B7 Tidak ada rasa nyeri");
        LblNyeriKet.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        LblNyeriKet.setForeground(GREEN_FG);
        LblNyeriKet.setBorder(new EmptyBorder(0, 8, 0, 0));
        pNyeriPills.add(LblNyeriKet);

        TingkatNyeriTransfer.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String v = TingkatNyeriTransfer.getSelectedItem() != null ? TingkatNyeriTransfer.getSelectedItem().toString() : "";
                syncRadioPill(bgNyeri, v);
                updateNyeriDisplay(v);
            }
        });

        body.add(buatBarisDuaKolom("Tingkat Nyeri (0\u201310)", pNyeriPills, true));

        // 4. Baris Dukungan Oksigen (Judul di Kiri, Pilihan di Kanan)
        OksigenStatus = new widget.ComboBox();
        OksigenStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Tidak", "Ya"}));
        OksigenStatus.setPreferredSize(new Dimension(80, 24));

        RadioPill pillTidak = new RadioPill("Tidak", bgOksigenStatus);
        pillTidak.setSelected(true);
        pillTidak.addActionListener(e -> OksigenStatus.setSelectedItem("Tidak"));

        RadioPill pillYa = new RadioPill("Ya", bgOksigenStatus);
        pillYa.addActionListener(e -> OksigenStatus.setSelectedItem("Ya"));

        JPanel pStatusPills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pStatusPills.setOpaque(false);
        pStatusPills.add(pillTidak);
        pStatusPills.add(pillYa);
        body.add(buatBarisDuaKolom("Dukungan Oksigen", pStatusPills, true));

        // 5. Baris Jenis Alat Oksigen (Judul di Kiri, Chips di Kanan)
        CekOksigenNasalKanule = new ChipCheckBox("Nasal Kanule");
        CekOksigenNasalKateter = new ChipCheckBox("Nasal Kateter");
        CekOksigenFaceMask = new ChipCheckBox("Face Mask");
        CekOksigenSungkupRebreathing = new ChipCheckBox("Sungkup Rebreathing");
        CekOksigenSungkupNonRebreathing = new ChipCheckBox("Sungkup Non-Rebreathing");
        CekOksigenTracheostomy = new ChipCheckBox("Tracheostomy");
        CekOksigenEtt = new ChipCheckBox("ETT");

        JPanel pChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pChips.setOpaque(false);
        pChips.add(CekOksigenNasalKanule);
        pChips.add(CekOksigenNasalKateter);
        pChips.add(CekOksigenFaceMask);
        pChips.add(CekOksigenSungkupRebreathing);
        pChips.add(CekOksigenSungkupNonRebreathing);
        pChips.add(CekOksigenTracheostomy);
        pChips.add(CekOksigenEtt);
        body.add(buatBarisDuaKolom("Jenis Alat Oksigen", pChips, false));

        JPanel cardB = createSectionCard(1, "02", SECTION_TITLES[1], SECTION_SUBTITLES[1], hRight, body);
        SectionsWrapper.add(cardB);
        sectionStruts[1] = Box.createVerticalStrut(16);
        SectionsWrapper.add(sectionStruts[1]);
    }

    /**
     * Membentuk 1 baris dua kolom konsisten Form IGD:
     * - Kolom Kiri  : Judul kelompok / label baris (lebar tetap 210px)
     * - Kolom Kanan : Field isian (kesadaran, grid tanda vital, radio pills, chips)
     * - Pemisah     : Garis batas bawah horizontal tipis #F2F3F0
     */
    private JPanel buatBarisDuaKolom(String judul, JComponent isian, boolean garisPemisah) {
        JPanel baris = new JPanel(new GridBagLayout());
        baris.setOpaque(false);
        if (garisPemisah) {
            baris.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF2, 0xF3, 0xF0)),
                new EmptyBorder(10, 0, 10, 0)
            ));
        } else {
            baris.setBorder(new EmptyBorder(10, 0, 6, 0));
        }

        JLabel lbl = new JLabel(judul);
        lbl.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
        lbl.setForeground(new Color(0x64, 0x74, 0x8b));
        lbl.setVerticalAlignment(SwingConstants.TOP);
        lbl.setPreferredSize(new Dimension(210, 24));
        lbl.setMinimumSize(new Dimension(210, 24));

        GridBagConstraints cl = new GridBagConstraints();
        cl.gridx = 0;
        cl.gridy = 0;
        cl.anchor = GridBagConstraints.NORTHWEST;
        cl.fill = GridBagConstraints.NONE;
        cl.weightx = 0.0;
        cl.insets = new Insets(3, 0, 0, 14);
        baris.add(lbl, cl);

        GridBagConstraints ck = new GridBagConstraints();
        ck.gridx = 1;
        ck.gridy = 0;
        ck.anchor = GridBagConstraints.NORTHWEST;
        ck.fill = GridBagConstraints.HORIZONTAL;
        ck.weightx = 1.0;
        baris.add(isian, ck);

        return baris;
    }

    /**
     * Sel tanda vital konsisten Form IGD: mini-label di atas, field di tengah, satuan di kanan.
     */
    private JPanel buatSelVital(String label, JComponent field, String unit) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        lbl.setForeground(new Color(0x64, 0x74, 0x8b));

        JPanel boxUnit = new JPanel(new BorderLayout(6, 0));
        boxUnit.setOpaque(false);
        field.setPreferredSize(new Dimension(80, 26));
        boxUnit.add(field, BorderLayout.CENTER);

        if (unit != null && !unit.isEmpty()) {
            JLabel lblUnit = new JLabel(unit);
            lblUnit.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
            lblUnit.setForeground(new Color(0x64, 0x74, 0x8b));
            lblUnit.setPreferredSize(new Dimension(38, 26));
            boxUnit.add(lblUnit, BorderLayout.EAST);
        }

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 4, 0);
        p.add(lbl, c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        p.add(boxUnit, c);

        return p;
    }

    /** Panel isian kolom kanan: komponen berjajar rata kiri. */
    private JPanel flowIsian(Component... komponen) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        p.setOpaque(false);
        for (Component k : komponen) p.add(k);
        return p;
    }

    /** Mini-label di dalam kolom isian (mis. "Tgl. Pasang"), konsisten Form IGD. */
    private JLabel subLabel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        l.setForeground(new Color(0x64, 0x74, 0x8b));
        l.setBorder(new EmptyBorder(0, 8, 0, 0));
        return l;
    }

    /** Area teks multi-baris untuk kolom isian kanan. */
    private widget.ScrollPane areaIsian(widget.TextArea ta, int tinggi) {
        ta.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        widget.ScrollPane sp = new widget.ScrollPane();
        sp.setViewportView(ta);
        sp.setPreferredSize(new Dimension(400, tinggi));
        sp.setMinimumSize(new Dimension(200, tinggi));
        pasangScrollTembus(sp, ta);
        return sp;
    }

    /**
     * Meneruskan gerakan mouse-wheel ke halaman utama saat kursor berada di atas kotak teks (JScrollPane dalam).
     * Jika kotak teks sedang fokus diedit dan teks melebihi ukuran kotak, isi kotak teks dapat digulir;
     * di luar itu (kotak tidak fokus, teks pendek/kosong, atau sudah mentok batas atas/bawah),
     * gerakan mouse-wheel otomatis menggulir halaman utama agar form tidak pernah macet di mana pun pointer berada.
     */
    private void pasangScrollTembus(final widget.ScrollPane sp, final widget.TextArea ta) {
        if (sp == null || ta == null) return;
        sp.setWheelScrollingEnabled(false);
        java.awt.event.MouseWheelListener mwl = new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                if (scrollInput == null) return;
                javax.swing.JScrollBar barDalam = sp.getVerticalScrollBar();
                int rot = e.getWheelRotation();
                boolean keBawah = rot > 0;

                boolean butuhScrollDalam = barDalam != null && barDalam.isVisible() 
                        && (barDalam.getMaximum() > barDalam.getVisibleAmount());
                boolean bisaScrollDalam = butuhScrollDalam && (keBawah
                        ? (barDalam.getValue() + barDalam.getVisibleAmount() < barDalam.getMaximum())
                        : (barDalam.getValue() > barDalam.getMinimum()));

                if (bisaScrollDalam && ta.hasFocus()) {
                    int unit = Math.max(12, barDalam.getUnitIncrement(rot));
                    barDalam.setValue(barDalam.getValue() + (e.getUnitsToScroll() * unit));
                } else {
                    javax.swing.JScrollBar barLuar = scrollInput.getVerticalScrollBar();
                    if (barLuar != null) {
                        int unit = Math.max(18, barLuar.getUnitIncrement(rot));
                        int langkah = Math.max(1, Math.abs(rot)) * unit * 3;
                        barLuar.setValue(barLuar.getValue() + (keBawah ? langkah : -langkah));
                    }
                }
                e.consume();
            }
        };
        for (java.awt.event.MouseWheelListener l : ta.getMouseWheelListeners()) ta.removeMouseWheelListener(l);
        for (java.awt.event.MouseWheelListener l : sp.getMouseWheelListeners()) sp.removeMouseWheelListener(l);
        ta.addMouseWheelListener(mwl);
        sp.addMouseWheelListener(mwl);
        if (sp.getViewport() != null) {
            for (java.awt.event.MouseWheelListener l : sp.getViewport().getMouseWheelListeners()) sp.getViewport().removeMouseWheelListener(l);
            sp.getViewport().addMouseWheelListener(mwl);
        }
    }

    private void pasangScrollTembusTextBox(final widget.TextBox tb) {
        if (tb == null) return;
        tb.addMouseWheelListener(e -> {
            if (scrollInput != null) {
                javax.swing.JScrollBar barLuar = scrollInput.getVerticalScrollBar();
                if (barLuar != null) {
                    int rot = e.getWheelRotation();
                    boolean keBawah = rot > 0;
                    int unit = Math.max(18, barLuar.getUnitIncrement(rot));
                    int langkah = Math.max(1, Math.abs(rot)) * unit * 3;
                    barLuar.setValue(barLuar.getValue() + (keBawah ? langkah : -langkah));
                }
                e.consume();
            }
        });
    }

    private void pasangScrollTembusCombo(javax.swing.JComboBox cb) {
        if (cb == null) return;
        cb.addMouseWheelListener(e -> {
            if (!cb.isPopupVisible() && scrollInput != null) {
                javax.swing.JScrollBar barLuar = scrollInput.getVerticalScrollBar();
                if (barLuar != null) {
                    int rot = e.getWheelRotation();
                    boolean keBawah = rot > 0;
                    int unit = Math.max(18, barLuar.getUnitIncrement(rot));
                    int langkah = Math.max(1, Math.abs(rot)) * unit * 3;
                    barLuar.setValue(barLuar.getValue() + (keBawah ? langkah : -langkah));
                }
                e.consume();
            }
        });
    }

    private void pasangScrollTembusSemuaKomponen(java.awt.Container container) {
        if (container == null) return;
        for (Component c : container.getComponents()) {
            if (c instanceof widget.ScrollPane) {
                widget.ScrollPane sp = (widget.ScrollPane) c;
                if (sp != scrollInput) {
                    sp.setWheelScrollingEnabled(false);
                    Component view = sp.getViewport() != null ? sp.getViewport().getView() : null;
                    if (view instanceof widget.TextArea) {
                        pasangScrollTembus(sp, (widget.TextArea) view);
                    }
                }
            } else if (c instanceof widget.TextBox) {
                pasangScrollTembusTextBox((widget.TextBox) c);
            } else if (c instanceof javax.swing.JComboBox) {
                pasangScrollTembusCombo((javax.swing.JComboBox) c);
            }
            if (c instanceof java.awt.Container) {
                pasangScrollTembusSemuaKomponen((java.awt.Container) c);
            }
        }
    }

    private void updateNyeriDisplay(String val) {
        try {
            int n = Integer.parseInt(val.trim());
            if (n == 0) {
                LblNyeriKet.setText("0 \u00B7 Tidak ada rasa nyeri");
                LblNyeriKet.setForeground(GREEN_FG);
            } else if (n <= 3) {
                LblNyeriKet.setText(n + " \u00B7 Nyeri ringan (masih dapat beraktivitas)");
                LblNyeriKet.setForeground(GREEN_FG);
            } else if (n <= 6) {
                LblNyeriKet.setText(n + " \u00B7 Nyeri sedang (mengganggu aktivitas)");
                LblNyeriKet.setForeground(ORANGE_FG);
            } else {
                LblNyeriKet.setText(n + " \u00B7 Nyeri berat / hebat");
                LblNyeriKet.setForeground(new Color(0xa8, 0x44, 0x2e));
            }
        } catch (Exception e) {
            LblNyeriKet.setText("-");
        }
    }

    private void rakitSeksiC() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // 1. Line Invasif & Tanggal Pasang
        RbLineTidakAda = new RadioPill("Tidak Ada Line Invasive", bgLineInvasif);
        RbLineCvpPicc = new RadioPill("CVP/PICC", bgLineInvasif);
        RbLineIvLine = new RadioPill("IV Line", bgLineInvasif);
        LineInvasifTanggal = new PanelTanggalDanJam(true, 24);
        body.add(buatBarisDuaKolom("Line Invasif",
            flowIsian(RbLineTidakAda, RbLineCvpPicc, RbLineIvLine, subLabel("Tgl. Pasang"), LineInvasifTanggal), true));

        // 2. Prosedur Invasif
        ProsedurDilakukan = new widget.TextArea();
        body.add(buatBarisDuaKolom("Prosedur Pembedahan / Invasif", areaIsian(ProsedurDilakukan, 48), true));

        // 3. Kewaspadaan
        CekKewaspadaanStandar = new ChipCheckBox("Standar");
        CekKewaspadaanKontak = new ChipCheckBox("Kontak");
        CekKewaspadaanAirborne = new ChipCheckBox("Airborne");
        CekKewaspadaanDroplet = new ChipCheckBox("Droplet");
        body.add(buatBarisDuaKolom("Kewaspadaan / Precaution",
            flowIsian(CekKewaspadaanStandar, CekKewaspadaanKontak, CekKewaspadaanAirborne, CekKewaspadaanDroplet), true));

        // 4. Nutrisi (Sesuai Form Fisik RM 04: Puasa/Oral/NGT, Batasan Cairan, Diet Khusus)
        CekNutrisiPuasa = new ChipCheckBox("Puasa");
        CekNutrisiOral = new ChipCheckBox("Oral");
        CekNutrisiNgt = new ChipCheckBox("NGT");

        CekNutrisiBatasanCairan = new ChipCheckBox("Batasan Cairan");
        TNutrisiBatasanCairanCc = new widget.TextBox();
        TNutrisiBatasanCairanCc.setPreferredSize(new Dimension(80, 24));

        CekNutrisiDietKhusus = new ChipCheckBox("Diet khusus, Jelaskan");
        TNutrisiDietKhususJelaskan = new widget.TextBox();
        TNutrisiDietKhususJelaskan.setPreferredSize(new Dimension(320, 24));

        // Interaksi otomatis: centang fokus ke textbox, ketik teks auto-centang
        CekNutrisiBatasanCairan.addActionListener(e -> {
            if (CekNutrisiBatasanCairan.isSelected()) {
                TNutrisiBatasanCairanCc.requestFocus();
            } else {
                TNutrisiBatasanCairanCc.setText("");
            }
        });
        TNutrisiBatasanCairanCc.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (!TNutrisiBatasanCairanCc.getText().trim().isEmpty()) {
                    CekNutrisiBatasanCairan.setSelected(true);
                }
            }
        });

        CekNutrisiDietKhusus.addActionListener(e -> {
            if (CekNutrisiDietKhusus.isSelected()) {
                TNutrisiDietKhususJelaskan.requestFocus();
            } else {
                TNutrisiDietKhususJelaskan.setText("");
            }
        });
        TNutrisiDietKhususJelaskan.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (!TNutrisiDietKhususJelaskan.getText().trim().isEmpty()) {
                    CekNutrisiDietKhusus.setSelected(true);
                }
            }
        });

        JPanel pNutrisi = new JPanel();
        pNutrisi.setLayout(new BoxLayout(pNutrisi, BoxLayout.Y_AXIS));
        pNutrisi.setOpaque(false);

        JPanel rowNutrisi1 = flowIsian(CekNutrisiPuasa, CekNutrisiOral, CekNutrisiNgt);
        rowNutrisi1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel rowNutrisi2 = flowIsian(CekNutrisiBatasanCairan, TNutrisiBatasanCairanCc, subLabel("Cc"));
        rowNutrisi2.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel rowNutrisi3 = flowIsian(CekNutrisiDietKhusus, TNutrisiDietKhususJelaskan);
        rowNutrisi3.setAlignmentX(Component.LEFT_ALIGNMENT);

        pNutrisi.add(rowNutrisi1);
        pNutrisi.add(rowNutrisi2);
        pNutrisi.add(rowNutrisi3);

        body.add(buatBarisDuaKolom("Nutrisi", pNutrisi, true));

        // 5. Eliminasi BAB
        RbEliminasiBabNormal = new RadioPill("Normal", bgEliminasiBab);
        RbEliminasiBabDiareKonstipasi = new RadioPill("Diare/Konstipasi", bgEliminasiBab);
        RbEliminasiBabIleustomyColostomy = new RadioPill("Ileustomy/Colostomy", bgEliminasiBab);
        RbEliminasiBabInkontinensiaAlvi = new RadioPill("Inkontinensia Alvi", bgEliminasiBab);
        body.add(buatBarisDuaKolom("Eliminasi BAB",
            flowIsian(RbEliminasiBabNormal, RbEliminasiBabDiareKonstipasi, RbEliminasiBabIleustomyColostomy, RbEliminasiBabInkontinensiaAlvi), true));

        // 6. Eliminasi BAK
        RbEliminasiBakNormal = new RadioPill("Normal", bgEliminasiBak);
        RbEliminasiBakInkontinensiaUrine = new RadioPill("Inkontinensia Urine", bgEliminasiBak);
        RbEliminasiBakKateterUrine = new RadioPill("Kateter Urine", bgEliminasiBak);
        body.add(buatBarisDuaKolom("Eliminasi BAK",
            flowIsian(RbEliminasiBakNormal, RbEliminasiBakInkontinensiaUrine, RbEliminasiBakKateterUrine), true));

        // 7. Kateter Urine (No. & Tgl. Pasang)
        TEliminasiBakKateterNo = new widget.TextBox();
        TEliminasiBakKateterNo.setPreferredSize(new Dimension(80, 24));
        DTPEliminasiBakKateterTanggalPasang = new PanelTanggalDanJam(true, 24);
        body.add(buatBarisDuaKolom("Kateter Urine",
            flowIsian(subLabel("No. Kateter"), TEliminasiBakKateterNo, subLabel("Tgl. Pasang"), DTPEliminasiBakKateterTanggalPasang), true));

        // 8. Aktifitas
        RbAktifitasMandiri = new RadioPill("Mandiri", bgAktifitas);
        RbAktifitasDibantuSebagian = new RadioPill("Dibantu Sebagian", bgAktifitas);
        RbAktifitasDibantuPenuh = new RadioPill("Dibantu Penuh", bgAktifitas);
        body.add(buatBarisDuaKolom("Aktifitas",
            flowIsian(RbAktifitasMandiri, RbAktifitasDibantuSebagian, RbAktifitasDibantuPenuh), true));

        // 9. Mobilisasi
        RbMobilisasiTirahBaring = new RadioPill("Tirah Baring", bgMobilisasi);
        RbMobilisasiDuduk = new RadioPill("Duduk", bgMobilisasi);
        RbMobilisasiJalan = new RadioPill("Jalan", bgMobilisasi);
        body.add(buatBarisDuaKolom("Mobilisasi",
            flowIsian(RbMobilisasiTirahBaring, RbMobilisasiDuduk, RbMobilisasiJalan), true));

        // 10. Drainase
        CekDrainaseWsd = new ChipCheckBox("WSD");
        CekDrainaseDrainLuka = new ChipCheckBox("Drain Luka");
        CekDrainaseStoma = new ChipCheckBox("Stoma");
        CekDrainaseLain = new ChipCheckBox("Lain-lain");
        TDrainaseLainKet = new widget.TextBox();
        TDrainaseLainKet.setPreferredSize(new Dimension(200, 24));
        CekDrainaseLain.addActionListener(e -> {
            if (CekDrainaseLain.isSelected()) TDrainaseLainKet.requestFocus();
        });
        TDrainaseLainKet.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (!TDrainaseLainKet.getText().trim().isEmpty()) {
                    CekDrainaseLain.setSelected(true);
                }
            }
        });
        body.add(buatBarisDuaKolom("Drainase",
            flowIsian(CekDrainaseWsd, CekDrainaseDrainLuka, CekDrainaseStoma, CekDrainaseLain, subLabel("Ket. Lain:"), TDrainaseLainKet), true));

        // 11. Integritas Kulit / Luka
        JPanel pLukaCard = new JPanel(new BorderLayout());
        pLukaCard.setBackground(Color.WHITE);
        pLukaCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE2, 0xE8, 0xF0), 1, true),
            new EmptyBorder(0, 0, 4, 0)
        ));

        // Baris Kontrol Atas: Opsi Tidak Ada Luka
        JPanel pLukaTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pLukaTop.setBackground(new Color(0xF8, 0xFA, 0xFC));
        pLukaTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2, 0xE8, 0xF0)));

        CekLukaTidakAda = new ChipCheckBox("Tidak Ada Luka");
        JLabel lblKetTidakAda = new JLabel("(Centang jika pasien tidak memiliki luka / integritas kulit utuh)");
        lblKetTidakAda.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.ITALIC, 11));
        lblKetTidakAda.setForeground(new Color(0x94, 0xA3, 0xB8));

        pLukaTop.add(CekLukaTidakAda);
        pLukaTop.add(lblKetTidakAda);
        pLukaCard.add(pLukaTop, BorderLayout.NORTH);

        // Grid Tabel Luka 4 Kolom: Jenis | Ukuran | Lokasi | Kondisi
        JPanel pLukaTable = new JPanel(new GridBagLayout());
        pLukaTable.setBackground(Color.WHITE);
        pLukaTable.setBorder(new EmptyBorder(6, 8, 8, 8));

        GridBagConstraints gl = new GridBagConstraints();
        gl.fill = GridBagConstraints.HORIZONTAL;
        gl.insets = new Insets(3, 4, 3, 4);

        // Header Tabel
        gl.gridy = 0;
        gl.gridx = 0; gl.weightx = 0.22; pLukaTable.add(buatHeaderKolomTabel("JENIS LUKA"), gl);
        gl.gridx = 1; gl.weightx = 0.22; pLukaTable.add(buatHeaderKolomTabel("UKURAN"), gl);
        gl.gridx = 2; gl.weightx = 0.28; pLukaTable.add(buatHeaderKolomTabel("LOKASI"), gl);
        gl.gridx = 3; gl.weightx = 0.28; pLukaTable.add(buatHeaderKolomTabel("KONDISI"), gl);

        // Baris 1: Operasi
        CekLukaOperasi = new ChipCheckBox("Luka Operasi");
        CekLukaOperasi.setPlainMode(true);
        TLukaOperasiUkuran = new widget.TextBox(); TLukaOperasiUkuran.setPreferredSize(new Dimension(130, 26));
        TLukaOperasiLokasi = new widget.TextBox(); TLukaOperasiLokasi.setPreferredSize(new Dimension(160, 26));
        TLukaOperasiKondisi = new widget.TextBox(); TLukaOperasiKondisi.setPreferredSize(new Dimension(170, 26));
        gl.gridy = 1;
        gl.gridx = 0; gl.weightx = 0.22; pLukaTable.add(CekLukaOperasi, gl);
        gl.gridx = 1; gl.weightx = 0.22; pLukaTable.add(TLukaOperasiUkuran, gl);
        gl.gridx = 2; gl.weightx = 0.28; pLukaTable.add(TLukaOperasiLokasi, gl);
        gl.gridx = 3; gl.weightx = 0.28; pLukaTable.add(TLukaOperasiKondisi, gl);

        // Baris 2: Dekubitus
        CekLukaDekubitus = new ChipCheckBox("Dekubitus");
        CekLukaDekubitus.setPlainMode(true);
        TLukaDekubitusUkuran = new widget.TextBox(); TLukaDekubitusUkuran.setPreferredSize(new Dimension(130, 26));
        TLukaDekubitusLokasi = new widget.TextBox(); TLukaDekubitusLokasi.setPreferredSize(new Dimension(160, 26));
        TLukaDekubitusKondisi = new widget.TextBox(); TLukaDekubitusKondisi.setPreferredSize(new Dimension(170, 26));
        gl.gridy = 2;
        gl.gridx = 0; gl.weightx = 0.22; pLukaTable.add(CekLukaDekubitus, gl);
        gl.gridx = 1; gl.weightx = 0.22; pLukaTable.add(TLukaDekubitusUkuran, gl);
        gl.gridx = 2; gl.weightx = 0.28; pLukaTable.add(TLukaDekubitusLokasi, gl);
        gl.gridx = 3; gl.weightx = 0.28; pLukaTable.add(TLukaDekubitusKondisi, gl);

        // Baris 3: Skin Lost
        CekLukaSkinLost = new ChipCheckBox("Skin Lost");
        CekLukaSkinLost.setPlainMode(true);
        TLukaSkinLostUkuran = new widget.TextBox(); TLukaSkinLostUkuran.setPreferredSize(new Dimension(130, 26));
        TLukaSkinLostLokasi = new widget.TextBox(); TLukaSkinLostLokasi.setPreferredSize(new Dimension(160, 26));
        TLukaSkinLostKondisi = new widget.TextBox(); TLukaSkinLostKondisi.setPreferredSize(new Dimension(170, 26));
        gl.gridy = 3;
        gl.gridx = 0; gl.weightx = 0.22; pLukaTable.add(CekLukaSkinLost, gl);
        gl.gridx = 1; gl.weightx = 0.22; pLukaTable.add(TLukaSkinLostUkuran, gl);
        gl.gridx = 2; gl.weightx = 0.28; pLukaTable.add(TLukaSkinLostLokasi, gl);
        gl.gridx = 3; gl.weightx = 0.28; pLukaTable.add(TLukaSkinLostKondisi, gl);

        // Baris 4: Gangren
        CekLukaGangren = new ChipCheckBox("Gangren");
        CekLukaGangren.setPlainMode(true);
        TLukaGangrenUkuran = new widget.TextBox(); TLukaGangrenUkuran.setPreferredSize(new Dimension(130, 26));
        TLukaGangrenLokasi = new widget.TextBox(); TLukaGangrenLokasi.setPreferredSize(new Dimension(160, 26));
        TLukaGangrenKondisi = new widget.TextBox(); TLukaGangrenKondisi.setPreferredSize(new Dimension(170, 26));
        gl.gridy = 4;
        gl.gridx = 0; gl.weightx = 0.22; pLukaTable.add(CekLukaGangren, gl);
        gl.gridx = 1; gl.weightx = 0.22; pLukaTable.add(TLukaGangrenUkuran, gl);
        gl.gridx = 2; gl.weightx = 0.28; pLukaTable.add(TLukaGangrenLokasi, gl);
        gl.gridx = 3; gl.weightx = 0.28; pLukaTable.add(TLukaGangrenKondisi, gl);

        // Baris 5: Lain-lain
        CekLukaLain = new ChipCheckBox("Luka Lain-lain");
        CekLukaLain.setPlainMode(true);
        TLukaLainKet = new widget.TextBox();
        TLukaLainKet.setPreferredSize(new Dimension(460, 26));
        gl.gridy = 5;
        gl.gridx = 0; gl.gridwidth = 1; gl.weightx = 0.22;
        pLukaTable.add(CekLukaLain, gl);
        gl.gridx = 1; gl.gridwidth = 3; gl.weightx = 0.78;
        pLukaTable.add(TLukaLainKet, gl);
        gl.gridwidth = 1;

        pLukaCard.add(pLukaTable, BorderLayout.CENTER);

        // Interaksi Cerdas Tabel Luka
        java.util.function.Consumer<Boolean> aturStatusLuka = aktif -> {
            CekLukaOperasi.setEnabled(aktif);
            TLukaOperasiUkuran.setEnabled(aktif);
            TLukaOperasiLokasi.setEnabled(aktif);
            TLukaOperasiKondisi.setEnabled(aktif);

            CekLukaDekubitus.setEnabled(aktif);
            TLukaDekubitusUkuran.setEnabled(aktif);
            TLukaDekubitusLokasi.setEnabled(aktif);
            TLukaDekubitusKondisi.setEnabled(aktif);

            CekLukaSkinLost.setEnabled(aktif);
            TLukaSkinLostUkuran.setEnabled(aktif);
            TLukaSkinLostLokasi.setEnabled(aktif);
            TLukaSkinLostKondisi.setEnabled(aktif);

            CekLukaGangren.setEnabled(aktif);
            TLukaGangrenUkuran.setEnabled(aktif);
            TLukaGangrenLokasi.setEnabled(aktif);
            TLukaGangrenKondisi.setEnabled(aktif);

            CekLukaLain.setEnabled(aktif);
            TLukaLainKet.setEnabled(aktif);
        };

        CekLukaTidakAda.addActionListener(e -> {
            boolean noLuka = CekLukaTidakAda.isSelected();
            if (noLuka) {
                CekLukaOperasi.setSelected(false);
                TLukaOperasiUkuran.setText("");
                TLukaOperasiLokasi.setText("");
                TLukaOperasiKondisi.setText("");

                CekLukaDekubitus.setSelected(false);
                TLukaDekubitusUkuran.setText("");
                TLukaDekubitusLokasi.setText("");
                TLukaDekubitusKondisi.setText("");

                CekLukaSkinLost.setSelected(false);
                TLukaSkinLostUkuran.setText("");
                TLukaSkinLostLokasi.setText("");
                TLukaSkinLostKondisi.setText("");

                CekLukaGangren.setSelected(false);
                TLukaGangrenUkuran.setText("");
                TLukaGangrenLokasi.setText("");
                TLukaGangrenKondisi.setText("");

                CekLukaLain.setSelected(false);
                TLukaLainKet.setText("");
                aturStatusLuka.accept(false);
            } else {
                aturStatusLuka.accept(true);
            }
        });

        java.awt.event.KeyAdapter autoOperasi = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (!TLukaOperasiUkuran.getText().trim().isEmpty() || !TLukaOperasiLokasi.getText().trim().isEmpty() || !TLukaOperasiKondisi.getText().trim().isEmpty()) {
                    CekLukaOperasi.setSelected(true);
                    CekLukaTidakAda.setSelected(false);
                    aturStatusLuka.accept(true);
                }
            }
        };
        TLukaOperasiUkuran.addKeyListener(autoOperasi);
        TLukaOperasiLokasi.addKeyListener(autoOperasi);
        TLukaOperasiKondisi.addKeyListener(autoOperasi);
        CekLukaOperasi.addActionListener(e -> {
            if (CekLukaOperasi.isSelected()) { CekLukaTidakAda.setSelected(false); aturStatusLuka.accept(true); }
        });

        java.awt.event.KeyAdapter autoDekubitus = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (!TLukaDekubitusUkuran.getText().trim().isEmpty() || !TLukaDekubitusLokasi.getText().trim().isEmpty() || !TLukaDekubitusKondisi.getText().trim().isEmpty()) {
                    CekLukaDekubitus.setSelected(true);
                    CekLukaTidakAda.setSelected(false);
                    aturStatusLuka.accept(true);
                }
            }
        };
        TLukaDekubitusUkuran.addKeyListener(autoDekubitus);
        TLukaDekubitusLokasi.addKeyListener(autoDekubitus);
        TLukaDekubitusKondisi.addKeyListener(autoDekubitus);
        CekLukaDekubitus.addActionListener(e -> {
            if (CekLukaDekubitus.isSelected()) { CekLukaTidakAda.setSelected(false); aturStatusLuka.accept(true); }
        });

        java.awt.event.KeyAdapter autoSkinLost = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (!TLukaSkinLostUkuran.getText().trim().isEmpty() || !TLukaSkinLostLokasi.getText().trim().isEmpty() || !TLukaSkinLostKondisi.getText().trim().isEmpty()) {
                    CekLukaSkinLost.setSelected(true);
                    CekLukaTidakAda.setSelected(false);
                    aturStatusLuka.accept(true);
                }
            }
        };
        TLukaSkinLostUkuran.addKeyListener(autoSkinLost);
        TLukaSkinLostLokasi.addKeyListener(autoSkinLost);
        TLukaSkinLostKondisi.addKeyListener(autoSkinLost);
        CekLukaSkinLost.addActionListener(e -> {
            if (CekLukaSkinLost.isSelected()) { CekLukaTidakAda.setSelected(false); aturStatusLuka.accept(true); }
        });

        java.awt.event.KeyAdapter autoGangren = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (!TLukaGangrenUkuran.getText().trim().isEmpty() || !TLukaGangrenLokasi.getText().trim().isEmpty() || !TLukaGangrenKondisi.getText().trim().isEmpty()) {
                    CekLukaGangren.setSelected(true);
                    CekLukaTidakAda.setSelected(false);
                    aturStatusLuka.accept(true);
                }
            }
        };
        TLukaGangrenUkuran.addKeyListener(autoGangren);
        TLukaGangrenLokasi.addKeyListener(autoGangren);
        TLukaGangrenKondisi.addKeyListener(autoGangren);
        CekLukaGangren.addActionListener(e -> {
            if (CekLukaGangren.isSelected()) { CekLukaTidakAda.setSelected(false); aturStatusLuka.accept(true); }
        });

        TLukaLainKet.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (!TLukaLainKet.getText().trim().isEmpty()) {
                    CekLukaLain.setSelected(true);
                    CekLukaTidakAda.setSelected(false);
                    aturStatusLuka.accept(true);
                }
            }
        });
        CekLukaLain.addActionListener(e -> {
            if (CekLukaLain.isSelected()) {
                CekLukaTidakAda.setSelected(false);
                aturStatusLuka.accept(true);
                TLukaLainKet.requestFocus();
            }
        });

        body.add(buatBarisDuaKolom("Luka / Integritas Kulit", pLukaCard, false));

        JPanel cardC = createSectionCard(2, "03", SECTION_TITLES[2], SECTION_SUBTITLES[2], null, body);
        SectionsWrapper.add(cardC);
        sectionStruts[2] = Box.createVerticalStrut(16);
        SectionsWrapper.add(sectionStruts[2]);
    }

    private void rakitSeksiD() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // 1. Tindakan / Kebutuhan Khusus
        CekTindakanResikoJatuh = new ChipCheckBox("Resiko Jatuh");
        CekTindakanPemasanganRestrain = new ChipCheckBox("Pemasangan Restrain");
        CekTindakanPerawatanLuka = new ChipCheckBox("Perawatan Luka");
        CekTindakanHygiene = new ChipCheckBox("Hygiene");
        body.add(buatBarisDuaKolom("Tindakan / Kebutuhan Khusus",
            flowIsian(CekTindakanResikoJatuh, CekTindakanPemasanganRestrain, CekTindakanPerawatanLuka, CekTindakanHygiene), true));

        // 2. Riwayat Alergi
        RiwayatAlergi = new widget.ComboBox();
        RiwayatAlergi.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Tidak", "Ya"}));
        RiwayatAlergi.setPreferredSize(new Dimension(80, 24));

        RadioPill pillAlergiTidak = new RadioPill("Tidak", bgAlergi);
        pillAlergiTidak.setSelected(true);
        pillAlergiTidak.addActionListener(e -> RiwayatAlergi.setSelectedItem("Tidak"));

        RadioPill pillAlergiYa = new RadioPill("Ya", bgAlergi);
        pillAlergiYa.addActionListener(e -> RiwayatAlergi.setSelectedItem("Ya"));
        body.add(buatBarisDuaKolom("Riwayat Alergi", flowIsian(pillAlergiTidak, pillAlergiYa), true));

        TJenisAlergi = new widget.TextBox(); TJenisAlergi.setPreferredSize(new Dimension(200, 24));
        TReaksiAlergi = new widget.TextBox(); TReaksiAlergi.setPreferredSize(new Dimension(200, 24));
        body.add(buatBarisDuaKolom("Jenis & Reaksi Alergi",
            flowIsian(subLabel("Jenis"), TJenisAlergi, subLabel("Reaksi"), TReaksiAlergi), true));

        // 3. Alat Bantu
        CekAlatBantuTanpa = new ChipCheckBox("Tanpa Alat Bantu");
        CekAlatBantuDengar = new ChipCheckBox("Alat Bantu Dengar");
        CekAlatBantuKacaMata = new ChipCheckBox("Kaca Mata");
        CekAlatBantuGigiPalsu = new ChipCheckBox("Gigi Palsu");
        CekAlatBantuTongkatKruk = new ChipCheckBox("Tongkat/Kruk");
        CekAlatBantuLain = new ChipCheckBox("Lain-lain");
        body.add(buatBarisDuaKolom("Alat Bantu",
            flowIsian(CekAlatBantuTanpa, CekAlatBantuDengar, CekAlatBantuKacaMata, CekAlatBantuGigiPalsu, CekAlatBantuTongkatKruk, CekAlatBantuLain), true));

        TAlatBantuLainKet = new widget.TextBox(); TAlatBantuLainKet.setPreferredSize(new Dimension(220, 24));
        body.add(buatBarisDuaKolom("Keterangan Alat Bantu Lain", flowIsian(TAlatBantuLainKet), false));

        JPanel cardD = createSectionCard(3, "04", SECTION_TITLES[3], SECTION_SUBTITLES[3], null, body);
        SectionsWrapper.add(cardD);
        sectionStruts[3] = Box.createVerticalStrut(16);
        SectionsWrapper.add(sectionStruts[3]);
    }

    private void rakitSeksiE() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // 1. Tindakan yang sudah dilakukan & Therapy
        TindakanYangSudahDilakukan = new widget.TextArea();
        body.add(buatBarisDuaKolom("Tindakan yang Sudah Dilakukan", areaIsian(TindakanYangSudahDilakukan, 56), true));

        ObatYangDiberikan = new widget.TextArea();
        body.add(buatBarisDuaKolom("Therapy yang Diberikan", areaIsian(ObatYangDiberikan, 56), true));

        // 2. Pemeriksaan Penunjang
        JPanel pPenunjang = new JPanel(new GridLayout(2, 2, 12, 6));
        pPenunjang.setOpaque(false);

        CekPenunjangLaboratorium = new ChipCheckBox("Laboratorium");
        TPenunjangLaboratoriumKet = new widget.TextBox(); TPenunjangLaboratoriumKet.setPreferredSize(new Dimension(180, 24));
        JPanel pLab = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); pLab.setOpaque(false);
        pLab.add(CekPenunjangLaboratorium); pLab.add(TPenunjangLaboratoriumKet);
        pPenunjang.add(pLab);

        CekPenunjangRadiologi = new ChipCheckBox("Radiologi");
        TPenunjangRadiologiKet = new widget.TextBox(); TPenunjangRadiologiKet.setPreferredSize(new Dimension(180, 24));
        JPanel pRad = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); pRad.setOpaque(false);
        pRad.add(CekPenunjangRadiologi); pRad.add(TPenunjangRadiologiKet);
        pPenunjang.add(pRad);

        CekPenunjangCtScan = new ChipCheckBox("CT Scan");
        TPenunjangCtScanKet = new widget.TextBox(); TPenunjangCtScanKet.setPreferredSize(new Dimension(180, 24));
        JPanel pCt = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); pCt.setOpaque(false);
        pCt.add(CekPenunjangCtScan); pCt.add(TPenunjangCtScanKet);
        pPenunjang.add(pCt);

        CekPenunjangLain = new ChipCheckBox("Penunjang Lain");
        TPenunjangLainKet = new widget.TextBox(); TPenunjangLainKet.setPreferredSize(new Dimension(180, 24));
        JPanel pLain = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); pLain.setOpaque(false);
        pLain.add(CekPenunjangLain); pLain.add(TPenunjangLainKet);
        pPenunjang.add(pLain);

        body.add(buatBarisDuaKolom("Pemeriksaan Penunjang", pPenunjang, true));

        // 3. Investigasi Abnormal, Rencana Tindak Lanjut, Masalah Utama
        HasilInvestigasiAbnormal = new widget.TextArea();
        body.add(buatBarisDuaKolom("Hasil Investigasi Abnormal", areaIsian(HasilInvestigasiAbnormal, 48), true));

        RencanaTindakLanjut = new widget.TextArea();
        body.add(buatBarisDuaKolom("Rencana Tindak Lanjut", areaIsian(RencanaTindakLanjut, 48), true));

        MasalahUtamaKeperawatan = new widget.TextArea();
        body.add(buatBarisDuaKolom("Masalah Utama Keperawatan", areaIsian(MasalahUtamaKeperawatan, 48), true));

        // 4. Dokumen yang diserahterimakan
        JPanel pDok = new JPanel(new BorderLayout(0, 4));
        pDok.setOpaque(false);

        JPanel pDokHead = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pDokHead.setOpaque(false);

        // Helper chips to append common documents
        String[] dokChips = {"Berkas rekam medis", "Hasil laboratorium", "Foto rontgen / CT", "Obat pasien", "Informed consent"};
        for (String dc : dokChips) {
            ChipCheckBox ckb = new ChipCheckBox(dc);
            ckb.addActionListener(ev -> {
                String cur = DokumenYangDiserahterimakan.getText();
                if (ckb.isSelected()) {
                    if (!cur.contains(dc)) {
                        DokumenYangDiserahterimakan.setText(cur.isEmpty() ? dc : cur + ", " + dc);
                    }
                } else {
                    cur = cur.replace(dc + ", ", "").replace(", " + dc, "").replace(dc, "");
                    DokumenYangDiserahterimakan.setText(cur.trim());
                }
            });
            pDokHead.add(ckb);
        }
        pDok.add(pDokHead, BorderLayout.NORTH);

        DokumenYangDiserahterimakan = new widget.TextArea();
        pDok.add(areaIsian(DokumenYangDiserahterimakan, 48), BorderLayout.CENTER);
        body.add(buatBarisDuaKolom("Dokumen yang Diserahterimakan", pDok, false));

        JPanel cardE = createSectionCard(4, "05", SECTION_TITLES[4], SECTION_SUBTITLES[4], null, body);
        SectionsWrapper.add(cardE);
    }

    private JLabel labelField(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 10));
        lbl.setForeground(TEXT_LABEL_MUTED);
        return lbl;
    }

    // =================================================================
    //  TAB 2: DATA TRANSFER PASIEN & ACCORDION PREVIEW
    // =================================================================

    private JPanel rakitTabData() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PAGE);

        // Toolbar Bawah: Periode & Pencarian (sesuai standar form IGD)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
        toolbar.setBackground(CARD_BG);
        toolbar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_GRAY));
        toolbar.setPreferredSize(new Dimension(100, 42));

        toolbar.add(labelHeader("PERIODE"));
        DTPCari1 = new widget.Tanggal();
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setPreferredSize(new Dimension(100, 24));
        toolbar.add(DTPCari1);

        JLabel sd = new JLabel("s.d.");
        sd.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));
        toolbar.add(sd);

        DTPCari2 = new widget.Tanggal();
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setPreferredSize(new Dimension(100, 24));
        toolbar.add(DTPCari2);

        toolbar.add(labelHeader("CARI"));
        TCari = new widget.TextBox();
        TCari.setPreferredSize(new Dimension(200, 24));
        toolbar.add(TCari);

        BtnCari = new widget.Button();
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setText("Cari");
        BtnCari.setPreferredSize(new Dimension(100, 26));
        BtnCari.setMargin(new Insets(2, 6, 2, 6));
        BtnCari.addActionListener(e -> runBackground(() -> tampil()));
        toolbar.add(BtnCari);

        BtnAll = new widget.Button();
        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setText("Semua");
        BtnAll.setPreferredSize(new Dimension(105, 26));
        BtnAll.setMargin(new Insets(2, 6, 2, 6));
        BtnAll.addActionListener(e -> {
            TCari.setText("");
            runBackground(() -> tampil());
        });
        toolbar.add(BtnAll);

        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(labelHeader("RECORD:"));
        LCount = new widget.Label();
        LCount.setFont(new Font("Monospaced", Font.BOLD, 12));
        LCount.setForeground(TEAL_ACCENT);
        LCount.setText("0");
        toolbar.add(LCount);

        ChkAccor = new widget.CekBox();
        ChkAccor.setText("Pratinjau & TTD");

        // Tabel Data
        tbObat = new widget.Table();
        tbObat.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (tbObat.getSelectedRow() != -1) {
                    bukaPratinjauHtml();
                    if (e.getClickCount() == 2) {
                        TabRawat.setSelectedIndex(0);
                    }
                }
            }
        });
        tbObat.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (tbObat.getSelectedRow() != -1) {
                        bukaPratinjauHtml();
                        TabRawat.setSelectedIndex(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    SwingUtilities.invokeLater(() -> {
                        if (tbObat.getSelectedRow() != -1) {
                            bukaPratinjauHtml();
                        }
                    });
                }
            }
            @Override public void keyReleased(KeyEvent e) {}
        });

        widget.ScrollPane spTable = new widget.ScrollPane();
        spTable.setViewportView(tbObat);

        // Panel Accordion Preview Ringkasan & TTD (Tertutup default, langsung terbuka saat klik baris pasien)
        PanelAccor = new widget.PanelBiasa();
        PanelAccor.setLayout(new BorderLayout());
        PanelAccor.setPreferredSize(new Dimension(520, 400));
        PanelAccor.setVisible(false);

        FormPhoto = new widget.PanelBiasa();
        FormPhoto.setLayout(new BorderLayout());

        // TabRingkasanTtd
        TabRingkasanTtd = new javax.swing.JTabbedPane();
        TabRingkasanTtd.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 11));

        // Sub-tab 1: Ringkasan Inputan HTML
        JPanel pRingkas = new JPanel(new BorderLayout());
        JPanel pRingkasBar = new JPanel(new BorderLayout());
        pRingkasBar.setBackground(CARD_HEADER_BG);
        pRingkasBar.setBorder(new EmptyBorder(4, 8, 4, 8));
        JLabel lblRingkas = new JLabel("Ringkasan Data Lembar Transfer");
        lblRingkas.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        pRingkasBar.add(lblRingkas, BorderLayout.WEST);

        JPanel pRingkasActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pRingkasActions.setOpaque(false);

        BtnPrint1 = new widget.Button();
        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png")));
        BtnPrint1.setText("Cetak Lembar");
        BtnPrint1.setPreferredSize(new Dimension(120, 24));
        BtnPrint1.addActionListener(e -> BtnPrint1ActionPerformed(e));
        pRingkasActions.add(BtnPrint1);

        widget.Button btnTutupPratinjau = new widget.Button();
        btnTutupPratinjau.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        btnTutupPratinjau.setText("Tutup");
        btnTutupPratinjau.setPreferredSize(new Dimension(80, 24));
        btnTutupPratinjau.addActionListener(e -> {
            PanelAccor.setVisible(false);
            p.revalidate();
            p.repaint();
        });
        pRingkasActions.add(btnTutupPratinjau);
        pRingkasBar.add(pRingkasActions, BorderLayout.EAST);
        pRingkas.add(pRingkasBar, BorderLayout.NORTH);

        LoadHTML = new widget.editorpane();
        ScrollHTML = new widget.ScrollPane();
        ScrollHTML.setViewportView(LoadHTML);
        pRingkas.add(ScrollHTML, BorderLayout.CENTER);
        TabRingkasanTtd.addTab("Data Inputan", pRingkas);

        // Sub-tab 2: Bukti Foto / TTD
        JPanel pTtd = new JPanel(new BorderLayout());
        JPanel pTtdBar = new JPanel(new BorderLayout());
        pTtdBar.setBackground(CARD_HEADER_BG);
        pTtdBar.setBorder(new EmptyBorder(4, 8, 4, 8));
        JLabel lblTtd = new JLabel("Bukti Tanda Tangan Elektronik");
        lblTtd.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.BOLD, 11));
        pTtdBar.add(lblTtd, BorderLayout.WEST);

        JPanel pTtdActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pTtdActions.setOpaque(false);

        btnAmbil = new widget.Button();
        btnAmbil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png")));
        btnAmbil.setText("Ambil TTD");
        btnAmbil.setToolTipText("Kirim data transfer ini ke antrean tablet/web untuk tanda tangan digital");
        btnAmbil.setPreferredSize(new Dimension(105, 24));
        btnAmbil.addActionListener(e -> ambilTtdAntrian());
        pTtdActions.add(btnAmbil);

        BtnRefreshPhoto1 = new widget.Button();
        BtnRefreshPhoto1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh.png")));
        BtnRefreshPhoto1.setText("Refresh TTD");
        BtnRefreshPhoto1.setToolTipText("Muat ulang tanda tangan digital terbaru dari server");
        BtnRefreshPhoto1.setPreferredSize(new Dimension(110, 24));
        BtnRefreshPhoto1.addActionListener(e -> {
            if (tbObat.getSelectedRow() > -1) {
                panggilPhoto();
            } else {
                JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data transfer terlebih dahulu..!!");
            }
        });
        pTtdActions.add(BtnRefreshPhoto1);

        widget.Button btnTutupTtd = new widget.Button();
        btnTutupTtd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        btnTutupTtd.setText("Tutup");
        btnTutupTtd.setPreferredSize(new Dimension(80, 24));
        btnTutupTtd.addActionListener(e -> {
            PanelAccor.setVisible(false);
            p.revalidate();
            p.repaint();
        });
        pTtdActions.add(btnTutupTtd);

        pTtdBar.add(pTtdActions, BorderLayout.EAST);
        pTtd.add(pTtdBar, BorderLayout.NORTH);

        LoadHTML2 = new widget.editorpane();
        widget.ScrollPane spTtd = new widget.ScrollPane();
        spTtd.setViewportView(LoadHTML2);
        pTtd.add(spTtd, BorderLayout.CENTER);
        TabRingkasanTtd.addTab("Tanda Tangan & Bukti", pTtd);

        FormPhoto.add(TabRingkasanTtd, BorderLayout.CENTER);
        PanelAccor.add(FormPhoto, BorderLayout.CENTER);

        // Pasang Tabel + Accordion ke Tab
        JPanel splitTable = new JPanel(new BorderLayout());
        splitTable.add(spTable, BorderLayout.CENTER);
        splitTable.add(PanelAccor, BorderLayout.EAST);
        p.add(splitTable, BorderLayout.CENTER);
        p.add(toolbar, BorderLayout.SOUTH);

        return p;
    }

    private void bukaPratinjauHtml() {
        if (tbObat.getSelectedRow() == -1) return;
        PanelAccor.setVisible(true);
        FormPhoto.setVisible(true);
        PanelAccor.setPreferredSize(new Dimension(520, getHeight()));
        getData();
        panggilPhoto();
        PanelAccor.revalidate();
        PanelAccor.repaint();
    }

    // =================================================================
    //  ACTION BAR BAWAH 56px
    // =================================================================

    private JPanel rakitActionBar() {
        JPanel bar = new JPanel(new BorderLayout(15, 0));
        bar.setPreferredSize(new Dimension(100, 56));
        bar.setBackground(CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER_GRAY),
            new EmptyBorder(0, 18, 0, 18)
        ));

        // Tombol aksi transaksi di sebelah KIRI (sesuai standar form IGD)
        JPanel pLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
        pLeft.setOpaque(false);

        BtnSimpan = new widget.Button();
        BtnSimpan.setText("Simpan");
        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setPreferredSize(new Dimension(100, 32));
        BtnSimpan.setMargin(new Insets(2, 6, 2, 6));
        BtnSimpan.setRoundRect(false);
        BtnSimpan.addActionListener(e -> simpan());
        pLeft.add(BtnSimpan);

        BtnBatal = new widget.Button();
        BtnBatal.setText("Baru");
        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBatal.setPreferredSize(new Dimension(100, 32));
        BtnBatal.setMargin(new Insets(2, 6, 2, 6));
        BtnBatal.setRoundRect(false);
        BtnBatal.addActionListener(e -> emptTeks());
        pLeft.add(BtnBatal);

        BtnEdit = new widget.Button();
        BtnEdit.setText("Ganti");
        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png")));
        BtnEdit.setPreferredSize(new Dimension(100, 32));
        BtnEdit.setMargin(new Insets(2, 6, 2, 6));
        BtnEdit.setRoundRect(false);
        BtnEdit.addActionListener(e -> ganti());
        pLeft.add(BtnEdit);

        BtnHapus = new widget.Button();
        BtnHapus.setText("Hapus");
        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setPreferredSize(new Dimension(100, 32));
        BtnHapus.setMargin(new Insets(2, 6, 2, 6));
        BtnHapus.setRoundRect(false);
        BtnHapus.addActionListener(e -> hapus());
        pLeft.add(BtnHapus);

        BtnPrint = new widget.Button();
        BtnPrint.setText("Cetak RM 04");
        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setPreferredSize(new Dimension(135, 32));
        BtnPrint.setMargin(new Insets(2, 6, 2, 6));
        BtnPrint.setRoundRect(false);
        BtnPrint.addActionListener(e -> BtnPrintActionPerformed(e));
        pLeft.add(BtnPrint);

        bar.add(pLeft, BorderLayout.WEST);

        // Status bar & tombol Keluar di sebelah KANAN
        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pRight.setOpaque(false);

        JLabel iconStatus = new JLabel("\u21C4");
        iconStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconStatus.setForeground(TEAL_ACCENT);
        pRight.add(iconStatus);

        LblStatusBar = new JLabel("Draft transfer aktif \u00B7 Pasien belum dipilih");
        LblStatusBar.setFont(new Font(ThemeConfig.FONT_FAMILY, Font.PLAIN, 12));
        LblStatusBar.setForeground(new Color(0x6c, 0x73, 0x6e));
        pRight.add(LblStatusBar);

        pRight.add(Box.createHorizontalStrut(8));

        BtnKeluar = new widget.Button();
        BtnKeluar.setText("Keluar");
        BtnKeluar.setRoundRect(false);
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setPreferredSize(new Dimension(100, 32));
        BtnKeluar.setMargin(new Insets(2, 6, 2, 6));
        BtnKeluar.addActionListener(e -> dispose());
        pRight.add(BtnKeluar);

        bar.add(pRight, BorderLayout.EAST);
        return bar;
    }

    // =================================================================
    //  LISTENERS & PEMBAHARUAN STATE DINAMIS
    // =================================================================

    private void pasangListeners() {
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateProgress(); updateAlurDisplay(); }
            @Override public void removeUpdate(DocumentEvent e) { updateProgress(); updateAlurDisplay(); }
            @Override public void changedUpdate(DocumentEvent e) { updateProgress(); updateAlurDisplay(); }
        };

        TNoRw.getDocument().addDocumentListener(dl);
        AsalRuang.getDocument().addDocumentListener(dl);
        RuangSelanjutnya.getDocument().addDocumentListener(dl);
        DiagnosaUtama.getDocument().addDocumentListener(dl);
        NamaMenyetujui.getDocument().addDocumentListener(dl);
        TDTransfer.getDocument().addDocumentListener(dl);
        HRTransfer.getDocument().addDocumentListener(dl);
        RRTransfer.getDocument().addDocumentListener(dl);
        SuhuTransfer.getDocument().addDocumentListener(dl);
    }

    private void updateProgress() {
        int total = 12;
        int count = 0;
        if (!TNoRw.getText().trim().isEmpty()) count++;
        if (!AsalRuang.getText().trim().isEmpty()) count++;
        if (!RuangSelanjutnya.getText().trim().isEmpty()) count++;
        if (!DiagnosaUtama.getText().trim().isEmpty()) count++;
        if (!KdPetugasMenyerahkan.getText().trim().isEmpty()) count++;
        if (!KdPetugasMenerima.getText().trim().isEmpty()) count++;
        if (KesadaranTransfer.getSelectedIndex() > 0) count++;
        if (!TDTransfer.getText().trim().isEmpty()) count++;
        if (!HRTransfer.getText().trim().isEmpty()) count++;
        if (!SuhuTransfer.getText().trim().isEmpty()) count++;
        if (!NamaMenyetujui.getText().trim().isEmpty()) count++;
        if (SudahDijelaskanDiagnosa.getSelectedIndex() >= 0) count++;

        int pct = (count * 100) / total;
        LblProgressPct.setText(pct + "%");
        ProgressBarFill.setBounds(0, 0, (260 * pct) / 100, 6);
        LblProgressNote.setText(count + " dari " + total + " isian penting terisi");

        if (pct == 100) {
            ProgressBarFill.setBackground(new Color(0x1a, 0x6b, 0x4c));
            LblProgressPct.setForeground(new Color(0x1a, 0x6b, 0x4c));
        } else {
            ProgressBarFill.setBackground(TEAL_ACCENT);
            LblProgressPct.setForeground(TEAL_ACCENT);
        }

        // Update nav checks
        navChecks[0].setForeground(!AsalRuang.getText().isEmpty() && !RuangSelanjutnya.getText().isEmpty() ? GREEN_FG : BORDER_GRAY);
        navChecks[1].setForeground(!TDTransfer.getText().isEmpty() && KesadaranTransfer.getSelectedIndex() > 0 ? GREEN_FG : BORDER_GRAY);
        navChecks[2].setForeground(RbLineTidakAda.isSelected() || RbLineCvpPicc.isSelected() || RbLineIvLine.isSelected() || CekLukaTidakAda.isSelected() || CekLukaOperasi.isSelected() ? GREEN_FG : BORDER_GRAY);
        navChecks[3].setForeground(RiwayatAlergi.getSelectedIndex() >= 0 ? GREEN_FG : BORDER_GRAY);
        navChecks[4].setForeground(!RencanaTindakLanjut.getText().isEmpty() || !TindakanYangSudahDilakukan.getText().isEmpty() ? GREEN_FG : BORDER_GRAY);
    }

    private void updateAlurDisplay() {
        String ar = AsalRuang.getText().trim().isEmpty() ? "-" : AsalRuang.getText().trim();
        String kr = KelasAsalRuang.getText().trim().isEmpty() ? "" : " (" + KelasAsalRuang.getText().trim() + ")";
        LblAlurAsal.setText("Asal: " + ar);
        LblAlurKelasAsal.setText("Kelas: " + (kr.isEmpty() ? "-" : kr));

        String tr = RuangSelanjutnya.getText().trim().isEmpty() ? "-" : RuangSelanjutnya.getText().trim();
        String ktr = KelasRuangSelanjutnya.getText().trim().isEmpty() ? "" : " (" + KelasRuangSelanjutnya.getText().trim() + ")";
        LblAlurTujuan.setText("Tujuan: " + tr);
        LblAlurKelasTujuan.setText("Kelas: " + (ktr.isEmpty() ? "-" : ktr));

        String cp = MetodePemindahan.getSelectedItem() != null ? MetodePemindahan.getSelectedItem().toString() : "-";
        LblAlurCara.setText("Cara Pindah: " + cp);

        if (!TNoRw.getText().trim().isEmpty()) {
            LblStatusBar.setText("Transfer: " + TNoRw.getText() + " \u00B7 Dari " + ar + " ke " + tr);
        } else {
            LblStatusBar.setText("Draft transfer aktif \u00B7 Pasien belum dipilih");
        }
    }

    private void updatePatientCard() {
        String nm = TPasien.getText().trim();
        LblNmPasien.setText(nm.isEmpty() ? "-" : nm);
        LblInfoPasien.setText(Jk.getText() + " \u00B7 " + TglLahir.getText());
        LblNoRM.setText("No. RM: " + TNoRM.getText());
        LblNoRw.setText("No. Rawat: " + TNoRw.getText());
        LblLokasi.setText("Lokasi: " + (AsalRuang.getText().isEmpty() ? "-" : AsalRuang.getText()));

        if (!nm.isEmpty()) {
            String[] parts = nm.split("\\s+");
            if (parts.length >= 2) {
                LblAvatar.setText(("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase());
            } else if (parts[0].length() >= 2) {
                LblAvatar.setText(parts[0].substring(0, 2).toUpperCase());
            } else {
                LblAvatar.setText(parts[0].toUpperCase());
            }
        } else {
            LblAvatar.setText("PS");
        }
    }

    private void syncRadioPill(ButtonGroup bg, String val) {
        if (val == null || val.trim().isEmpty()) {
            bg.clearSelection();
            return;
        }
        for (Enumeration<AbstractButton> e = bg.getElements(); e.hasMoreElements(); ) {
            AbstractButton b = e.nextElement();
            if (b.getText().equalsIgnoreCase(val.trim())) {
                b.setSelected(true);
                return;
            }
        }
        bg.clearSelection();
    }

    // =================================================================
    //  OPERASI DATABASE (SIMPAN, GANTI, HAPUS, GETDATA, TAMPIL)
    // =================================================================

    private String ya(AbstractButton c) {
        return c != null && c.isSelected() ? "Ya" : "Tidak";
    }

    private String comboValOrNull(widget.ComboBox c) {
        if (c == null) return null;
        Object v = c.getSelectedItem();
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private String txtOrNull(widget.TextBox t) {
        if (t == null) return null;
        String s = t.getText();
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private String[] nilaiTransfer() {
        String lineInvasif = RbLineTidakAda.isSelected() ? "Tidak Ada Line Invasive" :
            (RbLineCvpPicc.isSelected() ? "CVP/PICC" : (RbLineIvLine.isSelected() ? "IV Line" : null));
        String lineInvasifTanggal = (RbLineCvpPicc.isSelected() || RbLineIvLine.isSelected()) ?
            LineInvasifTanggal.getTanggalDanJamSql(Valid) : null;

        String eliminasiBab = RbEliminasiBabNormal.isSelected() ? "Normal" :
            (RbEliminasiBabDiareKonstipasi.isSelected() ? "Diare/Konstipasi" :
            (RbEliminasiBabIleustomyColostomy.isSelected() ? "Ileustomy/Colostomy" :
            (RbEliminasiBabInkontinensiaAlvi.isSelected() ? "Inkontinensia Alvi" : null)));

        String eliminasiBak = RbEliminasiBakNormal.isSelected() ? "Normal" :
            (RbEliminasiBakInkontinensiaUrine.isSelected() ? "Inkontinensia Urine" :
            (RbEliminasiBakKateterUrine.isSelected() ? "Kateter Urine" : null));
        String kateterTanggal = RbEliminasiBakKateterUrine.isSelected() ?
            DTPEliminasiBakKateterTanggalPasang.getTanggalDanJamSql(Valid) : null;

        String aktifitas = RbAktifitasMandiri.isSelected() ? "Mandiri" :
            (RbAktifitasDibantuSebagian.isSelected() ? "Dibantu Sebagian" : (RbAktifitasDibantuPenuh.isSelected() ? "Dibantu Penuh" : null));
        String mobilisasi = RbMobilisasiTirahBaring.isSelected() ? "Tirah Baring" :
            (RbMobilisasiDuduk.isSelected() ? "Duduk" : (RbMobilisasiJalan.isSelected() ? "Jalan" : null));

        return new String[]{
            TNoRw.getText(),
            TanggalMasuk.getTanggalDanJamSql(Valid),
            TanggalPindah.getTanggalDanJamSql(Valid),
            AsalRuang.getText(),RuangSelanjutnya.getText(),DiagnosaUtama.getText(),DiagnosaSekunder.getText(),
            IndikasiPindah.getSelectedItem().toString(),KeteranganIndikasiPindahRuang.getText(),
            ProsedurDilakukan.getText(),ObatYangDiberikan.getText(),MetodePemindahan.getSelectedItem().toString(),
            MenyetujuiPemindahan.getSelectedItem().toString(),NamaMenyetujui.getText(),HubunganMenyetujui.getSelectedItem().toString(),
            KdPetugasMenyerahkan.getText(),KdPetugasMenerima.getText(),

            TNoTlpMenyetujui.getText(),comboValOrNull(SudahDijelaskanDiagnosa),txtOrNull(KelasAsalRuang),txtOrNull(KelasRuangSelanjutnya),
            KdDokterMerawatAsal1.getText(),KdDokterMerawatAsal2.getText(),KdDokterMerawatAsal3.getText(),
            KdDokterMerawatTujuan1.getText(),KdDokterMerawatTujuan2.getText(),KdDokterMerawatTujuan3.getText(),

            comboValOrNull(KesadaranTransfer),TDTransfer.getText(),HRTransfer.getText(),RRTransfer.getText(),SuhuTransfer.getText(),
            GCSTransfer.getText(),SpO2Transfer.getText(),comboValOrNull(TingkatNyeriTransfer),
            ObservasiTerakhirPukul.getTanggalDanJamSql(Valid),

            comboValOrNull(OksigenStatus),ya(CekOksigenNasalKanule),ya(CekOksigenNasalKateter),ya(CekOksigenFaceMask),
            ya(CekOksigenSungkupRebreathing),ya(CekOksigenSungkupNonRebreathing),ya(CekOksigenTracheostomy),ya(CekOksigenEtt),

            lineInvasif,lineInvasifTanggal,
            ya(CekKewaspadaanStandar),ya(CekKewaspadaanKontak),ya(CekKewaspadaanAirborne),ya(CekKewaspadaanDroplet),

            ya(CekNutrisiPuasa),ya(CekNutrisiOral),ya(CekNutrisiNgt),
            (CekNutrisiBatasanCairan.isSelected() ? TNutrisiBatasanCairanCc.getText() : (TNutrisiBatasanCairanCc.getText().trim().isEmpty() ? "" : TNutrisiBatasanCairanCc.getText())),
            ya(CekNutrisiDietKhusus),TNutrisiDietKhususJelaskan.getText(),

            eliminasiBab,eliminasiBak,TEliminasiBakKateterNo.getText(),kateterTanggal,

            aktifitas,mobilisasi,ya(CekDrainaseWsd),ya(CekDrainaseDrainLuka),ya(CekDrainaseStoma),ya(CekDrainaseLain),TDrainaseLainKet.getText(),

            ya(CekLukaTidakAda),
            ya(CekLukaOperasi),TLukaOperasiUkuran.getText(),TLukaOperasiLokasi.getText(),TLukaOperasiKondisi.getText(),
            ya(CekLukaDekubitus),TLukaDekubitusUkuran.getText(),TLukaDekubitusLokasi.getText(),TLukaDekubitusKondisi.getText(),
            ya(CekLukaSkinLost),TLukaSkinLostUkuran.getText(),TLukaSkinLostLokasi.getText(),TLukaSkinLostKondisi.getText(),
            ya(CekLukaGangren),TLukaGangrenUkuran.getText(),TLukaGangrenLokasi.getText(),TLukaGangrenKondisi.getText(),
            ya(CekLukaLain),TLukaLainKet.getText(),

            HasilInvestigasiAbnormal.getText(),

            ya(CekTindakanResikoJatuh),ya(CekTindakanPemasanganRestrain),ya(CekTindakanPerawatanLuka),ya(CekTindakanHygiene),
            comboValOrNull(RiwayatAlergi),TJenisAlergi.getText(),TReaksiAlergi.getText(),

            ya(CekAlatBantuTanpa),ya(CekAlatBantuDengar),ya(CekAlatBantuKacaMata),ya(CekAlatBantuGigiPalsu),ya(CekAlatBantuTongkatKruk),
            ya(CekAlatBantuLain),TAlatBantuLainKet.getText(),

            TindakanYangSudahDilakukan.getText(),

            ya(CekPenunjangLaboratorium),TPenunjangLaboratoriumKet.getText(),ya(CekPenunjangRadiologi),TPenunjangRadiologiKet.getText(),
            ya(CekPenunjangCtScan),TPenunjangCtScanKet.getText(),ya(CekPenunjangLain),TPenunjangLainKet.getText(),

            RencanaTindakLanjut.getText(),MasalahUtamaKeperawatan.getText(),DokumenYangDiserahterimakan.getText()
        };
    }

    private void simpan() {
        if (TNoRw.getText().trim().isEmpty()) {
            Valid.textKosong(TNoRw, "Nomor Rawat Pasien");
            return;
        }
        if (AsalRuang.getText().trim().isEmpty()) {
            Valid.textKosong(AsalRuang, "Ruangan Asal");
            return;
        }
        if (RuangSelanjutnya.getText().trim().isEmpty()) {
            Valid.textKosong(RuangSelanjutnya, "Ruangan Tujuan");
            return;
        }

        StringBuilder kolom = new StringBuilder();
        StringBuilder placeholder = new StringBuilder();
        for (String k : KOLOM_TRANSFER) {
            if (kolom.length() > 0) { kolom.append(","); placeholder.append(","); }
            kolom.append(k); placeholder.append("?");
        }
        String[] param = nilaiTransfer();

        boolean sukses = false;
        try {
            PreparedStatement psSimpan = koneksi.prepareStatement(
                "insert into transfer_pasien_antar_ruang (" + kolom + ") values (" + placeholder + ")");
            try {
                for (int idx = 0; idx < param.length; idx++) {
                    psSimpan.setString(idx + 1, param[idx]);
                }
                psSimpan.executeUpdate();
                sukses = true;
            } finally {
                if (psSimpan != null) psSimpan.close();
            }
        } catch (Exception e) {
            System.out.println("Notif Simpan RM 04 : " + e);
            JOptionPane.showMessageDialog(null, "Maaf, gagal menyimpan data. Kemungkinan No.Rawat & Tanggal Masuk yang sama sudah dimasukkan sebelumnya...!");
        }

        if (sukses) {
            tabMode.addRow(new Object[]{
                TNoRw.getText(),TNoRM.getText(),TPasien.getText(),TglLahir.getText(),Jk.getText(),
                TanggalMasuk.getTanggalDanJamSql(Valid),
                TanggalPindah.getTanggalDanJamSql(Valid),
                DiagnosaUtama.getText(),KdPetugasMenyerahkan.getText(),NmPetugasMenyerahkan.getText(),KdPetugasMenerima.getText(),NmPetugasMenerima.getText()
            });
            LCount.setText("" + tabMode.getRowCount());
            JOptionPane.showMessageDialog(null, "Proses simpan data transfer pasien berhasil!");
            emptTeks();
        }
    }

    private void ganti() {
        if (tbObat.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Silakan pilih data transfer yang ingin diganti terlebih dahulu!");
            return;
        }

        StringBuilder set = new StringBuilder();
        for (String kolom : KOLOM_TRANSFER) {
            set.append(kolom).append("=?,");
        }
        set.setLength(set.length() - 1);

        String[] nilai = nilaiTransfer();
        String[] param = new String[nilai.length + 2];
        System.arraycopy(nilai, 0, param, 0, nilai.length);
        param[nilai.length] = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        param[nilai.length + 1] = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();

        if (Sequel.mengedittf("transfer_pasien_antar_ruang", "no_rawat=? and tanggal_masuk=?", set.toString(), param.length, param)) {
            int row = tbObat.getSelectedRow();
            tbObat.setValueAt(TNoRw.getText(), row, 0);
            tbObat.setValueAt(TNoRM.getText(), row, 1);
            tbObat.setValueAt(TPasien.getText(), row, 2);
            tbObat.setValueAt(TglLahir.getText(), row, 3);
            tbObat.setValueAt(Jk.getText(), row, 4);
            tbObat.setValueAt(TanggalMasuk.getTanggalDanJamSql(Valid), row, 5);
            tbObat.setValueAt(TanggalPindah.getTanggalDanJamSql(Valid), row, 6);
            tbObat.setValueAt(DiagnosaUtama.getText(), row, 7);
            tbObat.setValueAt(KdPetugasMenyerahkan.getText(), row, 8);
            tbObat.setValueAt(NmPetugasMenyerahkan.getText(), row, 9);
            tbObat.setValueAt(KdPetugasMenerima.getText(), row, 10);
            tbObat.setValueAt(NmPetugasMenerima.getText(), row, 11);
            JOptionPane.showMessageDialog(null, "Perubahan data transfer pasien berhasil disimpan!");
            emptTeks();
            TabRawat.setSelectedIndex(1);
        }
    }

    private void hapus() {
        if (tbObat.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Silakan pilih data transfer yang ingin dihapus terlebih dahulu!");
            return;
        }
        if (JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus data transfer ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (Sequel.queryu2tf("delete from transfer_pasien_antar_ruang where no_rawat=? and tanggal_masuk=?", 2, new String[]{
                tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString(), tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString()
            })) {
                tabMode.removeRow(tbObat.getSelectedRow());
                LCount.setText("" + tabMode.getRowCount());
                JOptionPane.showMessageDialog(null, "Data transfer pasien berhasil dihapus!");
                emptTeks();
                TabRawat.setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(null, "Gagal menghapus data transfer pasien!");
            }
        }
    }

    private void getData() {
        if (tbObat.getSelectedRow() == -1) return;
        String noRawatPilih = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        String tanggalMasukPilih = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();
        try {
            ps = koneksi.prepareStatement(
                "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                "transfer_pasien_antar_ruang.*,"+
                "petugasmenyerahkan.nama as nm_menyerahkan,petugasmenerima.nama as nm_menerima,"+
                "dokterasal1.nm_dokter as nm_dokter_asal_1,dokterasal2.nm_dokter as nm_dokter_asal_2,dokterasal3.nm_dokter as nm_dokter_asal_3,"+
                "doktertujuan1.nm_dokter as nm_dokter_tujuan_1,doktertujuan2.nm_dokter as nm_dokter_tujuan_2,doktertujuan3.nm_dokter as nm_dokter_tujuan_3 "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip "+
                "left join dokter as dokterasal1 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_1=dokterasal1.kd_dokter "+
                "left join dokter as dokterasal2 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_2=dokterasal2.kd_dokter "+
                "left join dokter as dokterasal3 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_3=dokterasal3.kd_dokter "+
                "left join dokter as doktertujuan1 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_1=doktertujuan1.kd_dokter "+
                "left join dokter as doktertujuan2 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_2=doktertujuan2.kd_dokter "+
                "left join dokter as doktertujuan3 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_3=doktertujuan3.kd_dokter "+
                "where transfer_pasien_antar_ruang.no_rawat=? and transfer_pasien_antar_ruang.tanggal_masuk=?");
            try {
                ps.setString(1, noRawatPilih);
                ps.setString(2, tanggalMasukPilih);
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRw.setText(rs.getString("no_rawat"));
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    Jk.setText(rs.getString("jk"));
                    TanggalMasuk.setNilaiSql(rs.getString("tanggal_masuk"));
                    TanggalPindah.setNilaiSql(rs.getString("tanggal_pindah"));
                    IndikasiPindah.setSelectedItem(nz(rs.getString("indikasi_pindah_ruang")));
                    KeteranganIndikasiPindahRuang.setText(nz(rs.getString("keterangan_indikasi_pindah_ruang")));
                    AsalRuang.setText(nz(rs.getString("asal_ruang")));
                    RuangSelanjutnya.setText(nz(rs.getString("ruang_selanjutnya")));
                    MetodePemindahan.setSelectedItem(nz(rs.getString("metode_pemindahan_pasien")));
                    syncRadioPill(bgCaraPindah, nz(rs.getString("metode_pemindahan_pasien")));
                    DiagnosaUtama.setText(nz(rs.getString("diagnosa_utama")));
                    DiagnosaSekunder.setText(nz(rs.getString("diagnosa_sekunder")));
                    ProsedurDilakukan.setText(nz(rs.getString("prosedur_yang_sudah_dilakukan")));
                    ObatYangDiberikan.setText(nz(rs.getString("obat_yang_telah_diberikan")));
                    MenyetujuiPemindahan.setSelectedItem(nz(rs.getString("pasien_keluarga_menyetujui")));
                    NamaMenyetujui.setText(nz(rs.getString("nama_menyetujui")));
                    HubunganMenyetujui.setSelectedItem(nz(rs.getString("hubungan_menyetujui")));
                    KdPetugasMenyerahkan.setText(rs.getString("nip_menyerahkan"));
                    NmPetugasMenyerahkan.setText(rs.getString("nm_menyerahkan"));
                    KdPetugasMenerima.setText(rs.getString("nip_menerima"));
                    NmPetugasMenerima.setText(rs.getString("nm_menerima"));

                    TNoTlpMenyetujui.setText(nz(rs.getString("no_tlp_menyetujui")));
                    SudahDijelaskanDiagnosa.setSelectedItem(nz(rs.getString("sudah_dijelaskan_diagnosa")));
                    syncRadioPill(bgDijelaskan, nz(rs.getString("sudah_dijelaskan_diagnosa")));

                    PilihRuangAsal.setSelectedIndex(0);
                    PilihRuangTujuan.setSelectedIndex(0);
                    KelasAsalRuang.setText(nz(rs.getString("kelas_asal_ruang")));
                    syncRadioPill(bgKelasAsal, nz(rs.getString("kelas_asal_ruang")));
                    KelasRuangSelanjutnya.setText(nz(rs.getString("kelas_ruang_selanjutnya")));
                    syncRadioPill(bgKelasTujuan, nz(rs.getString("kelas_ruang_selanjutnya")));

                    KdDokterMerawatAsal1.setText(nz(rs.getString("kd_dokter_merawat_asal_1")));
                    NmDokterMerawatAsal1.setText(nz(rs.getString("nm_dokter_asal_1")));
                    KdDokterMerawatAsal2.setText(nz(rs.getString("kd_dokter_merawat_asal_2")));
                    NmDokterMerawatAsal2.setText(nz(rs.getString("nm_dokter_asal_2")));
                    KdDokterMerawatAsal3.setText(nz(rs.getString("kd_dokter_merawat_asal_3")));
                    NmDokterMerawatAsal3.setText(nz(rs.getString("nm_dokter_asal_3")));
                    KdDokterMerawatTujuan1.setText(nz(rs.getString("kd_dokter_merawat_tujuan_1")));
                    NmDokterMerawatTujuan1.setText(nz(rs.getString("nm_dokter_tujuan_1")));
                    KdDokterMerawatTujuan2.setText(nz(rs.getString("kd_dokter_merawat_tujuan_2")));
                    NmDokterMerawatTujuan2.setText(nz(rs.getString("nm_dokter_tujuan_2")));
                    KdDokterMerawatTujuan3.setText(nz(rs.getString("kd_dokter_merawat_tujuan_3")));
                    NmDokterMerawatTujuan3.setText(nz(rs.getString("nm_dokter_tujuan_3")));

                    KesadaranTransfer.setSelectedItem(nz(rs.getString("kesadaran_transfer")));
                    syncRadioPill(bgKesadaran, nz(rs.getString("kesadaran_transfer")));
                    TDTransfer.setText(nz(rs.getString("td_transfer")));
                    HRTransfer.setText(nz(rs.getString("hr_transfer")));
                    RRTransfer.setText(nz(rs.getString("rr_transfer")));
                    SuhuTransfer.setText(nz(rs.getString("suhu_transfer")));
                    GCSTransfer.setText(nz(rs.getString("gcs_transfer")));
                    SpO2Transfer.setText(nz(rs.getString("spo2_transfer")));
                    TingkatNyeriTransfer.setSelectedItem(nz(rs.getString("tingkat_nyeri_transfer")));
                    syncRadioPill(bgNyeri, nz(rs.getString("tingkat_nyeri_transfer")));
                    updateNyeriDisplay(nz(rs.getString("tingkat_nyeri_transfer")));

                    if (rs.getString("observasi_terakhir_pukul") != null) {
                        ObservasiTerakhirPukul.setNilaiSql(rs.getString("observasi_terakhir_pukul"));
                    } else {
                        ObservasiTerakhirPukul.setSekarang();
                    }

                    OksigenStatus.setSelectedItem(nz(rs.getString("oksigen_status")));
                    syncRadioPill(bgOksigenStatus, nz(rs.getString("oksigen_status")));
                    setChk(CekOksigenNasalKanule, rs.getString("oksigen_nasal_kanule"));
                    setChk(CekOksigenNasalKateter, rs.getString("oksigen_nasal_kateter"));
                    setChk(CekOksigenFaceMask, rs.getString("oksigen_face_mask"));
                    setChk(CekOksigenSungkupRebreathing, rs.getString("oksigen_sungkup_rebreathing"));
                    setChk(CekOksigenSungkupNonRebreathing, rs.getString("oksigen_sungkup_non_rebreathing"));
                    setChk(CekOksigenTracheostomy, rs.getString("oksigen_tracheostomy"));
                    setChk(CekOksigenEtt, rs.getString("oksigen_ett"));

                    String lineInvasif = rs.getString("line_invasif");
                    bgLineInvasif.clearSelection();
                    if ("Tidak Ada Line Invasive".equals(lineInvasif)) RbLineTidakAda.setSelected(true);
                    else if ("CVP/PICC".equals(lineInvasif)) RbLineCvpPicc.setSelected(true);
                    else if ("IV Line".equals(lineInvasif)) RbLineIvLine.setSelected(true);
                    if (rs.getString("line_invasif_tanggal") != null) {
                        LineInvasifTanggal.setNilaiSql(rs.getString("line_invasif_tanggal"));
                    } else {
                        LineInvasifTanggal.setSekarang();
                    }

                    setChk(CekKewaspadaanStandar, rs.getString("kewaspadaan_standar"));
                    setChk(CekKewaspadaanKontak, rs.getString("kewaspadaan_kontak"));
                    setChk(CekKewaspadaanAirborne, rs.getString("kewaspadaan_airborne"));
                    setChk(CekKewaspadaanDroplet, rs.getString("kewaspadaan_droplet"));

                    setChk(CekNutrisiPuasa, rs.getString("nutrisi_puasa"));
                    setChk(CekNutrisiOral, rs.getString("nutrisi_oral"));
                    setChk(CekNutrisiNgt, rs.getString("nutrisi_ngt"));
                    setChk(CekNutrisiDietKhusus, rs.getString("nutrisi_diet_khusus"));
                    String ccVal = nz(rs.getString("nutrisi_batasan_cairan_cc"));
                    TNutrisiBatasanCairanCc.setText(ccVal);
                    CekNutrisiBatasanCairan.setSelected(!ccVal.trim().isEmpty() && !ccVal.trim().equals("-") && !ccVal.trim().equals("0"));
                    TNutrisiDietKhususJelaskan.setText(nz(rs.getString("nutrisi_diet_khusus_jelaskan")));

                    String eliminasiBab = rs.getString("eliminasi_bab");
                    bgEliminasiBab.clearSelection();
                    if ("Normal".equals(eliminasiBab)) RbEliminasiBabNormal.setSelected(true);
                    else if ("Diare/Konstipasi".equals(eliminasiBab)) RbEliminasiBabDiareKonstipasi.setSelected(true);
                    else if ("Ileustomy/Colostomy".equals(eliminasiBab)) RbEliminasiBabIleustomyColostomy.setSelected(true);
                    else if ("Inkontinensia Alvi".equals(eliminasiBab)) RbEliminasiBabInkontinensiaAlvi.setSelected(true);

                    String eliminasiBak = rs.getString("eliminasi_bak");
                    bgEliminasiBak.clearSelection();
                    if ("Normal".equals(eliminasiBak)) RbEliminasiBakNormal.setSelected(true);
                    else if ("Inkontinensia Urine".equals(eliminasiBak)) RbEliminasiBakInkontinensiaUrine.setSelected(true);
                    else if ("Kateter Urine".equals(eliminasiBak)) RbEliminasiBakKateterUrine.setSelected(true);
                    TEliminasiBakKateterNo.setText(nz(rs.getString("eliminasi_bak_kateter_no")));
                    if (rs.getString("eliminasi_bak_kateter_tanggal_pasang") != null) {
                        DTPEliminasiBakKateterTanggalPasang.setNilaiSql(rs.getString("eliminasi_bak_kateter_tanggal_pasang"));
                    } else {
                        DTPEliminasiBakKateterTanggalPasang.setSekarang();
                    }

                    String aktifitas = rs.getString("aktifitas");
                    bgAktifitas.clearSelection();
                    if ("Mandiri".equals(aktifitas)) RbAktifitasMandiri.setSelected(true);
                    else if ("Dibantu Sebagian".equals(aktifitas)) RbAktifitasDibantuSebagian.setSelected(true);
                    else if ("Dibantu Penuh".equals(aktifitas)) RbAktifitasDibantuPenuh.setSelected(true);

                    String mobilisasi = rs.getString("mobilisasi");
                    bgMobilisasi.clearSelection();
                    if ("Tirah Baring".equals(mobilisasi)) RbMobilisasiTirahBaring.setSelected(true);
                    else if ("Duduk".equals(mobilisasi)) RbMobilisasiDuduk.setSelected(true);
                    else if ("Jalan".equals(mobilisasi)) RbMobilisasiJalan.setSelected(true);

                    setChk(CekDrainaseWsd, rs.getString("drainase_wsd"));
                    setChk(CekDrainaseDrainLuka, rs.getString("drainase_drain_luka"));
                    setChk(CekDrainaseStoma, rs.getString("drainase_stoma"));
                    setChk(CekDrainaseLain, rs.getString("drainase_lain"));
                    TDrainaseLainKet.setText(nz(rs.getString("drainase_lain_ket")));

                    setChk(CekLukaTidakAda, rs.getString("luka_tidak_ada"));
                    setChk(CekLukaOperasi, rs.getString("luka_operasi_cek"));
                    TLukaOperasiUkuran.setText(nz(rs.getString("luka_operasi_ukuran")));
                    TLukaOperasiLokasi.setText(nz(rs.getString("luka_operasi_lokasi")));
                    TLukaOperasiKondisi.setText(nz(rs.getString("luka_operasi_kondisi")));
                    setChk(CekLukaDekubitus, rs.getString("luka_dekubitus_cek"));
                    TLukaDekubitusUkuran.setText(nz(rs.getString("luka_dekubitus_ukuran")));
                    TLukaDekubitusLokasi.setText(nz(rs.getString("luka_dekubitus_lokasi")));
                    TLukaDekubitusKondisi.setText(nz(rs.getString("luka_dekubitus_kondisi")));
                    setChk(CekLukaSkinLost, rs.getString("luka_skin_lost_cek"));
                    TLukaSkinLostUkuran.setText(nz(rs.getString("luka_skin_lost_ukuran")));
                    TLukaSkinLostLokasi.setText(nz(rs.getString("luka_skin_lost_lokasi")));
                    TLukaSkinLostKondisi.setText(nz(rs.getString("luka_skin_lost_kondisi")));
                    setChk(CekLukaGangren, rs.getString("luka_gangren_cek"));
                    TLukaGangrenUkuran.setText(nz(rs.getString("luka_gangren_ukuran")));
                    TLukaGangrenLokasi.setText(nz(rs.getString("luka_gangren_lokasi")));
                    TLukaGangrenKondisi.setText(nz(rs.getString("luka_gangren_kondisi")));
                    setChk(CekLukaLain, rs.getString("luka_lain_cek"));
                    TLukaLainKet.setText(nz(rs.getString("luka_lain_ket")));

                    HasilInvestigasiAbnormal.setText(nz(rs.getString("hasil_investigasi_abnormal")));

                    setChk(CekTindakanResikoJatuh, rs.getString("tindakan_resiko_jatuh"));
                    setChk(CekTindakanPemasanganRestrain, rs.getString("tindakan_pemasangan_restrain"));
                    setChk(CekTindakanPerawatanLuka, rs.getString("tindakan_perawatan_luka"));
                    setChk(CekTindakanHygiene, rs.getString("tindakan_hygiene"));
                    RiwayatAlergi.setSelectedItem(nz(rs.getString("riwayat_alergi")));
                    syncRadioPill(bgAlergi, nz(rs.getString("riwayat_alergi")));
                    TJenisAlergi.setText(nz(rs.getString("jenis_alergi")));
                    TReaksiAlergi.setText(nz(rs.getString("reaksi_alergi")));

                    setChk(CekAlatBantuTanpa, rs.getString("alat_bantu_tanpa"));
                    setChk(CekAlatBantuDengar, rs.getString("alat_bantu_dengar"));
                    setChk(CekAlatBantuKacaMata, rs.getString("alat_bantu_kaca_mata"));
                    setChk(CekAlatBantuGigiPalsu, rs.getString("alat_bantu_gigi_palsu"));
                    setChk(CekAlatBantuTongkatKruk, rs.getString("alat_bantu_tongkat_kruk"));
                    setChk(CekAlatBantuLain, rs.getString("alat_bantu_lain"));
                    TAlatBantuLainKet.setText(nz(rs.getString("alat_bantu_lain_ket")));

                    TindakanYangSudahDilakukan.setText(nz(rs.getString("tindakan_yang_sudah_dilakukan")));

                    setChk(CekPenunjangLaboratorium, rs.getString("penunjang_laboratorium_cek"));
                    TPenunjangLaboratoriumKet.setText(nz(rs.getString("penunjang_laboratorium_ket")));
                    setChk(CekPenunjangRadiologi, rs.getString("penunjang_radiologi_cek"));
                    TPenunjangRadiologiKet.setText(nz(rs.getString("penunjang_radiologi_ket")));
                    setChk(CekPenunjangCtScan, rs.getString("penunjang_ct_scan_cek"));
                    TPenunjangCtScanKet.setText(nz(rs.getString("penunjang_ct_scan_ket")));
                    setChk(CekPenunjangLain, rs.getString("penunjang_lain_cek"));
                    TPenunjangLainKet.setText(nz(rs.getString("penunjang_lain_ket")));

                    RencanaTindakLanjut.setText(nz(rs.getString("rencana_tindak_lanjut")));
                    MasalahUtamaKeperawatan.setText(nz(rs.getString("masalah_utama_keperawatan")));
                    DokumenYangDiserahterimakan.setText(nz(rs.getString("dokumen_yang_diserahterimakan")));

                    updatePatientCard();
                    updateAlurDisplay();
                    updateProgress();
                    bangunRingkasanHtml();
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif getData RM 04 : " + e);
        }
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if (TCari.getText().trim().equals("")) {
                ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                    "transfer_pasien_antar_ruang.tanggal_masuk,transfer_pasien_antar_ruang.tanggal_pindah,transfer_pasien_antar_ruang.diagnosa_utama,"+
                    "transfer_pasien_antar_ruang.nip_menyerahkan,petugasmenyerahkan.nama as petugasmenyerahkan,transfer_pasien_antar_ruang.nip_menerima,"+
                    "petugasmenerima.nama as petugasmenerima "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                    "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                    "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip where "+
                    "transfer_pasien_antar_ruang.tanggal_pindah between ? and ? order by transfer_pasien_antar_ruang.tanggal_pindah");
            } else {
                ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                    "transfer_pasien_antar_ruang.tanggal_masuk,transfer_pasien_antar_ruang.tanggal_pindah,transfer_pasien_antar_ruang.diagnosa_utama,"+
                    "transfer_pasien_antar_ruang.nip_menyerahkan,petugasmenyerahkan.nama as petugasmenyerahkan,transfer_pasien_antar_ruang.nip_menerima,"+
                    "petugasmenerima.nama as petugasmenerima "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                    "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                    "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip where "+
                    "transfer_pasien_antar_ruang.tanggal_pindah between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                    "transfer_pasien_antar_ruang.nip_menyerahkan like ? or petugasmenyerahkan.nama like ?) order by transfer_pasien_antar_ruang.tanggal_pindah");
            }

            try {
                if (TCari.getText().trim().equals("")) {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem()+"") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem()+"") + " 23:59:59");
                } else {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem()+"") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem()+"") + " 23:59:59");
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                    ps.setString(6, "%" + TCari.getText() + "%");
                    ps.setString(7, "%" + TCari.getText() + "%");
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                        rs.getDate("tgl_lahir"), rs.getString("jk"), rs.getString("tanggal_masuk"),
                        rs.getString("tanggal_pindah"), rs.getString("diagnosa_utama"),
                        rs.getString("nip_menyerahkan"), rs.getString("petugasmenyerahkan"),
                        rs.getString("nip_menerima"), rs.getString("petugasmenerima")
                    });
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tampil RM 04 : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                "select reg_periksa.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    updatePatientCard();
                    updateAlurDisplay();
                    updateProgress();
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif isRawat RM 04 : " + e);
        }
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
        runBackground(() -> tampil());
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnHapus.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnEdit.setEnabled(akses.gettransfer_pasien_antar_ruang());
        BtnPrint.setEnabled(akses.gettransfer_pasien_antar_ruang());
    }

    public void setTampil() {
        TabRawat.setSelectedIndex(1);
    }

    public void emptTeks() {
        TanggalMasuk.setSekarang();
        TanggalPindah.setSekarang();
        IndikasiPindah.setSelectedIndex(0);
        KeteranganIndikasiPindahRuang.setText("");
        AsalRuang.setText("");
        RuangSelanjutnya.setText("");
        MetodePemindahan.setSelectedIndex(0);
        bgCaraPindah.clearSelection();
        DiagnosaUtama.setText("");
        DiagnosaSekunder.setText("");
        KdPetugasMenyerahkan.setText(""); NmPetugasMenyerahkan.setText("");
        KdPetugasMenerima.setText(""); NmPetugasMenerima.setText("");
        ProsedurDilakukan.setText("");
        ObatYangDiberikan.setText("");
        MenyetujuiPemindahan.setSelectedIndex(0);
        NamaMenyetujui.setText("");
        HubunganMenyetujui.setSelectedIndex(0);

        TNoTlpMenyetujui.setText("");
        SudahDijelaskanDiagnosa.setSelectedIndex(0);
        bgDijelaskan.clearSelection();
        PilihRuangAsal.setSelectedIndex(0);
        KelasAsalRuang.setText("");
        bgKelasAsal.clearSelection();
        PilihRuangTujuan.setSelectedIndex(0);
        KelasRuangSelanjutnya.setText("");
        bgKelasTujuan.clearSelection();

        KdDokterMerawatAsal1.setText(""); NmDokterMerawatAsal1.setText("");
        KdDokterMerawatAsal2.setText(""); NmDokterMerawatAsal2.setText("");
        KdDokterMerawatAsal3.setText(""); NmDokterMerawatAsal3.setText("");
        KdDokterMerawatTujuan1.setText(""); NmDokterMerawatTujuan1.setText("");
        KdDokterMerawatTujuan2.setText(""); NmDokterMerawatTujuan2.setText("");
        KdDokterMerawatTujuan3.setText(""); NmDokterMerawatTujuan3.setText("");

        KesadaranTransfer.setSelectedIndex(0);
        bgKesadaran.clearSelection();
        TDTransfer.setText(""); HRTransfer.setText(""); RRTransfer.setText(""); SuhuTransfer.setText(""); GCSTransfer.setText(""); SpO2Transfer.setText("");
        TingkatNyeriTransfer.setSelectedIndex(0);
        bgNyeri.clearSelection();
        LblNyeriKet.setText("0 \u00B7 Tidak ada rasa nyeri");
        LblNyeriKet.setForeground(GREEN_FG);
        ObservasiTerakhirPukul.setSekarang();

        OksigenStatus.setSelectedIndex(0);
        bgOksigenStatus.clearSelection();
        CekOksigenNasalKanule.setSelected(false); CekOksigenNasalKateter.setSelected(false); CekOksigenFaceMask.setSelected(false);
        CekOksigenSungkupRebreathing.setSelected(false); CekOksigenSungkupNonRebreathing.setSelected(false);
        CekOksigenTracheostomy.setSelected(false); CekOksigenEtt.setSelected(false);

        bgLineInvasif.clearSelection();
        LineInvasifTanggal.setSekarang();
        CekKewaspadaanStandar.setSelected(false); CekKewaspadaanKontak.setSelected(false); CekKewaspadaanAirborne.setSelected(false); CekKewaspadaanDroplet.setSelected(false);

        CekNutrisiPuasa.setSelected(false); CekNutrisiOral.setSelected(false); CekNutrisiNgt.setSelected(false); 
        CekNutrisiBatasanCairan.setSelected(false); CekNutrisiDietKhusus.setSelected(false);
        TNutrisiBatasanCairanCc.setText(""); TNutrisiDietKhususJelaskan.setText("");

        bgEliminasiBab.clearSelection(); bgEliminasiBak.clearSelection();
        TEliminasiBakKateterNo.setText(""); DTPEliminasiBakKateterTanggalPasang.setSekarang();

        bgAktifitas.clearSelection(); bgMobilisasi.clearSelection();
        CekDrainaseWsd.setSelected(false); CekDrainaseDrainLuka.setSelected(false); CekDrainaseStoma.setSelected(false); CekDrainaseLain.setSelected(false);
        TDrainaseLainKet.setText("");

        CekLukaTidakAda.setSelected(false);
        CekLukaOperasi.setSelected(false); TLukaOperasiUkuran.setText(""); TLukaOperasiLokasi.setText(""); TLukaOperasiKondisi.setText("");
        CekLukaDekubitus.setSelected(false); TLukaDekubitusUkuran.setText(""); TLukaDekubitusLokasi.setText(""); TLukaDekubitusKondisi.setText("");
        CekLukaSkinLost.setSelected(false); TLukaSkinLostUkuran.setText(""); TLukaSkinLostLokasi.setText(""); TLukaSkinLostKondisi.setText("");
        CekLukaGangren.setSelected(false); TLukaGangrenUkuran.setText(""); TLukaGangrenLokasi.setText(""); TLukaGangrenKondisi.setText("");
        CekLukaLain.setSelected(false); TLukaLainKet.setText("");

        HasilInvestigasiAbnormal.setText("");

        CekTindakanResikoJatuh.setSelected(false); CekTindakanPemasanganRestrain.setSelected(false);
        CekTindakanPerawatanLuka.setSelected(false); CekTindakanHygiene.setSelected(false);
        RiwayatAlergi.setSelectedIndex(0); bgAlergi.clearSelection(); TJenisAlergi.setText(""); TReaksiAlergi.setText("");

        CekAlatBantuTanpa.setSelected(false); CekAlatBantuDengar.setSelected(false); CekAlatBantuKacaMata.setSelected(false);
        CekAlatBantuGigiPalsu.setSelected(false); CekAlatBantuTongkatKruk.setSelected(false); CekAlatBantuLain.setSelected(false);
        TAlatBantuLainKet.setText("");

        TindakanYangSudahDilakukan.setText("");

        CekPenunjangLaboratorium.setSelected(false); TPenunjangLaboratoriumKet.setText("");
        CekPenunjangRadiologi.setSelected(false); TPenunjangRadiologiKet.setText("");
        CekPenunjangCtScan.setSelected(false); TPenunjangCtScanKet.setText("");
        CekPenunjangLain.setSelected(false); TPenunjangLainKet.setText("");

        RencanaTindakLanjut.setText(""); MasalahUtamaKeperawatan.setText(""); DokumenYangDiserahterimakan.setText("");

        updatePatientCard();
        updateAlurDisplay();
        updateProgress();

        scrollToSection(0);
        TabRawat.setSelectedIndex(0);
        IndikasiPindah.requestFocus();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private void setChk(AbstractButton c, String val) {
        if (c != null) {
            c.setSelected("Ya".equalsIgnoreCase(val));
        }
    }

    private void muatPilihanKamar(widget.ComboBox combo) {
        javax.swing.DefaultComboBoxModel<KamarItem> model = new javax.swing.DefaultComboBoxModel<>();
        model.addElement(new KamarItem("- Pilih Ruangan -", "", ""));
        try {
            PreparedStatement psKamar = koneksi.prepareStatement(
                "select kamar.kd_kamar,kamar.kelas,bangsal.nm_bangsal from kamar "+
                "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "+
                "where kamar.statusdata='1' order by bangsal.nm_bangsal,kamar.kd_kamar");
            try {
                ResultSet rsKamar = psKamar.executeQuery();
                try {
                    while (rsKamar.next()) {
                        String label = rsKamar.getString("nm_bangsal") + " (" + rsKamar.getString("kd_kamar") + ")";
                        model.addElement(new KamarItem(label, rsKamar.getString("kd_kamar"), rsKamar.getString("kelas")));
                    }
                } finally { if (rsKamar != null) rsKamar.close(); }
            } finally { if (psKamar != null) psKamar.close(); }
        } catch (Exception e) {
            System.out.println("Notif muatPilihanKamar : " + e);
        }
        combo.setModel(model);
    }

    // =================================================================
    //  RINGKASAN HTML & CETAK LAPORAN
    // =================================================================

    private String judulBarisTransfer(String judul) {
        return "<tr><td colspan='2' bgcolor='#123c3a' style='color:#ffffff;font-weight:bold;'>&nbsp;" + judul + "</td></tr>";
    }

    private String barisTransfer(String label, String value) {
        return "<tr><td width='35%' valign='top' bgcolor='#f7f8f5'><b>" + label + "</b></td><td width='65%' valign='top'>" +
            ((value == null || value.trim().isEmpty()) ? "-" : value) + "</td></tr>";
    }

    private String cekListTransfer(AbstractButton c, String label) {
        return c != null && c.isSelected() ? "[" + label + "] " : "";
    }

    private void bangunRingkasanHtml() {
        StringBuilder h = new StringBuilder();
        h.append("<html><body style='font-family:Segoe UI,Tahoma;font-size:10px;color:#1c1f1d;'>");
        h.append("<table width='100%' cellpadding='4' cellspacing='1' style='border:1px solid #dcdfda;'>");

        h.append(judulBarisTransfer("IDENTITAS PASIEN"));
        h.append(barisTransfer("No. RM / No. Rawat", TNoRM.getText() + " / " + TNoRw.getText()));
        h.append(barisTransfer("Nama Pasien", TPasien.getText() + " (" + Jk.getText() + ")"));
        h.append(barisTransfer("Tgl. Lahir", TglLahir.getText()));

        h.append(judulBarisTransfer("ALUR TRANSFER & DOKTER"));
        h.append(barisTransfer("Ruang Asal", AsalRuang.getText() + " (" + KelasAsalRuang.getText() + ")"));
        h.append(barisTransfer("Dokter Merawat Asal", NmDokterMerawatAsal1.getText() +
            (!NmDokterMerawatAsal2.getText().isEmpty() ? "; " + NmDokterMerawatAsal2.getText() : "") +
            (!NmDokterMerawatAsal3.getText().isEmpty() ? "; " + NmDokterMerawatAsal3.getText() : "")));
        h.append(barisTransfer("Ruang Tujuan", RuangSelanjutnya.getText() + " (" + KelasRuangSelanjutnya.getText() + ")"));
        h.append(barisTransfer("Dokter Merawat Tujuan", NmDokterMerawatTujuan1.getText() +
            (!NmDokterMerawatTujuan2.getText().isEmpty() ? "; " + NmDokterMerawatTujuan2.getText() : "") +
            (!NmDokterMerawatTujuan3.getText().isEmpty() ? "; " + NmDokterMerawatTujuan3.getText() : "")));
        h.append(barisTransfer("Cara Pindah", MetodePemindahan.getSelectedItem() + ""));

        h.append(judulBarisTransfer("PETUGAS & PERSETUJUAN KELUARGA"));
        h.append(barisTransfer("Petugas Pengirim", KdPetugasMenyerahkan.getText() + " - " + NmPetugasMenyerahkan.getText()));
        h.append(barisTransfer("Petugas Penerima", KdPetugasMenerima.getText() + " - " + NmPetugasMenerima.getText()));
        h.append(barisTransfer("Keluarga Menyetujui", NamaMenyetujui.getText() + " (" + HubunganMenyetujui.getSelectedItem() + ") - Telp: " + TNoTlpMenyetujui.getText()));
        h.append(barisTransfer("Penjelasan Diagnosa & Risiko", SudahDijelaskanDiagnosa.getSelectedItem() + ""));

        h.append(judulBarisTransfer("KONDISI PASIEN & VITAL SIGNS"));
        h.append(barisTransfer("Observasi Terakhir", ObservasiTerakhirPukul.getSelectedItem() + ""));
        h.append(barisTransfer("Kesadaran / GCS", KesadaranTransfer.getSelectedItem() + " / " + GCSTransfer.getText()));
        h.append(barisTransfer("Tanda Vital", "TD: " + TDTransfer.getText() + " mmHg | HR: " + HRTransfer.getText() + " x/m | RR: " + RRTransfer.getText() + " x/m | Suhu: " + SuhuTransfer.getText() + " \u00B0C | SpO2: " + SpO2Transfer.getText() + "%"));
        h.append(barisTransfer("Tingkat Nyeri", (!nz(TingkatNyeriTransfer.getSelectedItem() + "").isEmpty() ? TingkatNyeriTransfer.getSelectedItem() + " / 10 (" + LblNyeriKet.getText() + ")" : "-")));
        h.append(barisTransfer("Dukungan Oksigen", "Ya".equals(OksigenStatus.getSelectedItem() + "") ?
            cekListTransfer(CekOksigenNasalKanule,"Nasal Kanule")+cekListTransfer(CekOksigenNasalKateter,"Nasal Kateter")+
            cekListTransfer(CekOksigenFaceMask,"Face Mask")+cekListTransfer(CekOksigenSungkupRebreathing,"Sungkup Rebreathing")+
            cekListTransfer(CekOksigenSungkupNonRebreathing,"Sungkup Non-Rebreathing")+cekListTransfer(CekOksigenTracheostomy,"Tracheostomy")+
            cekListTransfer(CekOksigenEtt,"ETT") : "Tidak"));

        h.append(judulBarisTransfer("DATA SERAH TERIMA"));
        String lineRingkas = RbLineTidakAda.isSelected() ? "Tidak Ada Line Invasive" : (RbLineCvpPicc.isSelected() ? "CVP/PICC" : (RbLineIvLine.isSelected() ? "IV Line" : "-"));
        h.append(barisTransfer("Line Invasif", lineRingkas));
        h.append(barisTransfer("Prosedur Pembedahan/Invasif", ProsedurDilakukan.getText()));
        h.append(barisTransfer("Kewaspadaan", cekListTransfer(CekKewaspadaanStandar,"Standar")+cekListTransfer(CekKewaspadaanKontak,"Kontak")+
            cekListTransfer(CekKewaspadaanAirborne,"Airborne")+cekListTransfer(CekKewaspadaanDroplet,"Droplet")));
        String nutrisiSummary = cekListTransfer(CekNutrisiPuasa,"Puasa")+cekListTransfer(CekNutrisiOral,"Oral")+cekListTransfer(CekNutrisiNgt,"NGT");
        if (CekNutrisiBatasanCairan.isSelected() || !TNutrisiBatasanCairanCc.getText().trim().isEmpty()) {
            nutrisiSummary += (nutrisiSummary.isEmpty() ? "" : ", ") + "Batasan Cairan " + TNutrisiBatasanCairanCc.getText().trim() + " Cc";
        }
        if (CekNutrisiDietKhusus.isSelected()) {
            String ketDiet = TNutrisiDietKhususJelaskan.getText().trim();
            nutrisiSummary += (nutrisiSummary.isEmpty() ? "" : ", ") + "Diet Khusus" + (ketDiet.isEmpty() ? "" : " (" + ketDiet + ")");
        }
        h.append(barisTransfer("Nutrisi", nutrisiSummary.isEmpty() ? "-" : nutrisiSummary));
        String babRingkas = RbEliminasiBabNormal.isSelected() ? "Normal" : (RbEliminasiBabDiareKonstipasi.isSelected() ? "Diare/Konstipasi" : (RbEliminasiBabIleustomyColostomy.isSelected() ? "Ileustomy/Colostomy" : "-"));
        h.append(barisTransfer("Eliminasi BAB", babRingkas));
        String bakRingkas = RbEliminasiBakNormal.isSelected() ? "Normal" : (RbEliminasiBakInkontinensiaUrine.isSelected() ? "Inkontinensia Urine" : (RbEliminasiBakKateterUrine.isSelected() ? "Kateter Urine No. " + TEliminasiBakKateterNo.getText() : "-"));
        h.append(barisTransfer("Eliminasi BAK", bakRingkas));

        h.append(judulBarisTransfer("AKTIFITAS, MOBILISASI, DRAINASE & LUKA"));
        String actRingkas = RbAktifitasMandiri.isSelected() ? "Mandiri" : (RbAktifitasDibantuSebagian.isSelected() ? "Dibantu Sebagian" : (RbAktifitasDibantuPenuh.isSelected() ? "Dibantu Penuh" : "-"));
        h.append(barisTransfer("Aktifitas", actRingkas));
        String mobRingkas = RbMobilisasiTirahBaring.isSelected() ? "Tirah Baring" : (RbMobilisasiDuduk.isSelected() ? "Duduk" : (RbMobilisasiJalan.isSelected() ? "Jalan" : "-"));
        h.append(barisTransfer("Mobilisasi", mobRingkas));
        h.append(barisTransfer("Drainase", cekListTransfer(CekDrainaseWsd,"WSD")+cekListTransfer(CekDrainaseDrainLuka,"Drain Luka")+
            cekListTransfer(CekDrainaseStoma,"Stoma")+cekListTransfer(CekDrainaseLain,"Lain-lain")));
        h.append(barisTransfer("Integritas Kulit / Luka", CekLukaTidakAda.isSelected() ? "Tidak Ada Luka" :
            cekListTransfer(CekLukaOperasi,"Operasi")+cekListTransfer(CekLukaDekubitus,"Dekubitus")+
            cekListTransfer(CekLukaSkinLost,"Skin Lost")+cekListTransfer(CekLukaGangren,"Gangren")+cekListTransfer(CekLukaLain,"Lain-lain")));

        h.append(judulBarisTransfer("TINDAKAN, PENUNJANG & RENCANA"));
        h.append(barisTransfer("Kebutuhan Khusus", cekListTransfer(CekTindakanResikoJatuh,"Resiko Jatuh")+cekListTransfer(CekTindakanPemasanganRestrain,"Restrain")+cekListTransfer(CekTindakanPerawatanLuka,"Luka")+cekListTransfer(CekTindakanHygiene,"Hygiene")));
        h.append(barisTransfer("Riwayat Alergi", "Ya".equals(RiwayatAlergi.getSelectedItem() + "") ? "Ya - " + TJenisAlergi.getText() + " / " + TReaksiAlergi.getText() : "Tidak"));
        h.append(barisTransfer("Alat Bantu", cekListTransfer(CekAlatBantuTanpa,"Tanpa")+cekListTransfer(CekAlatBantuDengar,"Dengar")+cekListTransfer(CekAlatBantuKacaMata,"Kaca Mata")+cekListTransfer(CekAlatBantuGigiPalsu,"Gigi Palsu")+cekListTransfer(CekAlatBantuTongkatKruk,"Tongkat")+cekListTransfer(CekAlatBantuLain,"Lain")));
        h.append(barisTransfer("Tindakan Sudah Dilakukan", TindakanYangSudahDilakukan.getText()));
        h.append(barisTransfer("Pemeriksaan Penunjang", cekListTransfer(CekPenunjangLaboratorium,"Laboratorium")+cekListTransfer(CekPenunjangRadiologi,"Radiologi")+cekListTransfer(CekPenunjangCtScan,"CT Scan")+cekListTransfer(CekPenunjangLain,"Lain")));
        h.append(barisTransfer("Therapy Yang Diberikan", ObatYangDiberikan.getText()));
        h.append(barisTransfer("Investigasi Abnormal", HasilInvestigasiAbnormal.getText()));
        h.append(barisTransfer("Rencana Tindak Lanjut", RencanaTindakLanjut.getText()));
        h.append(barisTransfer("Masalah Keperawatan", MasalahUtamaKeperawatan.getText()));
        h.append(barisTransfer("Dokumen Diserahterimakan", DokumenYangDiserahterimakan.getText()));

        h.append("</table></body></html>");
        LoadHTML.setText(h.toString());
        LoadHTML.setCaretPosition(0);
    }

    private void isPhoto() {
        if (ChkAccor != null && ChkAccor.isSelected()) {
            PanelAccor.setVisible(true);
            FormPhoto.setVisible(true);
            PanelAccor.setPreferredSize(new Dimension(520, getHeight()));
        } else {
            PanelAccor.setVisible(false);
        }
    }

    private String fotoImgTag(String tabelBukti, String label, String nama, String noRawat, String tanggalMasuk) {
        String namaTampil = (nama == null || nama.trim().isEmpty()) ? "-" : nama;
        String html = "<b>" + label + "</b><br><font face='tahoma' size='2'>" + namaTampil + "</font><br><font face='tahoma' size='2' color='#434343'>Kosong</font>";
        try {
            PreparedStatement psFoto = koneksi.prepareStatement(
                "select photo from " + tabelBukti + " where no_rawat=? and tanggal_masuk=?");
            try {
                psFoto.setString(1, noRawat);
                psFoto.setString(2, tanggalMasuk);
                ResultSet rsFoto = psFoto.executeQuery();
                try {
                    if (rsFoto.next() && rsFoto.getString("photo") != null && !rsFoto.getString("photo").equals("") && !rsFoto.getString("photo").equals("-")) {
                        html = "<b>" + label + "</b><br><font face='tahoma' size='2'>" + namaTampil + "</font><br><img src='http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/persetujuantransferruang/" + rsFoto.getString("photo") + "' alt='photo' width='220' height='220'/>";
                    }
                } finally { if (rsFoto != null) rsFoto.close(); }
            } finally { if (psFoto != null) psFoto.close(); }
        } catch (Exception e) {
            System.out.println("Notif fotoImgTag : " + e);
        }
        return html;
    }

    private void panggilPhoto() {
        if (tbObat.getSelectedRow() == -1) return;
        String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
        String tanggalMasuk = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();

        String namaPengirim = "", namaPenerima = "", namaDokter = "", namaPasien = "";
        try {
            PreparedStatement psNama = koneksi.prepareStatement(
                "select petugasmenyerahkan.nama as nm_menyerahkan, petugasmenerima.nama as nm_menerima, "+
                "coalesce(dokterasal1.nm_dokter,doktertujuan1.nm_dokter) as nm_dokter, transfer_pasien_antar_ruang.nama_menyetujui "+
                "from transfer_pasien_antar_ruang "+
                "left join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                "left join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip "+
                "left join dokter as dokterasal1 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_1=dokterasal1.kd_dokter "+
                "left join dokter as doktertujuan1 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_1=doktertujuan1.kd_dokter "+
                "where transfer_pasien_antar_ruang.no_rawat=? and transfer_pasien_antar_ruang.tanggal_masuk=?");
            try {
                psNama.setString(1, noRawat);
                psNama.setString(2, tanggalMasuk);
                ResultSet rsNama = psNama.executeQuery();
                try {
                    if (rsNama.next()) {
                        namaPengirim = rsNama.getString("nm_menyerahkan");
                        namaPenerima = rsNama.getString("nm_menerima");
                        namaDokter = rsNama.getString("nm_dokter");
                        namaPasien = rsNama.getString("nama_menyetujui");
                    }
                } finally { if (rsNama != null) rsNama.close(); }
            } finally { if (psNama != null) psNama.close(); }
        } catch (Exception e) {
            System.out.println("Notif panggilPhoto (nama) : " + e);
        }

        String htmlPengirim = fotoImgTag("bukti_transfer_petugas_pengirim", "Petugas Pengirim", namaPengirim, noRawat, tanggalMasuk);
        String htmlPenerima = fotoImgTag("bukti_transfer_petugas_penerima", "Petugas Penerima", namaPenerima, noRawat, tanggalMasuk);
        String htmlDokter   = fotoImgTag("bukti_transfer_dokter_merawat", "Dokter Yang Merawat", namaDokter, noRawat, tanggalMasuk);
        String htmlPasien   = fotoImgTag("bukti_persetujuan_transfer_pasien_antar_ruang", "Pasien/Keluarga", namaPasien, noRawat, tanggalMasuk);
        LoadHTML2.setText("<html><body><table width='100%'><tr>"+
            "<td align='center' valign='top'>" + htmlPengirim + "</td><td align='center' valign='top'>" + htmlPenerima + "</td></tr><tr>"+
            "<td align='center' valign='top'>" + htmlDokter + "</td><td align='center' valign='top'>" + htmlPasien + "</td></tr></table></body></html>");
    }

    private void ambilTtdAntrian() {
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        } else {
            if (tbObat.getSelectedRow() > -1) {
                String noRawatAmbil = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
                String tanggalMasukAmbil = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();
                Sequel.queryu("delete from antripersetujuantransferantarruang");
                Sequel.queryu("insert into antripersetujuantransferantarruang values('" + noRawatAmbil + "','" + tanggalMasukAmbil + "')");
                Sequel.queryu("delete from bukti_transfer_petugas_pengirim where no_rawat='" + noRawatAmbil + "' and tanggal_masuk='" + tanggalMasukAmbil + "'");
                Sequel.queryu("delete from bukti_transfer_petugas_penerima where no_rawat='" + noRawatAmbil + "' and tanggal_masuk='" + tanggalMasukAmbil + "'");
                Sequel.queryu("delete from bukti_transfer_dokter_merawat where no_rawat='" + noRawatAmbil + "' and tanggal_masuk='" + tanggalMasukAmbil + "'");
                Sequel.queryu("delete from bukti_persetujuan_transfer_pasien_antar_ruang where no_rawat='" + noRawatAmbil + "' and tanggal_masuk='" + tanggalMasukAmbil + "'");
                panggilPhoto();
                bukaWebappTransferRuang();
            } else {
                JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data transfer terlebih dahulu..!!");
            }
        }
    }

    private void bukaWebappTransferRuang() {
        try {
            String iyem = encryptIyem(koneksiDB.USERHYBRIDWEB(), koneksiDB.PASHYBRIDWEB());
            Valid.panggilUrl("persetujuantransferruang/login.php?iyem=" + java.net.URLEncoder.encode(iyem, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Notif bukaWebappTransferRuang : " + e);
        }
    }

    private String encryptIyem(String usere, String passwordte) throws Exception {
        String json = "{\"usere\":\"" + usere + "\",\"passwordte\":\"" + passwordte + "\"}";
        String secretKey = "Bar12345Bar12345";
        String secretIv = "sayangsamakhanza";
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        String keyHex = sha256Hex(sha256.digest(secretKey.getBytes(StandardCharsets.UTF_8)));
        String ivHex = sha256Hex(sha256.digest(secretIv.getBytes(StandardCharsets.UTF_8))).substring(0, 16);
        byte[] keyBytes = keyHex.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = ivHex.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(ivBytes));
        byte[] enc = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
        String once = Base64.getEncoder().encodeToString(enc);
        return Base64.getEncoder().encodeToString(once.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256Hex(byte[] digestBytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digestBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private java.io.InputStream resolveBukti(String relPath) {
        if (relPath == null || relPath.trim().isEmpty()) return null;
        String cleanedPath = relPath.replace("\\", "/");
        String[] localRoots = {
            "/Applications/XAMPP/xamppfiles/htdocs",
            "/opt/lampp/htdocs",
            "C:/xampp/htdocs",
            "D:/xampp/htdocs"
        };
        try {
            for (String root : localRoots) {
                File f = new File(root, koneksiDB.HYBRIDWEB() + "/persetujuantransferruang/" + cleanedPath);
                if (f.isFile()) {
                    return new FileInputStream(f);
                }
            }
            File fallback = new File(System.getProperty("user.dir") + File.separator + "webapps" + File.separator + "persetujuantransferruang" + File.separator + cleanedPath);
            return fallback.isFile() ? new FileInputStream(fallback) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void BtnPrint1ActionPerformed(ActionEvent evt) {
        if (tbObat.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, "Maaf, silakan pilih data terlebih dahulu!");
            return;
        }
        String modeCetak = (String) JOptionPane.showInputDialog(null, "Silakan pilih mode cetak:", "Cetak Lembar Transfer Pasien Antar Ruang",
            JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Lembar", "PDF"}, "Lembar");
        if (modeCetak == null) return;
        cetakLaporanTransfer(modeCetak);
    }

    private void BtnPrintActionPerformed(ActionEvent evt) {
        if (tbObat.getSelectedRow() != -1) {
            BtnPrint1ActionPerformed(evt);
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, tidak ada data transfer yang bisa dicetak!");
        } else {
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("namars", akses.getnamars());
                param.put("alamatrs", akses.getalamatrs());
                param.put("kotars", akses.getkabupatenrs());
                param.put("propinsirs", akses.getpropinsirs());
                param.put("kontakrs", akses.getkontakrs());
                param.put("emailrs", akses.getemailrs());
                param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

                String qryCetak =
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"+
                    "transfer_pasien_antar_ruang.*,"+
                    "petugasmenyerahkan.nama as nm_menyerahkan,petugasmenerima.nama as nm_menerima "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                    "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                    "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip where "+
                    "transfer_pasien_antar_ruang.tanggal_pindah between '" + Valid.SetTgl(DTPCari1.getSelectedItem()+"") + " 00:00:00' and '" +
                    Valid.SetTgl(DTPCari2.getSelectedItem()+"") + " 23:59:59' order by transfer_pasien_antar_ruang.tanggal_pindah";

                Valid.MyReportqry("rptTransferPasienAntarRuang.jasper", "report", "::[ Laporan Daftar Transfer Pasien Antar Ruang ]::", qryCetak, param);
            } catch (Exception e) {
                System.out.println("Notif Cetak Daftar : " + e);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    private void cetakLaporanTransfer(String mode) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String noRawatCetak = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
            String tanggalMasukCetak = tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString();

            String fotoPengirim = null, fotoPenerima = null, fotoDokter = null, fotoPasien = null;
            ps = koneksi.prepareStatement("select photo from bukti_transfer_petugas_pengirim where no_rawat=? and tanggal_masuk=?");
            ps.setString(1, noRawatCetak); ps.setString(2, tanggalMasukCetak);
            rs = ps.executeQuery();
            if (rs.next()) fotoPengirim = rs.getString("photo");
            rs.close(); ps.close();

            ps = koneksi.prepareStatement("select photo from bukti_transfer_petugas_penerima where no_rawat=? and tanggal_masuk=?");
            ps.setString(1, noRawatCetak); ps.setString(2, tanggalMasukCetak);
            rs = ps.executeQuery();
            if (rs.next()) fotoPenerima = rs.getString("photo");
            rs.close(); ps.close();

            ps = koneksi.prepareStatement("select photo from bukti_transfer_dokter_merawat where no_rawat=? and tanggal_masuk=?");
            ps.setString(1, noRawatCetak); ps.setString(2, tanggalMasukCetak);
            rs = ps.executeQuery();
            if (rs.next()) fotoDokter = rs.getString("photo");
            rs.close(); ps.close();

            ps = koneksi.prepareStatement("select photo from bukti_persetujuan_transfer_pasien_antar_ruang where no_rawat=? and tanggal_masuk=?");
            ps.setString(1, noRawatCetak); ps.setString(2, tanggalMasukCetak);
            rs = ps.executeQuery();
            if (rs.next()) fotoPasien = rs.getString("photo");
            rs.close(); ps.close();

            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("sig_pengirim", resolveBukti(fotoPengirim));
            param.put("sig_penerima", resolveBukti(fotoPenerima));
            param.put("sig_dokter", resolveBukti(fotoDokter));
            param.put("sig_pasien", resolveBukti(fotoPasien));

            String qryCetak =
                "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,"+
                "transfer_pasien_antar_ruang.*,"+
                "petugasmenyerahkan.nama as nm_menyerahkan,petugasmenerima.nama as nm_menerima,"+
                "dokterasal1.nm_dokter as nm_dokter_asal_1,dokterasal2.nm_dokter as nm_dokter_asal_2,dokterasal3.nm_dokter as nm_dokter_asal_3,"+
                "doktertujuan1.nm_dokter as nm_dokter_tujuan_1,doktertujuan2.nm_dokter as nm_dokter_tujuan_2,doktertujuan3.nm_dokter as nm_dokter_tujuan_3 "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join transfer_pasien_antar_ruang on reg_periksa.no_rawat=transfer_pasien_antar_ruang.no_rawat "+
                "inner join petugas as petugasmenyerahkan on transfer_pasien_antar_ruang.nip_menyerahkan=petugasmenyerahkan.nip "+
                "inner join petugas as petugasmenerima on transfer_pasien_antar_ruang.nip_menerima=petugasmenerima.nip "+
                "left join dokter as dokterasal1 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_1=dokterasal1.kd_dokter "+
                "left join dokter as dokterasal2 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_2=dokterasal2.kd_dokter "+
                "left join dokter as dokterasal3 on transfer_pasien_antar_ruang.kd_dokter_merawat_asal_3=dokterasal3.kd_dokter "+
                "left join dokter as doktertujuan1 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_1=doktertujuan1.kd_dokter "+
                "left join dokter as doktertujuan2 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_2=doktertujuan2.kd_dokter "+
                "left join dokter as doktertujuan3 on transfer_pasien_antar_ruang.kd_dokter_merawat_tujuan_3=doktertujuan3.kd_dokter "+
                "where transfer_pasien_antar_ruang.no_rawat='" + noRawatCetak + "' and transfer_pasien_antar_ruang.tanggal_masuk='" + tanggalMasukCetak + "'";

            if ("PDF".equals(mode)) {
                Valid.MyReportqrypdf("rptCetakTransferPasienAntarRuang.jasper", "report", "::[ Laporan Transfer Pasien Antar Ruang ]::", qryCetak, param);
            } else {
                Valid.MyReportqry("rptCetakTransferPasienAntarRuang.jasper", "report", "::[ Laporan Transfer Pasien Antar Ruang ]::", qryCetak, param);
            }
        } catch (Exception e) {
            System.out.println("Notif cetakLaporanTransfer : " + e);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }

    @Override
    public void dispose() {
        if (TanggalMasuk != null) TanggalMasuk.stopTimer();
        if (TanggalPindah != null) TanggalPindah.stopTimer();
        if (ObservasiTerakhirPukul != null) ObservasiTerakhirPukul.stopTimer();
        if (LineInvasifTanggal != null) LineInvasifTanggal.stopTimer();
        if (DTPEliminasiBakKateterTanggalPasang != null) DTPEliminasiBakKateterTanggalPasang.stopTimer();
        executor.shutdownNow();
        super.dispose();
    }
}
