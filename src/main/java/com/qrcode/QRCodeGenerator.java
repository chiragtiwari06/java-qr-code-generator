package com.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    /**
     * Generates an advanced QR Code with custom colors and logo embedding.
     */
    public static void createAdvancedQRCode(
            String text,
            int width,
            int height,
            Color fgColor,
            Color bgColor,
            File logoFile,
            String outputPath
    ) throws Exception {

        // Configure QR Hints
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // High error correction (H) allows up to 30% data recovery, enabling logo overlays
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1); // Border margin size

        // Generate matrix
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        // Convert BitMatrix to BufferedImage
        BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? fgColor.getRGB() : bgColor.getRGB());
            }
        }

        // Overlay Logo if provided
        if (logoFile != null && logoFile.exists()) {
            BufferedImage logo = ImageIO.read(logoFile);

            Graphics2D g = qrImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Scale logo to 20% of QR code dimensions
            int logoWidth = width / 5;
            int logoHeight = height / 5;
            int logoX = (width - logoWidth) / 2;
            int logoY = (height - logoHeight) / 2;

            // Draw white background behind logo for clean contrast
            g.setColor(Color.WHITE);
            g.fillRoundRect(logoX - 5, logoY - 5, logoWidth + 10, logoHeight + 10, 10, 10);

            // Draw logo image
            g.drawImage(logo, logoX, logoY, logoWidth, logoHeight, null);
            g.dispose();
        }

        // Save image to file
        File outputFile = new File(outputPath);
        ImageIO.write(qrImage, "PNG", outputFile);
        System.out.println("QR Code successfully saved to: " + outputFile.getAbsolutePath());
    }
}
