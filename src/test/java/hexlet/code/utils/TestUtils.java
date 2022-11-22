package hexlet.code.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import hexlet.code.component.JWTHelper;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.controller.TaskStatusController.STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class TestUtils {
    public static final String BASE_URL = "/api";
    public static final String TEST_EMAIL_1 = "email@email.com";
    public static final String TEST_EMAIL_2 = "email2@email.com";
    public static final String TEST_STATUS_1 = "status1";
    public static final String TEST_STATUS_2 = "status2";

    private final UserDto testUserDto = new UserDto(
            TEST_EMAIL_1,
            "fname",
            "lname",
            "pwd"
    );

    private final TaskStatusDto testTaskStatusDto = new TaskStatusDto(
            TEST_STATUS_1
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTHelper jwtHelper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    public void tearDown() {
        // ordered execution
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    // User utils

    public UserDto getTestUserDto() {
        return testUserDto;
    }

    public ResultActions regDefaultUser() throws Exception {
        return regUser(testUserDto);
    }

    public ResultActions regUser(final UserDto dto) throws Exception {
        final var request = post(BASE_URL + USER_CONTROLLER_PATH)
                .content(asJson(dto))
                .contentType(APPLICATION_JSON);

        return perform(request);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request, final String byUser) throws Exception {
        final String token = jwtHelper.expiring(Map.of("username", byUser));
        request.header(AUTHORIZATION, token);

        return perform(request);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static String asJson(final Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T fromJson(final String json, final TypeReference<T> to) throws JsonProcessingException {
        return MAPPER.readValue(json, to);
    }

    // TaskStatus utils

    public ResultActions regDefaultStatus(final String byUser) throws Exception {
        return regStatus(testTaskStatusDto, byUser);
    }

    public ResultActions regStatus(final TaskStatusDto dto, final String byUser) throws Exception {
        final var request = post(BASE_URL + STATUS_CONTROLLER_PATH)
                .content(asJson(dto))
                .contentType(APPLICATION_JSON);

        return perform(request, byUser);
    }

    // Task utils

    public ResultActions regDefaultTask(final String byUser) throws Exception {
        regDefaultUser();
        regDefaultStatus(TEST_EMAIL_1);

        final User user = userRepository.findAll().get(0);
        final TaskStatus taskStatus = taskStatusRepository.findAll().get(0);
        final TaskDto testRegTaskDto = new TaskDto(
                "Task name", "Task description", taskStatus.getId(), user.getId()
        );

        return regTask(testRegTaskDto, byUser);
    }

    public ResultActions regTask(final TaskDto dto, final String byUser) throws Exception {
        final var request = post(BASE_URL + TASK_CONTROLLER_PATH)
                .content(asJson(dto))
                .contentType(APPLICATION_JSON);

        return perform(request, byUser);
    }
}
