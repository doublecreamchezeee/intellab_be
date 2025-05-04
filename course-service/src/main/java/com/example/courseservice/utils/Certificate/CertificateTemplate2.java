package com.example.courseservice.utils.Certificate;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.courseservice.configuration.DotenvConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class CertificateTemplate2 {
    public static String linkExample = "https://res.cloudinary.com/diyn1vkim/image/upload/v1746391160/Certificate/Template/Example/j1khwcxhn0p02zz30ksn.png";
    private static final String linkCertificateTemplate = "https://res.cloudinary.com/diyn1vkim/image/upload/v1746388792/Certificate/Template/ev9ry37o4bk3uqgyfby4.png";

        /**
         * Tạo certificate từ template URL và điền thông tin
         *
         * @param studentName Tên học viên
         * @param courseName Tên khóa học
         * @param completionDate Ngày hoàn thành
         * @return Mảng byte của ảnh certificate đã tạo
         */
    public static byte[] createCertificate(String studentName, String courseName, String completionDate) {
        try {
            // 1. Tải ảnh template từ URL
            BufferedImage template = loadImageFromUrl(linkCertificateTemplate);

            // 2. Tạo đối tượng Graphics2D để vẽ lên ảnh
            Graphics2D g2d = template.createGraphics();

            // 3. Cải thiện chất lượng rendering
            configureGraphicsQuality(g2d);

            // 4. Lấy kích thước ảnh
            int width = template.getWidth();
            int height = template.getHeight();

            // 5. Định nghĩa các font và vị trí
            Font nameFont = new Font("Times New Roman", Font.BOLD, 64);
            Font courseFont = new Font("Times New Roman", Font.BOLD, 42);
            Font dateFont = new Font("Inter", Font.PLAIN, 30);

            // 6. Điền tên học viên (căn giữa)
            drawCenteredText(g2d, studentName, nameFont, new Color(79, 79, 79),
                    width / 2, height / 2 - 70, width - 780);

            // 7. Điền tên khóa học (căn giữa)
            drawCenteredText(g2d, courseName, courseFont, new Color(79, 79, 79),
                    width / 2, height / 2 + 80, width - 200);

            // 8. Điền ngày hoàn thành (căn giữa)
            drawCenteredText(g2d, "Completion Date: " + completionDate, dateFont,
                    new Color(130, 130, 130), width / 2, height / 2 + 180, width - 200);

            // 9. Giải phóng tài nguyên
            g2d.dispose();

            // 10. Chuyển đổi ảnh thành mảng byte
            return convertImageToBytes(template);

        } catch (IOException e) {
            throw new RuntimeException("Failed to create certificate: " + e.getMessage(), e);
        }



    }

    /**
     * Tải ảnh từ URL
     */
    private static BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        return ImageIO.read(url);
    }

    /**
     * Cấu hình chất lượng đồ họa
     */
    private static void configureGraphicsQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /**
     * Vẽ văn bản căn giữa với xử lý văn bản dài
     *
     * @param g2d Đối tượng Graphics2D
     * @param text Văn bản cần vẽ
     * @param font Font chữ
     * @param color Màu chữ
     * @param centerX Tọa độ X trung tâm
     * @param centerY Tọa độ Y trung tâm
     * @param maxWidth Chiều rộng tối đa cho phép
     */
    private static void drawCenteredText(Graphics2D g2d, String text, Font font, Color color,
                                         int centerX, int centerY, int maxWidth) {
        g2d.setFont(font);
        g2d.setColor(color);

        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);

        // Nếu văn bản quá dài, giảm kích thước font
        if (textWidth > maxWidth) {
            // Tính toán kích thước font mới
            float newSize = (float) font.getSize() * maxWidth / textWidth;
            font = font.deriveFont(newSize);
            g2d.setFont(font);
            metrics = g2d.getFontMetrics(font);
            textWidth = metrics.stringWidth(text);
        }

        // Tính toán vị trí X để căn giữa
        int x = centerX - (textWidth / 2);

        // Tính toán vị trí Y (căn giữa theo chiều dọc)
        // Lưu ý: drawString vẽ từ baseline, nên cần điều chỉnh
        int y = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;

        g2d.drawString(text, x, y);
    }

    /**
     * Chuyển đổi BufferedImage thành mảng byte
     */
    private static byte[] convertImageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }


    public static Map uploadCertificateImage(byte[] image, String filename) throws Exception {
        Cloudinary cloudinary = new Cloudinary(DotenvConfig.get("CLOUDINARY_URL"));

        Map params1 = ObjectUtils.asMap(
                "public_id",filename,
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true,
                "folder", "Certificate"
        );

        return cloudinary.uploader().upload(image, params1);
    }

}
