package fungsi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;

/**
 * Service Penghubung Resmi Asisten AI Boy untuk SIMRS Khanza (RSUD Demang Sepulau Raya).
 * Menghubungkan modul rekam medis, farmasi, dan operasional ke Google Gemini API secara asinkron.
 * Dilengkapi Smart Multi-Routing Auto-Fallback (mencoba model universal bila model tertentu dibatasi).
 */
public class AsistenAIBoy {

    private static AsistenAIBoy instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final ObjectMapper mapper = new ObjectMapper();

    private String namaAsisten = "Boy";
    private String apiKey = "";
    private String model = "gemini-flash-latest";
    private String status = "Aktif";
    private double temperature = 0.3;
    private String systemPrompt = "";
    private String knowledgeBase = "";
    private boolean configLoaded = false;

    public interface Callback {
        void onSukses(String jawaban);
        void onGagal(String pesanError);
    }

    private AsistenAIBoy() {
        muatKonfigurasi();
        muatBasisPengetahuan();
    }

    public static synchronized AsistenAIBoy getInstance() {
        if (instance == null) {
            instance = new AsistenAIBoy();
        }
        return instance;
    }

    /**
     * Membaca konfigurasi AI dari tabel database `set_ai`.
     */
    public synchronized void muatKonfigurasi() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = koneksiDB.condb();
            if (con != null) {
                ps = con.prepareStatement("SELECT nama_asisten, api_key, model, status, temperature, system_prompt FROM set_ai WHERE id=1");
                rs = ps.executeQuery();
                if (rs.next()) {
                    this.namaAsisten = rs.getString("nama_asisten") != null ? rs.getString("nama_asisten").trim() : "Boy";
                    this.apiKey = rs.getString("api_key") != null ? rs.getString("api_key").trim() : "";
                    this.model = rs.getString("model") != null ? rs.getString("model").trim() : "gemini-flash-latest";
                    this.status = rs.getString("status") != null ? rs.getString("status").trim() : "Aktif";
                    this.temperature = rs.getDouble("temperature");
                    if (this.temperature <= 0 || this.temperature > 1.0) this.temperature = 0.3;
                    this.systemPrompt = rs.getString("system_prompt") != null ? rs.getString("system_prompt").trim() : "";
                    this.configLoaded = true;
                }
            }
        } catch (Exception e) {
            System.err.println("[AsistenAIBoy] Gagal membaca konfigurasi set_ai: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }

        if (systemPrompt == null || systemPrompt.isEmpty()) {
            systemPrompt = "Kamu adalah Boy, Asisten AI Cerdas Resmi SIMRS Khanza di RSUD Demang Sepulau Raya, Lampung Tengah. "
                    + "Kamu bertugas membantu dokter, perawat, apoteker, dan staf manajemen rumah sakit dalam hal "
                    + "pemahaman klinis medis, penulisan SOAP/CPPT rekam medis yang baku, cek interaksi obat dan dosis farmakologi, "
                    + "kode diagnosis ICD-10 & ICD-9-CM, serta navigasi operasional modul SIMRS Khanza. "
                    + "Jawablah dengan sopan, cerdas, akurat, profesional, dan to the point dalam Bahasa Indonesia.";
        }
    }

    /**
     * Memuat berkas basis pengetahuan lokal SIMRS.
     */
    public synchronized void muatBasisPengetahuan() {
        try {
            File f = new File("setting/ai_knowledge_simrs.md");
            if (f.exists() && f.isFile()) {
                byte[] bytes = Files.readAllBytes(f.toPath());
                this.knowledgeBase = new String(bytes, StandardCharsets.UTF_8);
            } else {
                this.knowledgeBase = "RSUD Demang Sepulau Raya, Lampung Tengah. Menggunakan SIMRS Khanza E-RM.";
            }
        } catch (Exception e) {
            this.knowledgeBase = "RSUD Demang Sepulau Raya, Lampung Tengah. Menggunakan SIMRS Khanza E-RM.";
        }
    }

    /**
     * Simpan konfigurasi ke tabel MySQL `set_ai`.
     */
    public boolean simpanKonfigurasi(String nama, String key, String mdl, String stts, double temp, String prompt) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = koneksiDB.condb();
            if (con != null) {
                ps = con.prepareStatement(
                    "INSERT INTO set_ai (id, nama_asisten, api_key, model, status, temperature, system_prompt) "
                    + "VALUES (1, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE nama_asisten=?, api_key=?, model=?, status=?, temperature=?, system_prompt=?"
                );
                ps.setString(1, nama);
                ps.setString(2, key);
                ps.setString(3, mdl);
                ps.setString(4, stts);
                ps.setDouble(5, temp);
                ps.setString(6, prompt);

                ps.setString(7, nama);
                ps.setString(8, key);
                ps.setString(9, mdl);
                ps.setString(10, stts);
                ps.setDouble(11, temp);
                ps.setString(12, prompt);

                ps.executeUpdate();

                this.namaAsisten = nama;
                this.apiKey = key;
                this.model = mdl;
                this.status = stts;
                this.temperature = temp;
                this.systemPrompt = prompt;
                this.configLoaded = true;
                return true;
            }
        } catch (Exception e) {
            System.err.println("[AsistenAIBoy] Gagal menyimpan konfigurasi set_ai: " + e.getMessage());
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Mengambil daftar model yang didukung oleh API Key dari Google AI Studio.
     */
    public List<String> ambilDaftarModelTersedia(String key) {
        Set<String> modelSet = new LinkedHashSet<>();
        if (key == null || key.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] endpoints = new String[]{
            "https://generativelanguage.googleapis.com/v1beta/models?key=" + key.trim(),
            "https://generativelanguage.googleapis.com/v1/models?key=" + key.trim()
        };

        for (String urlStr : endpoints) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(12000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStream is = conn.getInputStream()) {
                        JsonNode root = mapper.readTree(is);
                        JsonNode modelsNode = root.path("models");
                        if (modelsNode.isArray()) {
                            for (JsonNode m : modelsNode) {
                                JsonNode methods = m.path("supportedGenerationMethods");
                                boolean canGenerate = false;
                                if (methods.isArray()) {
                                    for (JsonNode met : methods) {
                                        if ("generateContent".equalsIgnoreCase(met.asText())) {
                                            canGenerate = true;
                                            break;
                                        }
                                    }
                                }
                                if (canGenerate) {
                                    String mName = m.path("name").asText();
                                    if (mName != null && mName.startsWith("models/")) {
                                        mName = mName.substring(7);
                                    }
                                    if (mName != null && !mName.trim().isEmpty()) {
                                        modelSet.add(mName.trim());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return new ArrayList<>(modelSet);
    }

    /**
     * Mengirim pesan ke Google Gemini API secara asinkron di background thread.
     */
    public void kirimPesanAsync(final String pesanUser, final String konteksLayar, final Callback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isAktif()) {
                        postGagal(callback, "Asisten AI (" + getNamaAsisten() + ") sedang dinonaktifkan di pengaturan sistem.");
                        return;
                    }

                    if (apiKey == null || apiKey.trim().isEmpty()) {
                        postGagal(callback, "API Key Google Gemini belum diatur. Silakan masukkan API Key di menu Pengaturan Asisten AI.");
                        return;
                    }

                    String hasil = eksekusiPanggilanGemini(apiKey, model, temperature, systemPrompt, knowledgeBase, pesanUser, konteksLayar);
                    postSukses(callback, hasil);
                } catch (Exception ex) {
                    postGagal(callback, "Gagal berkomunikasi dengan Asisten AI: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * Menguji koneksi API Key secara langsung dengan auto-detect model.
     */
    public String tesKoneksi(String testApiKey, String testModel) throws Exception {
        if (testApiKey == null || testApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key tidak boleh kosong.");
        }
        String mdl = (testModel != null && !testModel.trim().isEmpty()) ? testModel.trim() : "gemini-flash-latest";
        String jawaban = eksekusiPanggilanGemini(testApiKey.trim(), mdl, 0.2, "Jawab singkat saja.", "", "Tes koneksi. Jawab persis: 'Koneksi Berhasil, Boy Siap Melayani!'", null);
        return jawaban + " (Aktif via model: " + this.model + ")";
    }

    /**
     * Memanggil Google Gemini REST API dengan Multi-Routing Auto-Fallback.
     * Jika model yang dipilih 404/dibatasi, otomatis beralih ke model flash aktif.
     */
    private String eksekusiPanggilanGemini(String key, String mdl, double temp, String sysPrompt, String knowledge, String userMsg, String context) throws Exception {
        String cleanModel = mdl != null ? mdl.trim() : "gemini-flash-latest";
        if (cleanModel.startsWith("models/")) {
            cleanModel = cleanModel.substring(7);
        }

        // Susun daftar model kandidat beserta URL-nya
        List<String[]> candidatePairs = new ArrayList<>();

        // 1. Model yang diminta (v1beta dan v1)
        candidatePairs.add(new String[]{cleanModel, "https://generativelanguage.googleapis.com/v1beta/models/" + cleanModel + ":generateContent?key=" + key});
        candidatePairs.add(new String[]{cleanModel, "https://generativelanguage.googleapis.com/v1/models/" + cleanModel + ":generateContent?key=" + key});

        // 2. Daftar model universal yang aktif dan didukung untuk generasi teks
        String[] fallbackModels = new String[]{
            "gemini-flash-latest",
            "gemini-3.5-flash",
            "gemini-3.6-flash",
            "gemini-3.7-flash",
            "gemini-pro-latest",
            "gemini-1.5-flash-latest",
            "gemini-2.0-flash",
            "gemini-2.0-flash-exp"
        };

        for (String fb : fallbackModels) {
            if (!fb.equalsIgnoreCase(cleanModel)) {
                candidatePairs.add(new String[]{fb, "https://generativelanguage.googleapis.com/v1beta/models/" + fb + ":generateContent?key=" + key});
                candidatePairs.add(new String[]{fb, "https://generativelanguage.googleapis.com/v1/models/" + fb + ":generateContent?key=" + key});
            }
        }

        // Gabungkan System Prompt dan Basis Pengetahuan SIMRS
        StringBuilder fullSys = new StringBuilder();
        if (sysPrompt != null && !sysPrompt.isEmpty()) {
            fullSys.append(sysPrompt).append("\n\n");
        }
        if (knowledge != null && !knowledge.isEmpty()) {
            fullSys.append("=== BASIS PENGETAHUAN SIMRS KHANZA RSUD DEMANG SEPULAU RAYA ===\n")
                   .append(knowledge).append("\n\n");
        }

        // Susun pesan pengguna beserta konteks layar bila ada
        StringBuilder fullUserMsg = new StringBuilder();
        if (context != null && !context.trim().isEmpty()) {
            fullUserMsg.append("=== KONTEKS FORM / PASIEN AKTIF DI SIMRS ===\n")
                       .append(context.trim()).append("\n\n");
        }
        fullUserMsg.append(userMsg);

        // Bangun payload JSON standar Gemini
        ObjectNode root = mapper.createObjectNode();
        if (fullSys.length() > 0) {
            ObjectNode sysInst = root.putObject("system_instruction");
            ArrayNode sysParts = sysInst.putArray("parts");
            ObjectNode partObj = sysParts.addObject();
            partObj.put("text", fullSys.toString());
        }

        ArrayNode contents = root.putArray("contents");
        ObjectNode userContent = contents.addObject();
        userContent.put("role", "user");
        ArrayNode parts = userContent.putArray("parts");
        ObjectNode userPart = parts.addObject();
        userPart.put("text", fullUserMsg.toString());

        ObjectNode genConfig = root.putObject("generationConfig");
        genConfig.put("temperature", temp);
        genConfig.put("maxOutputTokens", 2048);

        byte[] payloadBytes = mapper.writeValueAsString(root).getBytes(StandardCharsets.UTF_8);

        String lastError = "";
        int lastCode = 0;

        for (String[] pair : candidatePairs) {
            String candModel = pair[0];
            String endpointUrl = pair[1];

            try {
                URL url = new URL(endpointUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payloadBytes, 0, payloadBytes.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream is = conn.getInputStream();
                         BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }

                        JsonNode respNode = mapper.readTree(response.toString());
                        JsonNode candidates = respNode.path("candidates");
                        if (candidates.isArray() && candidates.size() > 0) {
                            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                            if (!textNode.isMissingNode()) {
                                // Update model aktif yang berhasil tersambung
                                this.model = candModel;
                                return textNode.asText().trim();
                            }
                        }
                        return "Maaf, Boy tidak menerima teks jawaban yang valid dari Google Gemini.";
                    }
                } else {
                    lastCode = responseCode;
                    StringBuilder errResp = new StringBuilder();
                    try (InputStream es = conn.getErrorStream()) {
                        if (es != null) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(es, StandardCharsets.UTF_8));
                            String line;
                            while ((line = br.readLine()) != null) {
                                errResp.append(line);
                            }
                        }
                    } catch (Exception ignored) {}
                    lastError = errResp.toString();

                    // Jika 404 (model tidak didukung/dibatasi), otomatis lanjut mencoba kandidat model berikutnya
                    if (responseCode == 404) {
                        continue;
                    } else if (responseCode == 403 || responseCode == 400 || responseCode == 429) {
                        // Error otentikasi kunci atau kuota limit
                        break;
                    }
                }
            } catch (Exception ex) {
                lastError = ex.getMessage();
            }
        }

        if (lastCode == 404) {
            throw new Exception("Seluruh model yang dicoba mengembalikan 404. Detail: " + lastError);
        } else if (lastCode == 400) {
            throw new Exception("HTTP 400 Bad Request. Periksa format API Key Google Gemini.");
        } else if (lastCode == 403) {
            throw new Exception("HTTP 403 Forbidden. API Key tidak valid atau dinonaktifkan di Google AI Studio.");
        } else if (lastCode == 429) {
            throw new Exception("HTTP 429 Terlalu Banyak Permintaan. Batas kuota gratis per menit tercapai. Silakan coba 1 menit lagi.");
        } else {
            throw new Exception("HTTP " + lastCode + " - " + (lastError.isEmpty() ? "Gagal menghubungi server Gemini" : lastError));
        }
    }

    private void postSukses(final Callback cb, final String pesan) {
        if (cb == null) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                cb.onSukses(pesan);
            }
        });
    }

    private void postGagal(final Callback cb, final String err) {
        if (cb == null) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                cb.onGagal(err);
            }
        });
    }

    public boolean isAktif() {
        return "Aktif".equalsIgnoreCase(this.status);
    }

    public String getNamaAsisten() {
        return namaAsisten;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getStatus() {
        return status;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }
}
