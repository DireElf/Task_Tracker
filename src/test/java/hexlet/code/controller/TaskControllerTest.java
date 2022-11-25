package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.TEST_EMAIL_1;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void registration() throws Exception {
        assertThat(taskRepository.count()).isZero();
        utils.regDefaultTask(TEST_EMAIL_1).andExpect(status().isCreated());
        assertThat(taskRepository.count()).isEqualTo(1);
    }

    @Test
    public void getTaskById() throws Exception {
        utils.regDefaultTask(TEST_EMAIL_1);
        final Task expectedTask = taskRepository.findAll().get(0);

        final var response = utils.perform(
                        get(BASE_URL + TASK_CONTROLLER_PATH + ID,
                                expectedTask.getId()), TEST_EMAIL_1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Task task = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(task.getId()).isEqualTo(expectedTask.getId());
        assertThat(task.getName()).isEqualTo(expectedTask.getName());
    }

    @Test
    public void getAllTasks() throws Exception {
        utils.regDefaultTask(TEST_EMAIL_1);

        final var response = utils.perform(get(BASE_URL + TASK_CONTROLLER_PATH), TEST_EMAIL_1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<Task> tasks = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(tasks).hasSize(1);
    }

    @Test
    public void twiceRegTheSameTaskFail() throws Exception {
        utils.regDefaultTask(TEST_EMAIL_1).andExpect(status().isCreated());
        utils.regDefaultTask(TEST_EMAIL_1).andExpect(status().isUnprocessableEntity());
        assertEquals(1, taskRepository.count());
    }

    @Test
    public void updateTask() throws Exception {
        utils.regDefaultTask(TEST_EMAIL_1);
        Task task = taskRepository.findAll().get(0);
        final Long taskId = task.getId();

        final var testTaskDto = new TaskDto(
                "TaskDto name",
                "Task description",
                task.getTaskStatus().getId(),
                task.getExecutor().getId(),
                null
        );

        final var updateRequest = put(BASE_URL + TASK_CONTROLLER_PATH + ID,
                taskId)
                .content(asJson(testTaskDto))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, TEST_EMAIL_1).andExpect(status().isOk());
        assertThat(taskRepository.existsById(taskId)).isTrue();
        assertThat(taskRepository.findByName(task.getName())).isEmpty();
        assertThat(taskRepository.findByName(testTaskDto.getName())).isPresent();
    }

    @Test
    public void deleteTask() throws Exception {
        utils.regDefaultTask(TEST_EMAIL_1);
        final Long taskId = taskRepository.findAll().get(0).getId();

        utils.perform(delete(BASE_URL + TASK_CONTROLLER_PATH + ID, taskId), TEST_EMAIL_1)
                .andExpect(status().isOk());
        assertThat(taskRepository.count()).isZero();
    }


    @Test
    public void getFilteredTask() throws Exception {
        utils.regDefaultTask(TEST_EMAIL_1);
        Task task = taskRepository.findAll().get(0);
        long statusId = task.getTaskStatus().getId();
        long executorId = task.getExecutor().getId();
        String queryString = String.format("/?taskStatus=%d&executorId=%d", statusId, executorId);
        var response = utils.perform(
                get(BASE_URL + TASK_CONTROLLER_PATH + queryString), TEST_EMAIL_1
        ).andReturn().getResponse();

        final Iterable<Task> tasks = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(tasks).hasSize(1);
    }
}
