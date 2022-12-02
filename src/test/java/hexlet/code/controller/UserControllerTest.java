package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LoginDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtils;
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

import java.util.List;

import static hexlet.code.config.security.SecurityConfig.LOGIN;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.DEFAULT_USER_EMAIL;
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
@Sql(value = {"/initial_script.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public final class UserControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtils utils;

    @Test
    public void registration() throws Exception {
        final long entriesBeforeReg = userRepository.count();
        utils.regDefaultUser().andExpect(status().isCreated());
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
    void twiceRegTheSameUserFail() throws Exception {
        utils.regDefaultUser().andExpect(status().isCreated());
        final long expectedEntriesAmount = userRepository.count();

        utils.regDefaultUser().andExpect(status().isUnprocessableEntity());

        assertThat(userRepository.count()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    void login() throws Exception {
        User user = userRepository.findAll().get(0);
        LoginDto loginDto = new LoginDto(
                user.getEmail(),
                "12345"
        );

        MockHttpServletRequestBuilder loginRequest =
                post(BASE_URL + LOGIN).content(asJson(loginDto)).contentType(APPLICATION_JSON);

        utils.perform(loginRequest).andExpect(status().isOk());
    }

    @Test
    void loginFail() throws Exception {
        LoginDto loginDto = new LoginDto(
                utils.getTestUserDto().getEmail(),
                utils.getTestUserDto().getPassword()
        );

        MockHttpServletRequestBuilder loginRequest =
                post(BASE_URL + LOGIN).content(asJson(loginDto)).contentType(APPLICATION_JSON);

        utils.perform(loginRequest).andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser() throws Exception {

        User userToUpdate = userRepository.findAll().get(0);
        UserDto userDto = utils.getTestUserDto();

        MockHttpServletRequestBuilder updateRequest =
                put(BASE_URL + USER_CONTROLLER_PATH + ID, userToUpdate.getId())
                        .content(asJson(userDto))
                        .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, userToUpdate.getEmail()).andExpect(status().isOk());

        assertThat(userRepository.findById(userToUpdate.getId())).isPresent();

        String oldEmail = userToUpdate.getEmail();
        assertThat(userRepository.findByEmail(oldEmail)).isEmpty();

        assertThat(userRepository.findByEmail(userDto.getEmail())).isPresent();
    }

    @Test
    void deleteUser() throws Exception {
        utils.regDefaultUser();
        final long entriesAmountBefore = userRepository.count();

        User userToDelete = userRepository.findByEmail(DEFAULT_USER_EMAIL).get();
        long userToDeleteId = userToDelete.getId();

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, userToDeleteId), DEFAULT_USER_EMAIL)
                .andExpect(status().isOk());

        assertThat(userRepository.count()).isEqualTo(entriesAmountBefore - 1);
    }

    @Test
    void deleteUserWithTaskFails() throws Exception {
        final long existingUserWithTaskId = 1;
        final long entriesAmountBefore = userRepository.count();

        User existingUser = userRepository.findById(existingUserWithTaskId).get();

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, existingUserWithTaskId), existingUser.getEmail())
                .andExpect(status().isUnprocessableEntity());

        assertThat(userRepository.count()).isEqualTo(entriesAmountBefore);
    }

    @Test
    void deleteUserFails() throws Exception {
        long userToDelete = userRepository.findAll().get(0).getId();
        utils.regDefaultUser();
        final long entriesAmountBefore = userRepository.count();

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, userToDelete), DEFAULT_USER_EMAIL)
                .andExpect(status().isForbidden());

        assertThat(userRepository.count()).isEqualTo(entriesAmountBefore);
    }
}
