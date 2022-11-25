package hexlet.code.controller;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static hexlet.code.controller.TaskStatusController.STATUS_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@RestController
@RequestMapping("${base-url}" + STATUS_CONTROLLER_PATH)
public class TaskStatusController {

    public static final String STATUS_CONTROLLER_PATH = "/statuses";
    public static final String ID = "/{id}";

    private final TaskStatusService taskStatusService;
    private final TaskStatusRepository taskStatusRepository;

    @Operation(summary = "Get status")
    @GetMapping(ID)
    public Optional<TaskStatus> getStatus(@PathVariable long id) throws NoSuchElementException {
        return taskStatusRepository.findById(id);
    }

    @Operation(summary = "Get all statuses")
    @ApiResponses(@ApiResponse(responseCode = "200", content =
        @Content(schema =
        @Schema(implementation = TaskStatus.class))
        ))
    @GetMapping("")
    public List<TaskStatus> getAllStatuses() throws Exception {
        return taskStatusRepository.findAll()
                .stream()
                .toList();
    }

    @Operation(summary = "Create new task status")
    @ApiResponse(responseCode = "201", description = "Task status created")
    @PostMapping("")
    @ResponseStatus(CREATED)
    public TaskStatus createStatus(@RequestBody @Valid TaskStatusDto taskStatusDto) {
        return taskStatusService.createStatus(taskStatusDto);
    }

    @Operation(summary = "Update task status")
    @PutMapping(ID)
    public TaskStatus updateStatus(@PathVariable long id, @RequestBody @Valid TaskStatusDto taskStatusDto) {
        return taskStatusService.updateStatus(id, taskStatusDto);
    }

    @Operation(summary = "Delete task status")
    @DeleteMapping(ID)
    public void deleteStatus(@PathVariable long id) {
        taskStatusRepository.deleteById(id);
    }
}
