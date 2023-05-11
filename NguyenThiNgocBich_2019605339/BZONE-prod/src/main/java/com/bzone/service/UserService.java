package com.bzone.service;

import com.bzone.exception.UserAlreadyExistsException;
import com.bzone.model.User;

public interface UserService {
    void addUser(User user) throws UserAlreadyExistsException;
    void changePassword(User user, String newPassword);
    String generatePassword();
}
