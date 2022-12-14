package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.TaskStatus;
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

import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.controller.TaskStatusController.STATUS_CONTROLLER_PATH;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(SpringConfigForIT.TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
public final class TaskStatusControllerTest {

    private final TaskStatusDto sampleTaskStatus = new TaskStatusDto("Sample status");
    private final TaskStatusDto anotherTaskStatus = new TaskStatusDto("Another status");
    private final UserDto sampleUserDto = UserControllerTest.getSampleUserDto();
    private static String existingUserEmail;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TestUtils utils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @BeforeEach
    public void initialization() throws Exception {
        utils.setUp();
        utils.regEntity(sampleUserDto, USER_CONTROLLER_PATH).andExpect(status().isCreated());
        existingUserEmail = userRepository.findAll().get(0).getEmail();
    }

    @Test
    void registration() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH)
                .andExpect(status().isCreated());
        assertThat(taskStatusRepository.count()).isEqualTo(1);
    }

    @Test
    void getStatusById() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        TaskStatus expectedStatus = taskStatusRepository.findAll().get(0);
        long expectedStatusId = expectedStatus.getId();

        MockHttpServletResponse response = utils.perform(
                        get(BASE_URL + STATUS_CONTROLLER_PATH + ID, expectedStatusId), existingUserEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        TaskStatus actualStatus = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(actualStatus.getId()).isEqualTo(expectedStatusId);
        assertThat(actualStatus.getName()).isEqualTo(expectedStatus.getName());
    }

    @Test
    void getAllStatuses() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        utils.regEntity(anotherTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        long expectedCount = taskStatusRepository.count();

        MockHttpServletResponse response = utils.perform(
                        get(BASE_URL + STATUS_CONTROLLER_PATH), existingUserEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<TaskStatus> statuses = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) statuses.size()).isEqualTo(expectedCount);
    }

    @Test
    void twiceRegTheSameStatusFail() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        long expectedCount = taskStatusRepository.count();
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH)
                .andExpect(status().isUnprocessableEntity());
        assertThat(taskStatusRepository.count()).isEqualTo(expectedCount);
    }

    @Test
    void updateStatus() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        long statusToUpdateId = taskStatusRepository.findAll().get(0).getId();

        MockHttpServletRequestBuilder updateRequest =
                put(BASE_URL + STATUS_CONTROLLER_PATH + ID, statusToUpdateId)
                        .content(asJson(anotherTaskStatus))
                        .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, existingUserEmail).andExpect(status().isOk());
        assertThat(taskStatusRepository.findById(statusToUpdateId).get().getName())
                .isEqualTo(anotherTaskStatus.getName());
    }

    @Test
    void deleteStatus() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        final long expectedCount = taskStatusRepository.count();
        long statusToDeleteId = taskStatusRepository.findAll().get(0).getId();

        utils.perform(delete(BASE_URL + STATUS_CONTROLLER_PATH + ID, statusToDeleteId), existingUserEmail)
                .andExpect(status().isOk());

        assertThat(taskStatusRepository.count()).isEqualTo(expectedCount - 1);
    }

    @Test
    void deleteAssignedStatus() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        long expectedCount = taskStatusRepository.count();
        long assignedStatusId = taskStatusRepository.findAll().get(0).getId();
        utils.regEntity(new LabelDto("Sample label"), existingUserEmail, LABEL_CONTROLLER_PATH);
        TaskDto taskDto = new TaskDto(
                "Task name",
                "Task description",
                assignedStatusId,
                userRepository.findAll().get(0).getId(),
                Set.of(labelRepository.findAll().get(0).getId())
        );
        utils.regEntity(taskDto, existingUserEmail, TASK_CONTROLLER_PATH);

        utils.perform(delete(BASE_URL + STATUS_CONTROLLER_PATH + ID, assignedStatusId), existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        assertThat(taskStatusRepository.count()).isEqualTo(expectedCount);
    }

    @Test
    void createByUnauthorized() throws Exception {
        long countBefore = taskStatusRepository.count();
        try {
            utils.regEntity(anotherTaskStatus, STATUS_CONTROLLER_PATH);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Unauthorized");
        }
        assertThat(taskStatusRepository.count()).isEqualTo(countBefore);
    }

    @Test
    void editByUnauthorized() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        TaskStatus statusToUpdate = taskStatusRepository.findAll().get(0);
        String statusNameBefore = statusToUpdate.getName();
        long existingStatusId = statusToUpdate.getId();

        MockHttpServletRequestBuilder updateRequest =
                put(BASE_URL + STATUS_CONTROLLER_PATH + ID, existingStatusId)
                        .content(asJson(anotherTaskStatus))
                        .contentType(APPLICATION_JSON);

        try {
            utils.perform(updateRequest);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Unauthorized");
        }
        assertThat(taskStatusRepository.findById(existingStatusId).get().getName())
                .isEqualTo(statusNameBefore);
    }

    @Test
    void deleteByUnauthorized() throws Exception {
        utils.regEntity(sampleTaskStatus, existingUserEmail, STATUS_CONTROLLER_PATH);
        TaskStatus statusToUpdate = taskStatusRepository.findAll().get(0);
        long existingStatusId = statusToUpdate.getId();

        MockHttpServletRequestBuilder deleteRequest =
                delete(BASE_URL + STATUS_CONTROLLER_PATH + ID, existingStatusId);

        try {
            utils.perform(deleteRequest);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Unauthorized");
        }
        assertThat(taskStatusRepository.findById(existingStatusId)).isPresent();
    }
}
