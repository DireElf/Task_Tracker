package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.LoginDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Set;

import static hexlet.code.config.security.SecurityConfig.LOGIN;
import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.controller.TaskStatusController.STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
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
public class UserControllerTest {

    private static final UserDto SAMPLE_USER_DTO = TestUtils.fromJson(
            TestUtils.readFixtureJson("sample_user_dto.json"),
            new TypeReference<>() {
            }
    );
    private static final UserDto ANOTHER_USER_DTO = TestUtils.fromJson(
            TestUtils.readFixtureJson("another_user_dto.json"),
            new TypeReference<>() {
            }
    );

    public static UserDto getSampleUserDto() {
        return SAMPLE_USER_DTO;
    }

    public static UserDto getAnotherUserDto() {
        return ANOTHER_USER_DTO;
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void initialization() throws Exception {
        utils.setUp();
    }

    @Test
    public void registration() throws Exception {
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH).andExpect(status().isCreated());
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void getUserById() throws Exception {
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH);
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
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH);
        utils.regEntity(ANOTHER_USER_DTO, USER_CONTROLLER_PATH);
        long expectedCount = userRepository.count();

        MockHttpServletResponse response = utils.perform(get(BASE_URL + USER_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<User> users = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) users.size()).isEqualTo(expectedCount);
        assertThat(users.get(0).getPassword()).isNull();
    }

    @Test
    void regTheSameUser() throws Exception {
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH).andExpect(status().isCreated());
        long expectedCount = userRepository.count();
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH).andExpect(status().isUnprocessableEntity());
        assertThat(userRepository.count()).isEqualTo(expectedCount);
    }

    @Test
    void login() throws Exception {
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH);
        LoginDto loginDto = new LoginDto(SAMPLE_USER_DTO.getEmail(), SAMPLE_USER_DTO.getPassword());

        MockHttpServletRequestBuilder loginRequest =
                post(BASE_URL + LOGIN).content(asJson(loginDto)).contentType(APPLICATION_JSON);

        utils.perform(loginRequest).andExpect(status().isOk());
    }

    @Test
    void loginUnregistered() throws Exception {
        LoginDto loginDto = new LoginDto(
                ANOTHER_USER_DTO.getEmail(),
                ANOTHER_USER_DTO.getPassword()
        );

        MockHttpServletRequestBuilder loginRequest =
                post(BASE_URL + LOGIN).content(asJson(loginDto)).contentType(APPLICATION_JSON);

        utils.perform(loginRequest).andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser() throws Exception {
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH);
        User userToUpdate = userRepository.findAll().get(0);

        MockHttpServletRequestBuilder updateRequest =
                put(BASE_URL + USER_CONTROLLER_PATH + ID, userToUpdate.getId())
                        .content(asJson(ANOTHER_USER_DTO))
                        .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, userToUpdate.getEmail()).andExpect(status().isOk());
        assertThat(userRepository.findById(userToUpdate.getId())).isPresent();
        String oldEmail = userToUpdate.getEmail();
        assertThat(userRepository.findByEmail(oldEmail)).isEmpty();
        assertThat(userRepository.findByEmail(ANOTHER_USER_DTO.getEmail())).isPresent();
    }

    @Test
    void deleteUser() throws Exception {
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH);
        long countBefore = userRepository.count();
        User userToDelete = userRepository.findAll().get(0);

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, userToDelete.getId()), userToDelete.getEmail())
                .andExpect(status().isOk());

        assertThat(userRepository.count()).isEqualTo(countBefore - 1);
    }

    @Test
    void deleteUserWithAssignedTask() throws Exception {
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH);
        User existingUser = userRepository.findAll().get(0);
        utils.regEntity(new TaskStatusDto("Sample status"), existingUser.getEmail(), STATUS_CONTROLLER_PATH);
        utils.regEntity(new LabelDto("Sample label"), existingUser.getEmail(), LABEL_CONTROLLER_PATH);
        TaskDto taskDto = new TaskDto(
                "Task name",
                "Task description",
                taskStatusRepository.findAll().get(0).getId(),
                userRepository.findAll().get(0).getId(),
                Set.of(labelRepository.findAll().get(0).getId())
        );
        utils.regEntity(taskDto, existingUser.getEmail(), TASK_CONTROLLER_PATH);
        long executorId = existingUser.getId();
        long countBefore = userRepository.count();

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, executorId), existingUser.getEmail())
                .andExpect(status().isUnprocessableEntity());

        assertThat(userRepository.count()).isEqualTo(countBefore);
    }

    @Test
    void deleteUserByAnother() throws Exception {
        utils.regEntity(SAMPLE_USER_DTO, USER_CONTROLLER_PATH);
        utils.regEntity(ANOTHER_USER_DTO, USER_CONTROLLER_PATH);
        User userToDelete = userRepository.findAll().get(0);
        User actualUser = userRepository.findAll().get(1);
        long countBefore = userRepository.count();

        utils.perform(delete(BASE_URL + USER_CONTROLLER_PATH + ID, userToDelete.getId()), actualUser.getEmail())
                .andExpect(status().isForbidden());

        assertThat(userRepository.count()).isEqualTo(countBefore);
    }
}
