package com.visitor.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService {
    private static final int CAPTCHA_LENGTH = 6;
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz123456789";
    private static final SecureRandom random = new SecureRandom();

    public String generateCaptchaText() {
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            captcha.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return captcha.toString();
    }

    public BufferedImage generateCaptchaImage(String captchaText) {
        int width = 250;
        int height = 60;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Background
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, width, height);

        // Noise lines
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 150; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.drawLine(x, y, x + 1, y + 1);
        }

        // Draw CAPTCHA text with distortion
        g.setFont(new Font("Arial", Font.BOLD, 32));
        for (int i = 0; i < captchaText.length(); i++) {
            g.setColor(new Color(
                random.nextInt(100) + 50,
                random.nextInt(100) + 50,
                random.nextInt(100) + 50
            ));
            int x = 25 + (i * 30);
            int y = 45 + random.nextInt(10) - 5;
            g.drawString(String.valueOf(captchaText.charAt(i)), x, y);
        }

        // Add more random lines
        g.setColor(new Color(100, 100, 100));
        for (int i = 0; i < 10; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();
        return image;
    }

    public String getCaptchaImageBase64(String captchaText) throws Exception {
        BufferedImage image = generateCaptchaImage(captchaText);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public boolean validateCaptcha(String userInput, jakarta.servlet.http.HttpSession session) {
        String storedCaptcha = (String) session.getAttribute("captcha_text");
        if (storedCaptcha == null) {
            return false;
        }
        
        boolean isValid = storedCaptcha.equalsIgnoreCase(userInput);
        session.removeAttribute("captcha_text");
        return isValid;
    }
}
