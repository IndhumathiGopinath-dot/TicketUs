package com.ticketsystem;

import com.ticketsystem.dto.AuthDtos;
import com.ticketsystem.exception.AppException;
import com.ticketsystem.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@SpringBootTest
public class AuthServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AuthService authService;

    @Test
    public void testSignupAndLogin() {
        AuthDtos.SignupRequest signup = AuthDtos.SignupRequest.builder()
                .name("Test User")
                .email("test_" + System.currentTimeMillis() + "@test.com")
                .password("password123")
                .role("EMPLOYEE")
                .department("Engineering")
                .build();

        AuthDtos.AuthResponse signupResp = authService.signup(signup);
        Assert.assertNotNull(signupResp.getToken());
        Assert.assertEquals(signupResp.getEmail(), signup.getEmail());

        AuthDtos.LoginRequest login = AuthDtos.LoginRequest.builder()
                .email(signup.getEmail())
                .password("password123")
                .build();
        AuthDtos.AuthResponse loginResp = authService.login(login);
        Assert.assertNotNull(loginResp.getToken());
    }

    @Test(expectedExceptions = AppException.class)
    public void testDuplicateSignupFails() {
        String email = "dup_" + System.currentTimeMillis() + "@test.com";
        AuthDtos.SignupRequest signup = AuthDtos.SignupRequest.builder()
                .name("Dup")
                .email(email)
                .password("password123")
                .role("EMPLOYEE")
                .department("Engineering")
                .build();
        authService.signup(signup);
        authService.signup(signup); // should throw
    }

    @Test(expectedExceptions = AppException.class)
    public void testWrongPasswordFails() {
        String email = "wrong_" + System.currentTimeMillis() + "@test.com";
        AuthDtos.SignupRequest signup = AuthDtos.SignupRequest.builder()
                .name("X")
                .email(email)
                .password("password123")
                .role("EMPLOYEE")
                .department("Engineering")
                .build();
        authService.signup(signup);

        AuthDtos.LoginRequest login = AuthDtos.LoginRequest.builder()
                .email(email)
                .password("wrongpass")
                .build();
        authService.login(login);
    }
}
