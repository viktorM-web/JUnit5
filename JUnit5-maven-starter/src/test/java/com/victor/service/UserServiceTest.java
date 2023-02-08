package com.victor.service;

import com.victor.dto.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    private UserService userService;

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    void userSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        var users = userService.getAll();
        assertEquals(2, users.size());
    }

    @Test
    void loginSuccessIfUserExists() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUserName(), IVAN.getPassword());

        assertTrue(maybeUser.isPresent());
        maybeUser.ifPresent(user -> assertEquals(IVAN,user));

    }

    @Test
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(IVAN);

        Optional<User> maybeUser = userService.login(IVAN.getUserName(), "dummy");

        assertTrue(maybeUser.isEmpty());
    }

    @Test
    void loginFailIfUserDoesNotExist() {
        userService.add(IVAN);

        Optional<User> maybeUser = userService.login("dummy", IVAN.getPassword());

        assertTrue(maybeUser.isEmpty());
    }

    @AfterEach
    void deleteDataFromDataBase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }
}
