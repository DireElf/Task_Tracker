package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
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

import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
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
@Sql(value = {"/script/before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/script/after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TaskControllerTest {

    private static final String TASK_DTO_FIXTURE = "sample_task_dto.json";

    private static TaskDto sampleTaskDto;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtils utils;

    @BeforeAll
    public static void getTaskDto() throws IOException {
        String taskDtoJson = TestUtils.readFixtureJson(TASK_DTO_FIXTURE);
        sampleTaskDto = fromJson(taskDtoJson, new TypeReference<>() {
        });
    }

    @Test
    public void registration() throws Exception {
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();
        final long entriesAmountBefore = taskRepository.count();
        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH)
                .andExpect(status().isCreated());
        assertThat(taskRepository.count()).isEqualTo(entriesAmountBefore + 1);
    }

    @Test
    public void getTaskById() throws Exception {
        final Task expectedTask = taskRepository.findAll().get(0);
        long authorId = expectedTask.getAuthor().getId();
        String authorEmail = userRepository.findById(authorId).get().getEmail();

        MockHttpServletResponse response = utils.perform(
                        get(BASE_URL + TASK_CONTROLLER_PATH + ID,
                                expectedTask.getId()), authorEmail)
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
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();
        long expectedEntriesAmount = userRepository.count();

        MockHttpServletResponse response = utils.perform(get(BASE_URL + TASK_CONTROLLER_PATH), existingUserEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<Task> tasks = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) tasks.size()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    public void twiceRegTheSameTaskFail() throws Exception {
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH)
                .andExpect(status().isCreated());

        final long expectedEntriesAmount = taskRepository.count();

        utils.regEntity(sampleTaskDto, existingUserEmail, TASK_CONTROLLER_PATH)
                .andExpect(status().isUnprocessableEntity());

        assertThat(taskRepository.count()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    public void updateTask() throws Exception {
        final Task taskToUpdate = taskRepository.findAll().get(0);
        long taskToUpdateId = taskToUpdate.getId();
        final String authorEmail = taskToUpdate.getAuthor().getEmail();

        MockHttpServletRequestBuilder updateRequest = put(BASE_URL + TASK_CONTROLLER_PATH + ID,
                taskToUpdateId)
                .content(asJson(sampleTaskDto))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, authorEmail).andExpect(status().isOk());
        assertThat(taskRepository.existsById(taskToUpdateId)).isTrue();
        assertThat(taskRepository.findByName(taskToUpdate.getName())).isEmpty();
        assertThat(taskRepository.findByName(sampleTaskDto.getName())).isPresent();
    }

    @Test
    public void deleteTask() throws Exception {
        final Task existingTask = taskRepository.findAll().get(0);
        long existingTaskId = existingTask.getId();
        String authorEmail = existingTask.getAuthor().getEmail();

        final long entriesAmountBefore = taskRepository.count();

        utils.perform(delete(BASE_URL + TASK_CONTROLLER_PATH + ID, existingTaskId), authorEmail)
                .andExpect(status().isOk());

        assertThat(taskRepository.count()).isEqualTo(entriesAmountBefore - 1);
    }


    @Test
    public void getFilteredTask() throws Exception {
        long totalEntriesAmount = taskRepository.count();
        long expectedEntriesAmount = 2;
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        long existingUserId = 1;
        long existingTaskStatusId = 1;
        long existingLabelId = 1;

        MockHttpServletResponse response = utils.perform(
                get(BASE_URL + TASK_CONTROLLER_PATH).param(
                        "executorId", existingUserId + "",
                        "taskStatus", existingTaskStatusId + "",
                        "labels", existingLabelId + ""
                ), existingUserEmail).andExpect(status().isOk()).andReturn().getResponse();

        List<Task> filteredTasks = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) filteredTasks.size()).isNotEqualTo(totalEntriesAmount);
        assertThat((long) filteredTasks.size()).isEqualTo(expectedEntriesAmount);
    }
}
