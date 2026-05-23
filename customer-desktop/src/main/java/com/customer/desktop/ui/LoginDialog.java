package com.customer.desktop.ui;

import com.customer.desktop.service.ApiService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {

    private static final Color BG_DARK = new Color(26, 27, 46);
    private static final Color BG_HEADER = new Color(45, 27, 105);
    private static final Color ACCENT_PURPLE = new Color(124, 92, 191);
    private static final Color TEXT_PRIMARY = new Color(224, 224, 224);
    private static final Color DANGER_COLOR = new Color(255, 107, 138);

    private final ApiService apiService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private JButton loginButton;
    private JButton registerButton;
    private boolean authenticated = false;

    public LoginDialog(JFrame owner, ApiService apiService) {
        super(owner, "Login", true);
        this.apiService = apiService;
        initComponents();
    }

    private void initComponents() {
        setSize(420, 360);
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(BG_DARK);
        setContentPane(content);

        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setPaint(new GradientPaint(0, 0, BG_HEADER, getWidth(), 0, new Color(30, 58, 95)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(new EmptyBorder(0, 24, 0, 24));
        headerPanel.setOpaque(false);

        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.setOpaque(false);
        headerTextPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerTextPanel.add(titleLabel);

        headerTextPanel.add(Box.createVerticalStrut(4));

        JLabel subtitleLabel = new JLabel("Sign in to continue");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(255, 255, 255, 160));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerTextPanel.add(subtitleLabel);

        headerPanel.add(headerTextPanel, BorderLayout.WEST);
        content.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_DARK);
        formPanel.setBorder(new EmptyBorder(24, 32, 10, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter username");
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(createLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.putClientProperty("JTextField.placeholderText", "Enter password");
        passwordField.addActionListener(e -> onLogin());
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        messageLabel.setForeground(DANGER_COLOR);
        formPanel.add(messageLabel, gbc);

        content.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 16));
        buttonPanel.setBackground(BG_DARK);

        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 36));
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> onRegister());
        buttonPanel.add(registerButton);

        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 36));
        loginButton.setBackground(ACCENT_PURPLE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> onLogin());
        buttonPanel.add(loginButton);

        content.add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
        SwingUtilities.invokeLater(usernameField::requestFocus);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private void setButtonsEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        registerButton.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        String error = validateInput(username, password);
        if (error != null) {
            messageLabel.setForeground(DANGER_COLOR);
            messageLabel.setText(error);
            return;
        }

        setButtonsEnabled(false);
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setText("Signing in...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return apiService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    get();
                    authenticated = true;
                    dispose();
                } catch (Exception e) {
                    setButtonsEnabled(true);
                    messageLabel.setForeground(DANGER_COLOR);
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    messageLabel.setText(msg);
                }
            }
        };
        worker.execute();
    }

    private void onRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        String error = validateInput(username, password);
        if (error != null) {
            messageLabel.setForeground(DANGER_COLOR);
            messageLabel.setText(error);
            return;
        }

        setButtonsEnabled(false);
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setText("Registering...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiService.register(username, password);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    messageLabel.setForeground(new Color(107, 203, 119));
                    messageLabel.setText("Registered! Click Login to continue.");
                    setButtonsEnabled(true);
                } catch (Exception e) {
                    setButtonsEnabled(true);
                    messageLabel.setForeground(DANGER_COLOR);
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    messageLabel.setText(msg);
                }
            }
        };
        worker.execute();
    }

    private String validateInput(String username, String password) {
        if (username.isEmpty()) return "Username is required.";
        if (username.length() < 3) return "Username must be at least 3 characters.";
        if (password.isEmpty()) return "Password is required.";
        if (password.length() < 8) return "Password must be at least 8 characters.";
        return null;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
