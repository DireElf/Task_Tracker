package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
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

import static hexlet.code.controller.TaskStatusController.STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.TEST_EMAIL_1;
import static hexlet.code.utils.TestUtils.TEST_STATUS_1;
import static hexlet.code.utils.TestUtils.TEST_STATUS_2;
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

public class TaskStatusControllerTest {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void registration() throws Exception {
        assertThat(taskStatusRepository.count()).isZero();
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_EMAIL_1).andExpect(status().isCreated());
        assertThat(taskStatusRepository.count()).isEqualTo(1);
    }

    @Test
    public void getStatusById() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_EMAIL_1);
        final TaskStatus expectedTaskStatus = taskStatusRepository.findAll().get(0);

        final var response = utils.perform(
                        get(BASE_URL + STATUS_CONTROLLER_PATH + ID,
                                expectedTaskStatus.getId()), TEST_EMAIL_1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final TaskStatus taskStatus = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(taskStatus.getId()).isEqualTo(expectedTaskStatus.getId());
        assertThat(taskStatus.getName()).isEqualTo(expectedTaskStatus.getName());
    }

    @Test
    public void getAllStatuses() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_EMAIL_1);

        final var response = utils.perform(
                get(BASE_URL + STATUS_CONTROLLER_PATH), TEST_EMAIL_1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<TaskStatus> taskStatuses = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(taskStatuses).hasSize(1);
    }

    @Test
    public void twiceRegTheSameStatusFail() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_EMAIL_1).andExpect(status().isCreated());
        utils.regDefaultStatus(TEST_EMAIL_1).andExpect(status().isUnprocessableEntity());

        assertThat(taskStatusRepository.count()).isEqualTo(1);
    }

    @Test
    public void updateStatus() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_EMAIL_1);
        long statusId = taskStatusRepository.findAll().get(0).getId();
        TaskStatusDto statusDto = new TaskStatusDto(TEST_STATUS_2);

        final var updateRequest =
                put(BASE_URL + STATUS_CONTROLLER_PATH + ID, statusId)
                .content(asJson(statusDto))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, TEST_EMAIL_1).andExpect(status().isOk());
        assertThat(taskStatusRepository.existsById(statusId)).isTrue();
        assertThat(taskStatusRepository.findByName(TEST_STATUS_1)).isEmpty();
        assertThat(taskStatusRepository.findByName(TEST_STATUS_2)).isPresent();
    }

    @Test
    public void deleteStatus() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_EMAIL_1);
        final Long statusId = taskStatusRepository.findAll().get(0).getId();

        utils.perform(delete(BASE_URL + STATUS_CONTROLLER_PATH + ID, statusId), TEST_EMAIL_1)
                .andExpect(status().isOk());
        assertThat(taskStatusRepository.count()).isZero();
    }
}
