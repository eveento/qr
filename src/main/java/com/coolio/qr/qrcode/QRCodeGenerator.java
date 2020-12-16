package com.coolio.qr.qrcode;

import com.coolio.qr.controller.QRCodeController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QRCodeGenerator {

    private static final String LOGO = "https://www.strunkmedia.com/wp-content/uploads/2018/05/bigstock-221516158.jpg";

    private static final String EXT = "png";

    public static byte[] getSquareQRCodeWithImage(String url, int size, String colors) throws WriterException, IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size, createErrorConfig());

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, loadMatrixConfig(colors));

        BufferedImage overly = getOverly(LOGO);
        BufferedImage resize = Scalr.resize(overly, 50);

        createLogo(os, qrImage, resize);

        Files.copy(new ByteArrayInputStream(os.toByteArray()), Paths.get(QRCodeController.QR_CODE_IMAGE_PATH), StandardCopyOption.REPLACE_EXISTING);

        return os.toByteArray();

    }

    private static void createLogo(ByteArrayOutputStream os, BufferedImage qrImage, BufferedImage resize) throws IOException {
        int deltaHeight = qrImage.getHeight() - resize.getHeight();
        int deltaWidth = qrImage.getWidth() - resize.getWidth();

        BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) combined.getGraphics();

        g.drawImage(qrImage, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        g.drawImage(resize, Math.round((float) (deltaWidth / 2)), Math.round((float) (deltaHeight / 2)), null);

        ImageIO.write(combined, EXT, os);
    }

    private static Map<EncodeHintType, ErrorCorrectionLevel> createErrorConfig() {
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        return hints;
    }


    private static BufferedImage getOverly(String LOGO) throws IOException {
        URL url = new URL(LOGO);
        return ImageIO.read(url);
    }


    /***
     *
     * @param colors includes 2 types of ARGB color separate by dot.
     *               first - squares color, second- background color
     * @return MatrixToImageConfig with set colors up
     */
    private static MatrixToImageConfig loadMatrixConfig(String colors) {
        if (colors == null || colors.isEmpty()) {
            return new MatrixToImageConfig();
        } else {
            List<String> collect = Stream.of(colors.split(","))
                    .map(String::trim)
                    .map(e -> "#" + e)
                    .collect(Collectors.toList());

            if (collect.size() == 2) {
                int squareColor = convertHtmlFormatColorToInt(collect.get(0));
                int backgroundColor = convertHtmlFormatColorToInt(collect.get(1));
                return new MatrixToImageConfig(squareColor, backgroundColor);

            } else {
                throw new IllegalStateException("Incorrect color format!");
            }
        }
    }

    private static int convertHtmlFormatColorToInt(String color) {
        Color decodeSquareHtmlFormat = Color.decode(color);
        return decodeSquareHtmlFormat.getRGB();
    }

}
