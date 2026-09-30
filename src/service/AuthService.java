package service;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class AuthService {

    public static final int MIN_USERNAME = 3;
    public static final int MAX_USERNAME = 20;
    public static final int MIN_PASSWORD = 6;

    private final UserDAO userDAO = new UserDAO();

    public LoginResult login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return new LoginResult(LoginResult.Status.USER_NOT_FOUND, null);
        }
        if (!user.getPassword().equals(password)) {
            return new LoginResult(LoginResult.Status.WRONG_PASSWORD, null);
        }
        return new LoginResult(LoginResult.Status.SUCCESS, user);
    }

    public boolean usernameExists(String username) throws SQLException {
        return username != null && userDAO.usernameExists(username.trim());
    }


    public String registerFan(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) return "Enter a username.";
        username = username.trim();

        if (username.length() < MIN_USERNAME || username.length() > MAX_USERNAME) {
            return "Username must be " + MIN_USERNAME + "-" + MAX_USERNAME + " characters.";
        }
        if (!username.matches("[A-Za-z0-9_]+")) {
            return "Username can only use letters, numbers and _.";
        }
        if (password == null || password.length() < MIN_PASSWORD) {
            return "Password must be at least " + MIN_PASSWORD + " characters.";
        }
        if (userDAO.usernameExists(username)) {
            return "That username is already taken.";
        }

        try {
            userDAO.createFan(username, password);
        } catch (SQLIntegrityConstraintViolationException dup) {
            return "That username is already taken.";
        }
        return null;
    }
}