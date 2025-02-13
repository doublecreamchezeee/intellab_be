package com.example.courseservice.utils;

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
import java.util.Date;
import java.util.Map;


public class CertificateTemplate {
    public static String linkCertificateTemplate = "https://res.cloudinary.com/diyn1vkim/image/upload/v1739296526/Certificate/Template/vojj5hz6abebtzi3c4jp.png";

    static int xDate = 165;
    static int yDate = 390;

    static int xUserName = 165;
    static int yUserName = 450;

    static int xCourseName = 165;
    static int yCourseName = 555;

    static int xStatement = 165;
    static int yStatement = 600;

    static int xSign = 240;
    static int ySign = 740;

    static int xDirector = 245;
    static int yDirector = 855;

    static BufferedImage convertToBufferedImage(Image img) {
            BufferedImage bufferedImage = new BufferedImage(
                    img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = bufferedImage.createGraphics();

            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();

            return bufferedImage;
        }

    public static byte[] createCertificate(String date, String userName, String courseName,
                                    Image sign, String directorName) throws IOException {
        String statement = "an online course authorized by " + directorName;

        try {


            URL url = new URL(CertificateTemplate.linkCertificateTemplate);

            BufferedImage template = ImageIO.read(url);

            Graphics2D graphic1 = template.createGraphics();

            // Thiết lập font chữ
            // date, director name - normal
            graphic1.setFont(new Font("Inter", Font.BOLD, 28));
            graphic1.setColor(Color.GRAY);
            graphic1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // font của UserName - large
            Graphics2D graphic2 = template.createGraphics();
            graphic2.setFont(new Font("Inter", Font.PLAIN, 56));
            graphic2.setColor(Color.BLACK);
            graphic2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // CourseName - semi-large
            Graphics2D graphic3 = template.createGraphics();
            graphic3.setFont(new Font("Inter", Font.PLAIN, 48));
            graphic3.setColor(Color.BLACK);
            graphic3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // sign name
            Graphics2D graphic4 = template.createGraphics();
            graphic4.setFont(new Font("Inter", Font.PLAIN, 23));
            graphic4.setColor(Color.BLACK);
            graphic4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphic1.drawString(date, xDate, yDate);
            graphic2.drawString(userName, xUserName, yUserName);
            graphic3.drawString(courseName, xCourseName, yCourseName);
            graphic1.drawString(statement, xStatement, yStatement);
            if (sign != null) {
                BufferedImage signImage = convertToBufferedImage(sign);
                graphic1.drawImage(signImage, xSign, ySign, null);
            }
            graphic4.drawString(directorName, xDirector, yDirector);

            graphic1.dispose();
            graphic2.dispose();
            graphic3.dispose();
            graphic4.dispose();

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
