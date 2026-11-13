package com.rssfeedgenerator.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rssfeedgenerator.userservice.dto.LoginRequest;
import com.rssfeedgenerator.userservice.dto.RegisterRequest;
import com.rssfeedgenerator.userservice.dto.UpdateProfileRequest;
import com.rssfeedgenerator.userservice.dto.UserResponse;
import com.rssfeedgenerator.userservice.exception.DuplicateEmailException;
import com.rssfeedgenerator.userservice.exception.InvalidCredentialsException;
import com.rssfeedgenerator.userservice.exception.UserExceptionHandler;
import com.rssfeedgenerator.userservice.exception.UserNotFoundException;
import com.rssfeedgenerator.userservice.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {
    private StubUserService userService;
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        userService = new StubUserService();
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new UserExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() { validator.close(); }

    @Test
    void registerReturnsCreatedLocationAndSafeProfile() throws Exception {
        userService.registerResponse = new UserResponse(12L, "Gyan Gaurav", "gyan@example.com");

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Gyan Gaurav","email":"gyan@example.com","password":"Password123!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/users/12"))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.email").value("gyan@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerRejectsBlankNameInvalidEmailAndShortPassword() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages.length()").value(3));
    }

    @Test
    void registerRejectsNameLongerThanMaximum() throws Exception {
        String tooLong = "N".repeat(121);
        String json = new ObjectMapper().writeValueAsString(java.util.Map.of(
                "name", tooLong, "email", "gyan@example.com", "password", "Password123!"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Name must be at most 120 characters."));
    }

    @Test
    void loginRejectsInvalidEmailBeforeServiceInvocation() throws Exception {
        mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad\",\"password\":\"Password123!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Enter a valid email address."));
        org.assertj.core.api.Assertions.assertThat(userService.loginCalls).isZero();
    }

    @Test
    void loginMapsInvalidCredentialsToUnauthorized() throws Exception {
        userService.loginFailure = new InvalidCredentialsException();

        mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gyan@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"));
    }

    @Test
    void registerMapsDuplicateEmailToConflict() throws Exception {
        userService.registerFailure = new DuplicateEmailException();

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Gyan\",\"email\":\"gyan@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void profileUpdateReturnsSafeResponse() throws Exception {
        userService.profileResponse = new UserResponse(12L, "Gyan G.", "new@example.com");

        mockMvc.perform(put("/api/users/12").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Gyan G.\",\"email\":\"new@example.com\",\"currentPassword\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gyan G."))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void profileUpdateMapsMissingAccountToNotFound() throws Exception {
        userService.profileFailure = new UserNotFoundException(404L);

        mockMvc.perform(put("/api/users/404").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Gyan\",\"email\":\"gyan@example.com\",\"currentPassword\":\"Password123!\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private static final class StubUserService extends UserService {
        private UserResponse registerResponse = new UserResponse(1L, "Gyan", "gyan@example.com");
        private UserResponse loginResponse = new UserResponse(1L, "Gyan", "gyan@example.com");
        private UserResponse profileResponse = new UserResponse(1L, "Gyan", "gyan@example.com");
        private RuntimeException registerFailure;
        private RuntimeException loginFailure;
        private RuntimeException profileFailure;
        private int loginCalls;

        private StubUserService() { super(null, null, null); }

        @Override public UserResponse register(RegisterRequest request) {
            if (registerFailure != null) throw registerFailure;
            return registerResponse;
        }
        @Override public UserResponse login(LoginRequest request) {
            loginCalls++;
            if (loginFailure != null) throw loginFailure;
            return loginResponse;
        }
        @Override public UserResponse updateProfile(Long id, UpdateProfileRequest request) {
            if (profileFailure != null) throw profileFailure;
            return profileResponse;
        }
    }
}
