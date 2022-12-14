package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(SpringConfigForIT.TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
public class TaskControllerTest {

    private final UserDto sampleUserDto = UserControllerTest.getSampleUserDto();
    private final UserDto anotherUserDto = UserControllerTest.getAnotherUserDto();
    private static String existingUserEmail;
    private static TaskDto sampleTaskDto;

    @Autowired
    private TaskRepository taskRepository;

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
        utils.regEntity(sampleUserDto, USER_CONTROLLER_PATH).andExpect(status().isCreated());
        existingUserEmail = userRepository.findAll().get(0).getEmail();
        long executorId = userRepository.findAll().get(0).getId();

        utils.regEntity(new TaskStatusDto("Sample status"), existingUserEmail, STATUS_CONTROLLER_PATH);
        utils.regEntity(new LabelDto("Sample label"), existingUserEmail, LABEL_CONTROLLER_PATH);
        sampleTaskDto = new TaskDto(
                "Sample task name",
                "Sample description",
                taskStatusRepository.findAll().get(0).getId(),
                executorId,
                Set.of(labelRepository.findAll().get(0).getId())
        );
    }

    @Test
    public void registration() throws Exception {
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH)
                .andExpect(status().isCreated());
        assertThat(taskRepository.count()).isEqualTo(1);
    }

    @Test
    public void getTaskById() throws Exception {
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH);
        Task expectedTask = taskRepository.findAll().get(0);
        long authorId = expectedTask.getAuthor().getId();
        String authorEmail = userRepository.findById(authorId).get().getEmail();

        MockHttpServletResponse response = utils.perform(
                        get(BASE_URL + TASK_CONTROLLER_PATH + ID, expectedTask.getId()), authorEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        Task task = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(task.getId()).isEqualTo(expectedTask.getId());
        assertThat(task.getName()).isEqualTo(expectedTask.getName());
    }

    @Test
    public void getAllTasks() throws Exception {
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH);
        TaskDto anotherTaskDto = new TaskDto();
        anotherTaskDto.setName("Another task name");
        anotherTaskDto.setTaskStatusId(sampleTaskDto.getTaskStatusId());
        anotherTaskDto.setExecutorId(sampleTaskDto.getExecutorId());
        utils.regEntity(anotherTaskDto, existingUserEmail, TASK_CONTROLLER_PATH);
        long expectedCount = taskRepository.count();

        MockHttpServletResponse response = utils.perform(get(BASE_URL + TASK_CONTROLLER_PATH), existingUserEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<Task> tasks = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) tasks.size()).isEqualTo(expectedCount);
    }

    @Test
    public void twiceRegTheSameTaskFail() throws Exception {
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH);
        long expectedCount = taskRepository.count();
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH)
                .andExpect(status().isUnprocessableEntity());
        assertThat(taskRepository.count()).isEqualTo(expectedCount);
    }

    @Test
    public void updateTask() throws Exception {
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH);
        Task taskToUpdate = taskRepository.findAll().get(0);
        long taskToUpdateId = taskToUpdate.getId();
        String authorEmail = taskToUpdate.getAuthor().getEmail();
        TaskDto anotherTaskDto = new TaskDto();
        anotherTaskDto.setName("Another task name");
        anotherTaskDto.setTaskStatusId(sampleTaskDto.getTaskStatusId());
        anotherTaskDto.setExecutorId(sampleTaskDto.getExecutorId());

        MockHttpServletRequestBuilder updateRequest = put(BASE_URL + TASK_CONTROLLER_PATH + ID, taskToUpdateId)
                .content(asJson(anotherTaskDto))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, authorEmail).andExpect(status().isOk());
        assertThat(taskRepository.existsById(taskToUpdateId)).isTrue();
        assertThat(taskRepository.findByName(taskToUpdate.getName())).isEmpty();
        assertThat(taskRepository.findByName(anotherTaskDto.getName())).isPresent();
    }

    @Test
    public void deleteTask() throws Exception {
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH);
        Task existingTask = taskRepository.findAll().get(0);
        long existingTaskId = existingTask.getId();
        String authorEmail = existingTask.getAuthor().getEmail();

        final long countBefore = taskRepository.count();

        utils.perform(delete(BASE_URL + TASK_CONTROLLER_PATH + ID, existingTaskId), authorEmail)
                .andExpect(status().isOk());

        assertThat(taskRepository.count()).isEqualTo(countBefore - 1);
    }


    @Test
    public void getFilteredTask() throws Exception {
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH);
        utils.regEntity(anotherUserDto, USER_CONTROLLER_PATH);
        User anotherUser = userRepository.findAll().get(1);
        String anotherUserEmail = anotherUser.getEmail();
        utils.regEntity(new TaskStatusDto("Another status"), anotherUserEmail, STATUS_CONTROLLER_PATH);
        utils.regEntity(new LabelDto("Another label"), anotherUserEmail, LABEL_CONTROLLER_PATH);
        TaskDto anotherTaskDto = new TaskDto(
                "Another task name",
                "Another description",
                taskStatusRepository.findAll().get(1).getId(),
                anotherUser.getId(),
                Set.of(labelRepository.findAll().get(1).getId())
        );
        utils.regEntity(anotherTaskDto, anotherUserEmail, TASK_CONTROLLER_PATH);

        long totalCount = taskRepository.count();
        long expectedCount = 1;

        Task taskToFind = taskRepository.findAll().get(0);
        long executorId = taskToFind.getExecutor().getId();
        long taskStatusId = taskToFind.getTaskStatus().getId();
        long labelId = taskToFind.getLabels().stream()
                .filter(label -> label.getName().equals("Sample label"))
                .findFirst()
                .get().getId();

        MockHttpServletResponse response = utils.perform(
                get(BASE_URL + TASK_CONTROLLER_PATH).param(
                        "executorId", executorId + "",
                        "taskStatus", taskStatusId + "",
                        "labels", labelId + ""
                ), existingUserEmail).andExpect(status().isOk()).andReturn().getResponse();

        List<Task> filteredTasks = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) filteredTasks.size()).isNotEqualTo(totalCount);
        assertThat((long) filteredTasks.size()).isEqualTo(expectedCount);
    }
}
