package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
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

import static hexlet.code.controller.TaskStatusController.STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.TEST_STATUS_1;
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
public final class TaskStatusControllerTest {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TestUtils utils;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registration() throws Exception {
        final long entriesAmountBefore = taskStatusRepository.count();
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        utils.regDefaultStatus(existingUserEmail).andExpect(status().isCreated());

        assertThat(taskStatusRepository.count()).isEqualTo(entriesAmountBefore + 1);
    }

    @Test
    void getStatusById() throws Exception {
        final long existingStatusId = taskStatusRepository.findAll().get(0).getId();
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();
        final TaskStatus expectedTaskStatus = taskStatusRepository.findById(existingStatusId).get();

        MockHttpServletResponse response = utils.perform(
                        get(BASE_URL + STATUS_CONTROLLER_PATH + ID,
                                expectedTaskStatus.getId()), existingUserEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        TaskStatus taskStatus = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(taskStatus.getId()).isEqualTo(expectedTaskStatus.getId());
        assertThat(taskStatus.getName()).isEqualTo(expectedTaskStatus.getName());
    }

    @Test
    void getAllStatuses() throws Exception {
        final long expectedEntriesAmount = taskStatusRepository.count();

        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        MockHttpServletResponse response = utils.perform(
                get(BASE_URL + STATUS_CONTROLLER_PATH), existingUserEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<TaskStatus> taskStatuses = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) taskStatuses.size()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    void twiceRegTheSameStatusFail() throws Exception {
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        utils.regDefaultStatus(existingUserEmail).andExpect(status().isCreated());

        final long expectedEntriesAmount = taskStatusRepository.count();

        utils.regDefaultStatus(existingUserEmail).andExpect(status().isUnprocessableEntity());

        assertThat(taskStatusRepository.count()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    void updateStatus() throws Exception {
        final long existingStatusId = taskStatusRepository.findAll().get(0).getId();
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        TaskStatusDto statusDto = new TaskStatusDto(TEST_STATUS_1);

        MockHttpServletRequestBuilder updateRequest =
                put(BASE_URL + STATUS_CONTROLLER_PATH + ID, existingStatusId)
                .content(asJson(statusDto))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, existingUserEmail).andExpect(status().isOk());
        assertThat(taskStatusRepository.findById(existingStatusId).get().getName())
                .isEqualTo(TEST_STATUS_1);
    }

    @Test
    void deleteStatus() throws Exception {
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();
        utils.regDefaultStatus(existingUserEmail);
        final long entriesAmountBefore = taskStatusRepository.count();

        long taskStatusId = taskStatusRepository.findByName(TEST_STATUS_1).get().getId();

        utils.perform(delete(BASE_URL + STATUS_CONTROLLER_PATH + ID, taskStatusId), existingUserEmail)
                .andExpect(status().isOk());
        assertThat(taskStatusRepository.count()).isEqualTo(entriesAmountBefore - 1);
    }

    @Test
    void deleteAssignedStatus() throws Exception {
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();
        final long entriesAmountBefore = taskStatusRepository.count();
        long existingAssignedStatusId = 1;

        utils.perform(delete(BASE_URL + STATUS_CONTROLLER_PATH + ID, existingAssignedStatusId), existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        assertThat(taskStatusRepository.count()).isEqualTo(entriesAmountBefore);
    }

    @Test
    void createByUnauthorized() throws Exception {
        final long entriesAmountBefore = taskStatusRepository.count();

        MockHttpServletRequestBuilder request = post(BASE_URL + STATUS_CONTROLLER_PATH)
                .content(asJson(utils.getTaskStatusDto()))
                .contentType(APPLICATION_JSON);

        try {
            utils.perform(request);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Unauthorized");
        }
        assertThat(taskStatusRepository.count()).isEqualTo(entriesAmountBefore);
    }

    @Test
    void editByUnauthorized() throws Exception {
        TaskStatus statusToUpdate = taskStatusRepository.findAll().get(0);
        final String statusNameBefore = statusToUpdate.getName();
        final long existingStatusId = statusToUpdate.getId();

        TaskStatusDto statusDto = new TaskStatusDto(TEST_STATUS_1);

        MockHttpServletRequestBuilder updateRequest =
                put(BASE_URL + STATUS_CONTROLLER_PATH + ID, existingStatusId)
                        .content(asJson(statusDto))
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
        TaskStatus statusToUpdate = taskStatusRepository.findAll().get(0);
        final long existingStatusId = statusToUpdate.getId();

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
