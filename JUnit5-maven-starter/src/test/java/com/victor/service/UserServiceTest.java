package com.victor.service;

import com.victor.TestBase;
import com.victor.dto.User;
import com.victor.extension.ConditionalExtension;
import com.victor.extension.GlobalExtension;
import com.victor.extension.PostProcessingExtension;
import com.victor.extension.ThrowableExtension;
import com.victor.extension.UserServiceParamResolver;
import com.victor.service.dao.UserDao;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.RepeatedTest.LONG_DISPLAY_NAME;

@Tag("fast")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        MockitoExtension.class
//        ConditionalExtension.class,
//        ThrowableExtension.class
//        GlobalExtension.class
})
class UserServiceTest extends TestBase {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;
    @Mock
    private UserDao userDao;
    @InjectMocks
    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
//        this.userDao = Mockito.spy(new UserDao());
//        this.userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
        Mockito.doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());

        assertThrows(RuntimeException.class, ()->userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.doReturn(true).when(userDao).delete(Mockito.any());

//        Mockito.when(userDao.delete(IVAN.getId()))
//                .thenReturn(true)
//                .thenReturn(false);

        var deleteResult = userService.delete(IVAN.getId());
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));

//        var integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(userDao, Mockito.times(3)).delete(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId());


        assertThat(deleteResult).isTrue();
    }

    @Test
//    @Order(1)
    void usersEmptyIfNoUserAdded() throws RuntimeException {
        if (true) {
            throw new RuntimeException();
        }
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

        MatcherAssert.assertThat(users, empty());
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
//    @Order(2)
    @DisplayName("user will be empty if no user added")
    void userSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN, PETR);

        var users = userService.getAll();

        assertThat(users).hasSize(2);
//        assertEquals(2, users.size());
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId()));

        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @AfterEach
    void deleteDataFromDataBase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }

    @Nested
    @Tag("login")
    @DisplayName("test user login functionality")
    class LoginTest {

        @Test
        @Disabled
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);

            Optional<User> maybeUser = userService.login(IVAN.getUserName(), "dummy");

            assertTrue(maybeUser.isEmpty());
        }

        //        @Test
        @RepeatedTest(value = 5, name = LONG_DISPLAY_NAME)
        void loginSuccessIfUserExists(RepetitionInfo repetitionInfo) {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUserName(), IVAN.getPassword());

            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        assertTrue(maybeUser.isPresent());
//        maybeUser.ifPresent(user -> assertEquals(IVAN,user));
        }

        @Test
        @Disabled
        void checkLoginFunctionalityPerformance() {
            var result = assertTimeout(Duration.ofMillis(200L), () -> {
                Thread.sleep(300L);
                return userService.login("dummy", IVAN.getPassword());
            });
        }

        @Test
            //    @org.junit.Test(expected = IllegalArgumentException.class)
        void throwExceptionIfUsernameOrPasswordIsNull() {

//        try{
//            userService.login(null, "dummy");
//            fail("login should throw exception on null username");
//        }catch (IllegalArgumentException ex){
//            assertTrue(true);
//        }

            assertAll(
                    () -> {
                        var exception = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"));
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );
        }

        @Test
        void loginFailIfUserDoesNotExist() {
            userService.add(IVAN);

            Optional<User> maybeUser = userService.login("dummy", IVAN.getPassword());

            assertTrue(maybeUser.isEmpty());
        }

        //        @ParameterizedTest
////        @ArgumentsSource()
//        @NullSource
//        @EmptySource
////        @NullAndEmptySource
////        @ValueSource
//        void loginParametrizedTest(String username) {
//            userService.add(IVAN, PETR);
//
//            var maybeUser = userService.login(username, null);
//        }
//
//        @ParameterizedTest
////        @ArgumentsSource()
////        @NullSource
////        @EmptySource
////        @NullAndEmptySource
//        @ValueSource(strings = {
//                "Ivan", "Petr"
//        })
////        @EnumSource
//        void loginParametrizedTest(String username) {
//            userService.add(IVAN, PETR);
//
//            var maybeUser = userService.login(username, null);
//        }
        @ParameterizedTest(name = "{arguments} test")
        @MethodSource("com.victor.service.UserServiceTest#getArgumentsForLoginTest")
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETR);

            var maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }

//        @ParameterizedTest
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
////        @CsvSource({
////                "Ivan,123",
////                "Petr,111"
////        })
//        void loginParametrizedTest(String username, Integer password) {
//            userService.add(IVAN, PETR);
//
//            var maybeUser = userService.login(username, null);
//            assertThat(maybeUser).isEqualTo(null);
//        }

    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Iva", "dummy", Optional.empty()),
                Arguments.of("dummy", "123", Optional.empty())
        );
    }
}
