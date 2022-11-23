package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;

import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
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

import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.TEST_EMAIL_1;
import static hexlet.code.utils.TestUtils.TEST_LABEL_1;
import static hexlet.code.utils.TestUtils.TEST_LABEL_2;

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
public class LabelControllerTest {
    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void registration() throws Exception {
        assertThat(labelRepository.count()).isZero();
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_EMAIL_1).andExpect(status().isCreated());
        assertThat(labelRepository.count()).isEqualTo(1);
    }

    @Test
    public void getLabelById() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_EMAIL_1);
        final Label expectedLabel = labelRepository.findAll().get(0);

        final var response = utils.perform(
                        get(BASE_URL + LABEL_CONTROLLER_PATH + ID,
                                expectedLabel.getId()), TEST_EMAIL_1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Label label = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(label.getId()).isEqualTo(expectedLabel.getId());
        assertThat(label.getName()).isEqualTo(expectedLabel.getName());
    }

    @Test
    public void getAllLabels() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_EMAIL_1);

        final var response = utils.perform(
                        get(BASE_URL + LABEL_CONTROLLER_PATH), TEST_EMAIL_1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<Label> taskLabels = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(taskLabels).hasSize(1);
    }

    @Test
    public void twiceRegTheSameLabel() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_EMAIL_1).andExpect(status().isCreated());
        utils.regDefaultLabel(TEST_EMAIL_1).andExpect(status().isUnprocessableEntity());

        assertThat(labelRepository.count()).isEqualTo(1);
    }

    @Test
    public void updateLabel() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_EMAIL_1);
        long labelId = labelRepository.findAll().get(0).getId();
        LabelDto labelDto = new LabelDto(TEST_LABEL_2);

        final var updateRequest =
                put(BASE_URL + LABEL_CONTROLLER_PATH + ID, labelId)
                        .content(asJson(labelDto))
                        .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, TEST_EMAIL_1).andExpect(status().isOk());
        assertThat(labelRepository.existsById(labelId)).isTrue();
        assertThat(labelRepository.findByName(TEST_LABEL_1)).isEmpty();
        assertThat(labelRepository.findByName(TEST_LABEL_2)).isPresent();
    }

    @Test
    public void deleteLabel() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_EMAIL_1);
        final Long labelId = labelRepository.findAll().get(0).getId();

        utils.perform(delete(BASE_URL + LABEL_CONTROLLER_PATH + ID, labelId), TEST_EMAIL_1)
                .andExpect(status().isOk());
        assertThat(labelRepository.count()).isZero();
    }
}
