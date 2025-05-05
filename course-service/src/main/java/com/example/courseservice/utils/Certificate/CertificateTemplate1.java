package com.example.courseservice.utils.Certificate;

// Import the required packages

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import com.example.courseservice.configuration.DotenvConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;


public class CertificateTemplate1 {
    public static String linkExample = "https://res.cloudinary.com/diyn1vkim/image/upload/v1746390048/Certificate/Template/Example/bfbnem6d2aoetmo1t7s0.png";

    private static final String linkCertificateTemplate = "https://res.cloudinary.com/diyn1vkim/image/upload/v1746382068/Certificate/Template/tmtiurw2fqutx8z7fybs.png";

    static  int leftMargin = 163;


    static int yDate = 390;


    static int yUserName = 455;


    static int yCourseName = 565;

//    static int xStatement = 165;
//    static int yStatement = 600;

//    static int xSign = 240;
//    static int ySign = 740;

//    static int xDirector = 245;
//    static int yDirector = 855;

    static BufferedImage convertToBufferedImage(Image img) {
            BufferedImage bufferedImage = new BufferedImage(
                    img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = bufferedImage.createGraphics();

            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();

            return bufferedImage;
        }

    public static byte[] createCertificate(String date, String userName, String courseName){
//        String statement = "an online course authorized by " + directorName;

        try {


            URL url = new URL(CertificateTemplate1.linkCertificateTemplate);

            BufferedImage template = ImageIO.read(url);

            Graphics2D g2d = template.createGraphics();

            int width = template.getWidth();
            int height = template.getHeight();

            // 3. Cải thiện chất lượng rendering
            configureGraphicsQuality(g2d);

            Font nameFont = new Font("Inter", Font.BOLD, 54);
            Font courseFont = new Font("Inter", Font.BOLD, 46);
            Font dateFont = new Font("Inter", Font.PLAIN, 30);

            // Điền tên người dùng
            drawText(g2d, userName, nameFont, new Color(79, 79, 79),
                    leftMargin, yUserName, width - 700);

            // Điền tên khóa học
            drawText(g2d, courseName, courseFont, new Color(79, 79, 79),
                    leftMargin, yCourseName, width - 200);

            // Điền ngày hoàn thành
            drawText(g2d, date, dateFont,
                    new Color(130, 130, 130), leftMargin, yDate, width - 200);

            // Giải phóng tài nguyên
            g2d.dispose();
            // Chuyển ảnh thành byte array để trả về
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(template, "png", baos);

            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void configureGraphicsQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private static void drawText(Graphics2D g2d, String text, Font font, Color color,
                                         int X, int Y, int maxWidth) {
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

        g2d.drawString(text, X, Y);
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
