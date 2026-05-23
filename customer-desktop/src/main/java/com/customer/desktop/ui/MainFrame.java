package com.customer.desktop.ui;

import com.customer.desktop.model.Customer;
import com.customer.desktop.service.ApiService;
import com.customer.desktop.service.ApiService.ApiException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {

    private static final Color BG_DARK = new Color(26, 27, 46);
    private static final Color BG_HEADER = new Color(45, 27, 105);
    private static final Color BG_TOOLBAR = new Color(34, 35, 64);
    private static final Color BG_STATUS = new Color(22, 23, 40);
    private static final Color ACCENT_PURPLE = new Color(124, 92, 191);
    private static final Color TEXT_PRIMARY = new Color(224, 224, 224);
    private static final Color TEXT_SECONDARY = new Color(107, 109, 138);
    private static final Color DANGER_COLOR = new Color(255, 107, 138);
    private static final Color BORDER_COLOR = new Color(42, 43, 74);
    private static final Color ALT_ROW_COLOR = new Color(30, 31, 53);
    private static final Color SELECTION_BG = new Color(61, 45, 107);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApiService apiService;
    private final Runnable logoutCallback;
    private DefaultTableModel tableModel;
    private JTable customerTable;
    private JTextField searchField;
    private JLabel statusLabel;
    private JButton editButton;
    private JButton deleteButton;
    private JPanel glassPane;
    private List<Customer> currentCustomers;

    public MainFrame(ApiService apiService, Runnable logoutCallback) {
        this.apiService = apiService;
        this.logoutCallback = logoutCallback;
        initComponents();
        loadCustomers();
    }

    private void initComponents() {
        setTitle("Customer Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 660);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_DARK);
        setContentPane(mainPanel);

        glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(26, 27, 46, 180));
                g2.fillRect(0, 0, getWidth(), getHeight());
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(ACCENT_PURPLE);
                g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cx - 20, cy - 20, 40, 40, (int) (System.currentTimeMillis() / 5 % 360), 270);
                g2.dispose();
            }
        };
        glassPane.setOpaque(false);
        glassPane.setVisible(false);
        setGlassPane(glassPane);

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setBackground(BG_DARK);
        centerPanel.add(createToolbar(), BorderLayout.NORTH);
        centerPanel.add(createTable(), BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
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
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        header.setOpaque(false);

        JLabel title = new JLabel("Customer Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setForeground(new Color(255, 255, 255, 200));
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.addActionListener(e -> onLogout());
        header.add(logoutBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(10, 20, 10, 20)));

        searchField = new JTextField(20);
        searchField.setMaximumSize(new Dimension(260, 36));
        searchField.setPreferredSize(new Dimension(260, 36));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or email...");
        searchField.addActionListener(e -> onSearch());
        toolbar.add(searchField);
        toolbar.add(Box.createHorizontalStrut(8));

        JButton searchBtn = createButton("Search", null, false);
        searchBtn.addActionListener(e -> onSearch());
        toolbar.add(searchBtn);

        toolbar.add(Box.createHorizontalGlue());

        JButton addButton = createButton("Add", ACCENT_PURPLE, true);
        addButton.addActionListener(e -> onAdd());
        toolbar.add(addButton);
        toolbar.add(Box.createHorizontalStrut(8));

        editButton = createButton("Edit", null, false);
        editButton.setEnabled(false);
        editButton.addActionListener(e -> onEdit());
        toolbar.add(editButton);
        toolbar.add(Box.createHorizontalStrut(8));

        deleteButton = createButton("Delete", null, false);
        deleteButton.setForeground(DANGER_COLOR);
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> onDelete());
        toolbar.add(deleteButton);
        toolbar.add(Box.createHorizontalStrut(16));

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setMaximumSize(new Dimension(2, 30));
        sep.setForeground(new Color(58, 59, 90));
        toolbar.add(sep);
        toolbar.add(Box.createHorizontalStrut(16));

        JButton refreshBtn = createButton("Refresh", null, false);
        refreshBtn.addActionListener(e -> onRefresh());
        toolbar.add(refreshBtn);

        return toolbar;
    }

    private JButton createButton(String text, Color bgColor, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 24, 36));
        button.setMaximumSize(new Dimension(200, 36));

        if (isPrimary && bgColor != null) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
        }

        return button;
    }

    private JScrollPane createTable() {
        String[] columns = {"ID", "Name", "Email", "Phone", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setRowHeight(40);
        customerTable.setShowGrid(false);
        customerTable.setIntercellSpacing(new Dimension(0, 1));
        customerTable.setFillsViewportHeight(true);
        customerTable.setBackground(BG_DARK);
        customerTable.setForeground(TEXT_PRIMARY);
        customerTable.setSelectionBackground(SELECTION_BG);
        customerTable.setSelectionForeground(Color.WHITE);
        customerTable.setGridColor(BG_TOOLBAR);
        customerTable.setFont(new Font("SansSerif", Font.PLAIN, 13));

        int[] colWidths = {60, 180, 220, 150, 180};
        for (int i = 0; i < colWidths.length; i++) {
            customerTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        customerTable.getColumnModel().getColumn(0).setMaxWidth(80);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? BG_DARK : ALT_ROW_COLOR);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        };
        for (int i = 0; i < customerTable.getColumnCount(); i++) {
            customerTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JTableHeader header = customerTable.getTableHeader();
        header.setBackground(BG_TOOLBAR);
        header.setForeground(TEXT_SECONDARY);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(BG_TOOLBAR);
                setForeground(TEXT_SECONDARY);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR),
                        new EmptyBorder(0, 10, 0, 10)));
                setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        });

        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = customerTable.getSelectedRow() >= 0;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });

        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && customerTable.getSelectedRow() >= 0) {
                    onEdit();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_DARK);
        return scrollPane;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(BG_STATUS);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(6, 20, 6, 20)));
        statusBar.setPreferredSize(new Dimension(0, 30));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusBar.add(statusLabel, BorderLayout.WEST);

        return statusBar;
    }

    private void onAdd() {
        CustomerDialog dialog = new CustomerDialog(this, null);
        dialog.setVisible(true);

        Customer result = dialog.getResult();
        if (result != null) {
            runAsync("Creating customer...", () -> {
                try {
                    apiService.createCustomer(result);
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Customer created successfully.");
                        loadCustomers();
                    });
                } catch (ApiException e) {
                    SwingUtilities.invokeLater(() -> showError("Create Failed", e.getMessage()));
                }
            });
        }
    }

    private void onEdit() {
        int row = customerTable.getSelectedRow();
        if (row < 0 || currentCustomers == null || row >= currentCustomers.size())
            return;

        Customer selected = currentCustomers.get(row);
        CustomerDialog dialog = new CustomerDialog(this, selected);
        dialog.setVisible(true);

        Customer result = dialog.getResult();
        if (result != null) {
            runAsync("Updating customer...", () -> {
                try {
                    apiService.updateCustomer(selected.getId(), result);
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Customer updated successfully.");
                        loadCustomers();
                    });
                } catch (ApiException e) {
                    SwingUtilities.invokeLater(() -> showError("Update Failed", e.getMessage()));
                }
            });
        }
    }

    private void onDelete() {
        int row = customerTable.getSelectedRow();
        if (row < 0 || currentCustomers == null || row >= currentCustomers.size())
            return;

        Customer selected = currentCustomers.get(row);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete \"" + selected.getName() + "\"?\n"
                        + "This action cannot be undone.",
                "Delete Customer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            runAsync("Deleting customer...", () -> {
                try {
                    apiService.deleteCustomer(selected.getId());
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Customer deleted successfully.");
                        loadCustomers();
                    });
                } catch (ApiException e) {
                    SwingUtilities.invokeLater(() -> showError("Delete Failed", e.getMessage()));
                }
            });
        }
    }

    private void onRefresh() {
        searchField.setText("");
        loadCustomers();
    }

    private void onSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadCustomers();
        } else {
            runAsync("Searching...", () -> {
                try {
                    List<Customer> results = apiService.searchCustomers(query);
                    SwingUtilities.invokeLater(() -> {
                        populateTable(results);
                        setStatus("Found " + results.size() + " customer(s) matching \""
                                + query + "\"");
                    });
                } catch (ApiException e) {
                    SwingUtilities.invokeLater(() -> showError("Search Failed", e.getMessage()));
                }
            });
        }
    }

    private void loadCustomers() {
        runAsync("Loading customers...", () -> {
            try {
                List<Customer> customers = apiService.getAllCustomers();
                SwingUtilities.invokeLater(() -> {
                    populateTable(customers);
                    setStatus("Loaded " + customers.size() + " customer(s)");
                });
            } catch (ApiException e) {
                SwingUtilities.invokeLater(() -> {
                    clearTable();
                    showError("Connection Error", e.getMessage());
                });
            }
        });
    }

    private void populateTable(List<Customer> customers) {
        this.currentCustomers = customers;
        tableModel.setRowCount(0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getEmail(),
                    c.getPhone() != null ? c.getPhone() : "",
                    c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FMT) : ""
            });
        }
    }

    private void clearTable() {
        this.currentCustomers = null;
        tableModel.setRowCount(0);
    }

    private void onLogout() {
        runAsync("Logging out...", () -> {
            try {
                apiService.logout();
            } catch (ApiService.ApiException ignored) {
            }
            SwingUtilities.invokeLater(() -> {
                dispose();
                logoutCallback.run();
            });
        });
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String title, String message) {
        setStatus("Error: " + message);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void runAsync(String statusMessage, Runnable task) {
        setStatus(statusMessage);
        glassPane.setVisible(true);

        Timer animTimer = new Timer(30, e -> glassPane.repaint());
        animTimer.start();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                animTimer.stop();
                glassPane.setVisible(false);
            }
        };
        worker.execute();
    }
}
