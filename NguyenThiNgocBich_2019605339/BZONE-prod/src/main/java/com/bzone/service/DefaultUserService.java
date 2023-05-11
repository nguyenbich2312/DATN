package com.bzone.service;

import com.bzone.exception.UserAlreadyExistsException;
import com.bzone.model.User;
import com.bzone.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service("userService")
public class DefaultUserService implements UserService {

    @Autowired
    private UserRepository userRepository;


    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final static String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";


    @Override
    public void
    addUser(User user) throws UserAlreadyExistsException {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if(existingUser != null) {
            throw new UserAlreadyExistsException("User already exists with email: " + user.getEmail());
        }
        if(!user.getPassword().matches(passwordRegex)) {
            throw new IllegalArgumentException("Password must contain at least one digit, one lowercase, one uppercase, one special character and must be at least 8 characters long.");
        }
        encodePassword(user);
        userRepository.save(user);
    }

    @Override
    public void changePassword(User user, String newPassword) {
        if(!newPassword.matches(passwordRegex)) {
            throw new IllegalArgumentException("Password must contain at least one digit, one lowercase, one uppercase, one special character and must be at least 8 characters long.");
        }
        user.setPassword(newPassword);
        encodePassword(user);
        userRepository.save(user);
    }

    @Override
    public String generatePassword() {
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String specialCharacters = "!@#$";
        String numbers = "1234567890";
        String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
        Random random = new Random();
        String password = "";
        password += lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
        password += capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
        password += specialCharacters.charAt(random.nextInt(specialCharacters.length()));
        password += numbers.charAt(random.nextInt(numbers.length()));
        for(int i = 4; i< 10 ; i++) {
            password += combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        return password;
    }

    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    public String encodePassword(String password) {

        return passwordEncoder.encode(password);
    }
}

