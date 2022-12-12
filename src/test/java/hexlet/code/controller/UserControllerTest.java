package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LoginDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.util.List;

import static hexlet.code.config.security.SecurityConfig.LOGIN;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(SpringConfigForIT.TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
@Sql(value = {"/script/before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/script/after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public final class UserControllerTest {

    private static final String USER_DTO_FIXTURE = "sample_user_dto.json";
    private static final String LOGIN_DTO_FIXTURE = "sample_login_dto.json";
    private static final String DEFAULT_PASSWORD = "12345";

    private static UserDto sampleUserDto;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtils utils;

    @BeforeAll
    public static void getUserDto() throws IOException {
        String userDtoJson = TestUtils.readFixtureJson(USER_DTO_FIXTURE);
        sampleUserDto = fromJson(userDtoJson, new TypeReference<>() {
        });
    }

    @Test
    public void registration() throws Exception {
        final long entriesBeforeReg = userRepository.count();
        utils.regEntity(sampleUserDto, USER_CONTROLLER_PATH).andExpect(status().isCreated());
        assertThat(userRepository.count()).isEqualTo(entriesBeforeReg + 1);
    }

    @Test
    void getUserById() throws Exception {
        User expectedUser = userRepository.findAll().get(0);

        MockHttpServletResponse response = utils.perform(
                        get(BASE_URL + USER_CONTROLLER_PATH + ID, expectedUser.getId()),
                        expectedUser.getEmail()
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse();

        User user = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(user.getEmail()).isEqualTo(expectedUser.getEmail());
        assertThat(user.getPassword()).isNull();
    }

    @Test
    void getAllUsers() throws Exception {
        final long expectedEntriesAmount = userRepository.count();

        MockHttpServletResponse response = utils.perform(get(BASE_URL + USER_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<User> users = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) users.size()).isEqualTo(expectedEntriesAmount);
        assertThat(users.get(0).getPassword()).isNull();
    }

    @Test
    void regTheSameUser() throws Exception {
        utils.regEntity(sampleUserDto, USER_CONTROLLER_PATH).andExpect(status().isCreated());
        final long expectedEntriesAmount = userRepository.count();

        utils.regEntity(sampleUserDto, USER_CONTROLLER_PATH).andExpect(status().isUnprocessableEntity());

        assertThat(userRepository.count()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    void login() throws Exception {
        User user = userRepository.findAll().get(0);
        LoginDto loginDto = new LoginDto(
                user.getEmail(),
                DEFAULT_PASSWORD
        );

        MockHttpServletRequestBuilder loginRequest =
                post(BASE_URL + LOGIN).content(asJson(loginDto)).contentType(APPLICATION_JSON);

        utils.perform(loginRequest).andExpect(status().isOk());
    }

    @Test
    void loginUnregistered() throws Exception {
        String loginDtoJson = TestUtils.readFixtureJson(LOGIN_DTO_FIXTURE);
        LoginDto loginDto = fromJson(loginDtoJson, new TypeReference<>() {
        });
        MockHttpServletRequestBuilder loginRequest =
                post(BASE_URL + LOGIN).content(asJson(loginDto)).contentType(APPLICATION_JSON);

        utils.perform(loginRequest).andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser() throws Exception {
        User userToUpdate = userRepository.findAll().get(0);

        MockHttpServletRequestBuilder updateRequest =
                put(BASE_URL + USER_CONTROLLER_PATH + ID, userToUpdate.getId())
                        .content(asJson(sampleUserDto))
                        .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, userToUpdate.getEmail()).andExpect(status().isOk());

        assertThat(userRepository.findById(userToUpdate.getId())).isPresent();

        String oldEmail = userToUpdate.getEmail();
        assertThat(userRepository.findByEmail(oldEmail)).isEmpty();

        assertThat(userRepository.findByEmail(sampleUserDto.getEmail())).isPresent();
    }

    @Test
    void deleteUser() throws Exception {
        utils.regEntity(sampleUserDto, USER_CONTROLLER_PATH).andExpect(status().isCreated());
        final long entriesAmountBefore = userRepository.count();

        User userToDelete = userRepository.findByEmail(sampleUserDto.getEmail()).get();
        long userToDeleteId = userToDelete.getId();

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, userToDeleteId), sampleUserDto.getEmail())
                .andExpect(status().isOk());

        assertThat(userRepository.count()).isEqualTo(entriesAmountBefore - 1);
    }

    @Test
    void deleteUserWithAssignedTask() throws Exception {
        User existingUser = userRepository.findAll().get(0);
        final long existingUserWithTaskId = existingUser.getId();

        final long entriesAmountBefore = userRepository.count();

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, existingUserWithTaskId), existingUser.getEmail())
                .andExpect(status().isUnprocessableEntity());

        assertThat(userRepository.count()).isEqualTo(entriesAmountBefore);
    }

    @Test
    void deleteUserByAnother() throws Exception {
        long userToDelete = userRepository.findAll().get(0).getId();
        utils.regEntity(sampleUserDto, USER_CONTROLLER_PATH).andExpect(status().isCreated());

        final long entriesAmountBefore = userRepository.count();

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, userToDelete), sampleUserDto.getEmail())
                .andExpect(status().isForbidden());

        assertThat(userRepository.count()).isEqualTo(entriesAmountBefore);
    }
}
