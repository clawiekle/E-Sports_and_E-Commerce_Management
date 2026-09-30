package main;

import gui.AdminDashboardFrame;
import gui.LoginFrame;
import model.Admin;
import model.Fan;
import model.User;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showLogin);
    }

    private static void showLogin() {
        new LoginFrame(Main::routeByRole).setVisible(true);
    }


    private static void routeByRole(User user) {
        if (user instanceof Admin) {
            openAdminUI((Admin) user);
        } else if (user instanceof Fan) {
            openFanUI((Fan) user);
        }
    }

    private static void openAdminUI(Admin admin) {
        new AdminDashboardFrame(admin, Main::showLogin).setVisible(true);
    }


    private static void openFanUI(Fan fan) {
        JOptionPane.showMessageDialog(null, "Welcome, " + fan.getUsername() + " (Fan). Fan UI coming next.");
        showLogin();
    }
}