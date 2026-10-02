package com.qrcode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MainGUI extends JFrame {

    // Form Inputs
    private JTextField textInput, wifiSsidInput, wifiPassInput;
    private JComboBox<String> wifiEncType;
    private JTextField vcardName, vcardPhone, vcardEmail, vcardCompany;

    // Color & File Controls
    private Color qrColor = Color.BLACK;
    private Color bgColor = Color.WHITE;
    private File selectedLogo = null;

    // Display & Export
    private JLabel previewLabel;
    private BufferedImage currentQRImage;
    private JTabbedPane tabbedPane;

    public MainGUI() {
        setTitle("Advanced QR Code Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(650, 450)); // Set a reasonable minimum bound

        // Root Panel using GridBagLayout for proportional splits
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // --- LEFT PANEL: DATA INPUT TABS & CONTROLS ---
        tabbedPane = new JTabbedPane();

        // Tab 1: Plain Text / URL
        JPanel textPanel = createFormPanel();
        addFormField(textPanel, "URL or Plain Text:", textInput = new JTextField());
        tabbedPane.addTab("Text / URL", textPanel);

        // Tab 2: Wi-Fi Generator
        JPanel wifiPanel = createFormPanel();
        addFormField(wifiPanel, "Network Name (SSID):", wifiSsidInput = new JTextField());
        addFormField(wifiPanel, "Password:", wifiPassInput = new JTextField());
        addFormField(wifiPanel, "Security Type:", wifiEncType = new JComboBox<>(new String[]{"WPA", "WEP", "nopass"}));
        tabbedPane.addTab("Wi-Fi", wifiPanel);

        // Tab 3: vCard Contact Card
        JPanel vcardPanel = createFormPanel();
        addFormField(vcardPanel, "Full Name:", vcardName = new JTextField());
        addFormField(vcardPanel, "Phone Number:", vcardPhone = new JTextField());
        addFormField(vcardPanel, "Email Address:", vcardEmail = new JTextField());
        addFormField(vcardPanel, "Company:", vcardCompany = new JTextField());
        tabbedPane.addTab("Contact vCard", vcardPanel);

        // Customization Action Buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));

        JButton fgColorBtn = new JButton("Foreground Color");
        fgColorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Select QR Color", qrColor);
            if (chosen != null) qrColor = chosen;
        });

        JButton bgColorBtn = new JButton("Background Color");
        bgColorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Select Background Color", bgColor);
            if (chosen != null) bgColor = chosen;
        });

        JButton logoBtn = new JButton("Attach Logo");
        logoBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedLogo = chooser.getSelectedFile();
                logoBtn.setText("Logo: " + selectedLogo.getName());
            }
        });

        JButton generateBtn = new JButton("Generate QR");
        generateBtn.setBackground(new Color(0, 122, 255));
        generateBtn.setForeground(Color.BLACK);
        generateBtn.addActionListener(e -> generateQR());

        controlPanel.add(fgColorBtn);
        controlPanel.add(bgColorBtn);
        controlPanel.add(logoBtn);
        controlPanel.add(generateBtn);

        // Assemble Left Side Stack
        JPanel leftContainer = new JPanel(new BorderLayout(5, 5));
        leftContainer.add(tabbedPane, BorderLayout.CENTER);
        leftContainer.add(controlPanel, BorderLayout.SOUTH);

        // Add Left Container to GridBag (50% Horizontal Weight)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        mainPanel.add(leftContainer, gbc);

        // --- RIGHT PANEL: LIVE RESPONSIVE PREVIEW & SAVE ---
        JPanel rightContainer = new JPanel(new BorderLayout(5, 5));
        previewLabel = new JLabel("Your QR Code Preview", SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createEtchedBorder());

        JButton saveBtn = new JButton("Export / Save PNG");
        saveBtn.addActionListener(e -> saveImage());

        rightContainer.add(previewLabel, BorderLayout.CENTER);
        rightContainer.add(saveBtn, BorderLayout.SOUTH);

        // Add Right Container to GridBag (50% Horizontal Weight)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        mainPanel.add(rightContainer, gbc);

        // --- RESPONSIVE IMAGE RESIZING EVENT ---
        // Dynamically scales the QR image preview when the user resizes the window
        previewLabel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateScaledPreview();
            }
        });

        add(mainPanel);
        pack(); // Automatically sizes frame based on component preferred sizes
        setLocationRelativeTo(null); // Center frame on screen
    }

    // Helper: Creates form container with GridBagLayout
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    // Helper: Adds label and input control pair responsively
    private void addFormField(JPanel panel, String labelText, Component inputComponent) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label Column
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        panel.add(new JLabel(labelText), gbc);

        // Input Field Column (Expands horizontally)
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(inputComponent, gbc);
    }

    private void generateQR() {
        String payload = "";
        int activeTab = tabbedPane.getSelectedIndex();

        if (activeTab == 0) {
            payload = textInput.getText();
        } else if (activeTab == 1) {
            payload = DataFormatters.formatWifi(
                    wifiSsidInput.getText(),
                    wifiPassInput.getText(),
                    (String) wifiEncType.getSelectedItem()
            );
        } else if (activeTab == 2) {
            payload = DataFormatters.formatVCard(
                    vcardName.getText(),
                    vcardPhone.getText(),
                    vcardEmail.getText(),
                    vcardCompany.getText()
            );
        }

        if (payload == null || payload.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in the required fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String tempPath = "temp_preview.png";
            // Generate full 800x800 resolution image file for high quality export
            QRCodeGenerator.createAdvancedQRCode(
                    payload, 800, 800, qrColor, bgColor, selectedLogo, tempPath
            );

            File imgFile = new File(tempPath);
            currentQRImage = ImageIO.read(imgFile);
            updateScaledPreview();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating QR: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Smoothly rescales preview image to fit available label space
    private void updateScaledPreview() {
        if (currentQRImage == null) return;

        int labelWidth = previewLabel.getWidth();
        int labelHeight = previewLabel.getHeight();

        if (labelWidth <= 0 || labelHeight <= 0) return;

        // Maintain square 1:1 aspect ratio based on smallest available space dimension
        int size = Math.min(labelWidth - 20, labelHeight - 20);
        if (size > 0) {
            Image scaled = currentQRImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            previewLabel.setIcon(new ImageIcon(scaled));
            previewLabel.setText("");
        }
    }

    private void saveImage() {
        if (currentQRImage == null) {
            JOptionPane.showMessageDialog(this, "Please generate a QR code first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("my_qrcode.png"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File targetFile = fileChooser.getSelectedFile();
                ImageIO.write(currentQRImage, "PNG", targetFile);
                JOptionPane.showMessageDialog(this, "QR Code saved successfully to:\n" + targetFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        // Enable system native Look and Feel for responsive high-DPI scaling
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}

 