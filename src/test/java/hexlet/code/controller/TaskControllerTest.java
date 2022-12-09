package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
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
import java.util.Set;

import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.DEFAULT_USER_EMAIL;
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

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TestUtils utils;

    @Test
    public void registration() throws Exception {
        final long entriesAmountBefore = taskRepository.count();
        utils.regDefaultTask(DEFAULT_USER_EMAIL).andExpect(status().isCreated());
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

        utils.regDefaultTask(existingUserEmail).andExpect(status().isCreated());

        final long expectedEntriesAmount = taskRepository.count();

        utils.regDefaultTask(existingUserEmail).andExpect(status().isUnprocessableEntity());

        assertThat(taskRepository.count()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    public void updateTask() throws Exception {
        final Task taskToUpdate = taskRepository.findAll().get(0);
        long taskToUpdateId = taskToUpdate.getId();
        final String authorEmail = taskToUpdate.getAuthor().getEmail();

        final long newStatusId = taskStatusRepository.findAll().get(1).getId();
        final long newExecutorId = userRepository.findAll().get(1).getId();
        final long newLabelId = labelRepository.findAll().get(1).getId();

        TaskDto testTaskDto = new TaskDto(
                "New name",
                "New description",
                newStatusId,
                newExecutorId,
                Set.of(newLabelId)
        );

        MockHttpServletRequestBuilder updateRequest = put(BASE_URL + TASK_CONTROLLER_PATH + ID,
                taskToUpdateId)
                .content(asJson(testTaskDto))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, authorEmail).andExpect(status().isOk());
        assertThat(taskRepository.existsById(taskToUpdateId)).isTrue();
        assertThat(taskRepository.findByName(taskToUpdate.getName())).isEmpty();
        assertThat(taskRepository.findByName(testTaskDto.getName())).isPresent();
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
