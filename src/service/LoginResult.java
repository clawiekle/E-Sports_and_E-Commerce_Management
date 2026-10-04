package service;

import model.User;

/** Outcome of a login attempt, following the login flow. */
public class LoginResult {

    public enum Status { USER_NOT_FOUND, WRONG_PASSWORD, SUCCESS }

    private final Status status;
    private final User user; // only set when status == SUCCESS

    public LoginResult(Status status, User user) {
        this.status = status;
        this.user = user;
    }

    public Status getStatus() { return status; }
    public User getUser() { return user; }
}