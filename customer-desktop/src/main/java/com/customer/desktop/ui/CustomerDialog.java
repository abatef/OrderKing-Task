package com.customer.desktop.ui;

import com.customer.desktop.model.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerDialog extends JDialog {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JLabel validationLabel;
    private Customer result;

    public CustomerDialog(JFrame owner, Customer existing) {
        super(owner, existing != null ? "Edit Customer" : "New Customer", true);
        this.result = null;
        initComponents(existing);
    }

    private void initComponents(Customer existing) {
        boolean isEdit = existing != null;

        setSize(440, 320);
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        setContentPane(content);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(45, 27, 105));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel headerLabel = new JLabel(isEdit ? "Update customer details" : "Enter new customer details");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        content.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(createLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.putClientProperty("JTextField.placeholderText", "Full Name");
        if (isEdit) nameField.setText(existing.getName());
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        emailField = new JTextField(20);
        emailField.putClientProperty("JTextField.placeholderText", "email@example.com");
        if (isEdit) emailField.setText(existing.getEmail());
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(createLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        phoneField = new JTextField(20);
        phoneField.putClientProperty("JTextField.placeholderText", "+1234567890");
        if (isEdit) phoneField.setText(existing.getPhone());
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        validationLabel = new JLabel(" ");
        validationLabel.setForeground(new Color(255, 107, 138));
        validationLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        formPanel.add(validationLabel, gbc);

        content.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(90, 34));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton(isEdit ? "Save" : "Create");
        saveButton.setPreferredSize(new Dimension(90, 34));
        saveButton.setBackground(new Color(124, 92, 191));
        saveButton.setForeground(Color.WHITE);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> onSave());
        buttonPanel.add(saveButton);

        content.add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
        SwingUtilities.invokeLater(nameField::requestFocus);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return label;
    }

    private void onSave() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        String error = validate(name, email);
        if (error != null) {
            validationLabel.setText(error);
            return;
        }

        result = new Customer(name, email, phone);
        dispose();
    }

    private String validate(String name, String email) {
        if (name.isEmpty()) return "Name is required.";
        if (name.length() > 100) return "Name must be at most 100 characters.";
        if (email.isEmpty()) return "Email is required.";
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            return "Please enter a valid email address.";
        if (email.length() > 100) return "Email must be at most 100 characters.";
        return null;
    }

    public Customer getResult() {
        return result;
    }
}
