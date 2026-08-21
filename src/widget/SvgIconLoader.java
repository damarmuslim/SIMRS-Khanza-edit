package widget;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

/**
 * Render file .svg (resource classpath) jadi Icon Swing lewat Apache Batik
 * (sudah tersedia di classpath project, tidak menambah dependency baru -
 * dipakai krn FlatSVGIcon/flatlaf-extras tidak ada di project ini).
 */
public class SvgIconLoader {

    private static class RasterTranscoder extends ImageTranscoder {
        private BufferedImage image;

        @Override
        public BufferedImage createImage(int w, int h) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            return image;
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput out) {
        }

        BufferedImage getImage() {
            return image;
        }
    }

    /**
     * @param resourcePath path classpath ke file .svg, mis. "/picture/icons/toolbar/menu.svg"
     * @param size lebar/tinggi target dlm px (SVG sumber diasumsikan persegi)
     * @param strokeColorHex kalau tidak null, warna "stroke" di SVG akan
     *        diganti dulu (sekadar string-replace teks XML) sblm dirender -
     *        dipakai utk varian warna hover/pressed tanpa perlu file svg terpisah
     */
    public static Icon load(String resourcePath, int size, String strokeColorHex) {
        try {
            InputStream is = SvgIconLoader.class.getResourceAsStream(resourcePath);
            if (is == null) {
                return null;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            is.close();
            String svg = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            if (strokeColorHex != null) {
                svg = svg.replaceAll("stroke=\"#[0-9A-Fa-f]{6}\"", "stroke=\"" + strokeColorHex + "\"");
            }

            RasterTranscoder transcoder = new RasterTranscoder();
            transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) size);
            transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) size);
            TranscoderInput input = new TranscoderInput(new StringReader(svg));
            transcoder.transcode(input, null);

            return new ImageIcon(transcoder.getImage());
        } catch (Exception e) {
            System.out.println("Notif SvgIconLoader (" + resourcePath + "): " + e);
            return null;
        }
    }
}
