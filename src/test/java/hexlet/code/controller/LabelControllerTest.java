package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
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

import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
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
public class LabelControllerTest {

    private final LabelDto sampleLabel = new LabelDto("Sample label");

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TestUtils utils;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void registration() throws Exception {
        final long entriesAmountBefore = labelRepository.count();
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        utils.regEntity(sampleLabel, existingUserEmail, LABEL_CONTROLLER_PATH)
                .andExpect(status().isCreated());

        assertThat(labelRepository.count()).isEqualTo(entriesAmountBefore + 1);
    }

    @Test
    public void getLabelById() throws Exception {
        final long existingLabelId = labelRepository.findAll().get(0).getId();
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();
        final Label expectedLabel = labelRepository.findById(existingLabelId).get();

        MockHttpServletResponse response = utils.perform(
                        get(BASE_URL + LABEL_CONTROLLER_PATH + ID,
                                expectedLabel.getId()), existingUserEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        Label label = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(label.getId()).isEqualTo(expectedLabel.getId());
        assertThat(label.getName()).isEqualTo(expectedLabel.getName());
    }

    @Test
    public void getAllLabels() throws Exception {
        final long expectedEntriesAmount = labelRepository.count();

        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        MockHttpServletResponse response = utils.perform(
                        get(BASE_URL + LABEL_CONTROLLER_PATH), existingUserEmail)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        List<Label> labels = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat((long) labels.size()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    public void twiceRegTheSameLabel() throws Exception {
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        utils.regEntity(sampleLabel, existingUserEmail, LABEL_CONTROLLER_PATH)
                .andExpect(status().isCreated());

        final long expectedEntriesAmount = labelRepository.count();

        utils.regEntity(sampleLabel, existingUserEmail, LABEL_CONTROLLER_PATH)
                .andExpect(status().isUnprocessableEntity());

        assertThat(labelRepository.count()).isEqualTo(expectedEntriesAmount);
    }

    @Test
    public void updateLabel() throws Exception {
        final long existingLabelId = labelRepository.findAll().get(0).getId();
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();

        MockHttpServletRequestBuilder updateRequest =
                put(BASE_URL + LABEL_CONTROLLER_PATH + ID, existingLabelId)
                        .content(asJson(sampleLabel))
                        .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, existingUserEmail).andExpect(status().isOk());
        assertThat(labelRepository.findById(existingLabelId).get().getName())
                .isEqualTo(sampleLabel.getName());
    }

    @Test
    public void deleteAssignedLabel() throws Exception {
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();
        final long entriesAmountBefore = labelRepository.count();

        long assignedLabelId = labelRepository.findAll().get(0).getId();

        utils.perform(delete(BASE_URL + LABEL_CONTROLLER_PATH + ID, assignedLabelId), existingUserEmail)
                .andExpect(status().isUnprocessableEntity());

        assertThat(labelRepository.count()).isEqualTo(entriesAmountBefore);
    }

    @Test
    public void deleteLabel() throws Exception {
        final String existingUserEmail = userRepository.findAll().get(0).getEmail();
        utils.regEntity(sampleLabel, existingUserEmail, LABEL_CONTROLLER_PATH)
                .andExpect(status().isCreated());

        final long entriesAmountBefore = labelRepository.count();

        long labelId = labelRepository.findByName(sampleLabel.getName()).get().getId();

        utils.perform(delete(BASE_URL + LABEL_CONTROLLER_PATH + ID, labelId), existingUserEmail)
                .andExpect(status().isOk());

        assertThat(labelRepository.count()).isEqualTo(entriesAmountBefore - 1);
    }
}
