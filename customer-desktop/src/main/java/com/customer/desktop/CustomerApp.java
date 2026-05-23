package com.customer.desktop;

import com.customer.desktop.service.ApiService;
import com.customer.desktop.ui.LoginDialog;
import com.customer.desktop.ui.MainFrame;

import javax.swing.SwingUtilities;

public class CustomerApp {

    private static ApiService apiService;

    public static void main(String[] args) {
        String baseUrl = System.getProperty("api.url", "http://127.0.0.1:8080");
        apiService = new ApiService(baseUrl);

        SwingUtilities.invokeLater(CustomerApp::showLogin);
    }

    private static void showLogin() {
        LoginDialog loginDialog = new LoginDialog(null, apiService);
        loginDialog.setVisible(true);

        if (loginDialog.isAuthenticated()) {
            showMainFrame();
        } else {
            System.exit(0);
        }
    }

    private static void showMainFrame() {
        MainFrame frame = new MainFrame(apiService, CustomerApp::showLogin);
        frame.setVisible(true);
    }
}
