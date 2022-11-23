package hexlet.code.controller;

import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@RestController
@RequestMapping("${base-url}" + TaskController.TASK_CONTROLLER_PATH)
public class TaskController {

    public static final String TASK_CONTROLLER_PATH = "/tasks";
    public static final String ID = "/{id}";

    private final TaskService taskService;
    private final TaskRepository taskRepository;

    private static final String ONLY_OWNER_BY_ID =
            "@taskRepository.findById(#id).get().getAuthor().getEmail() == authentication.getName()";

    @GetMapping(ID)
    public Optional<Task> getTask(@PathVariable long id) throws NoSuchElementException {
        return taskRepository.findById(id);
    }

    @GetMapping("")
    public List<Task> getAllTasks() throws Exception {
        return taskRepository.findAll();
    }

    @PostMapping("")
    @ResponseStatus(CREATED)
    public Task createTask(@RequestBody @Valid TaskDto taskDto) {
        return taskService.createTask(taskDto);
    }

    @PutMapping(ID)
    @PreAuthorize(ONLY_OWNER_BY_ID)
    public Task updateTask(@PathVariable long id, @RequestBody @Valid TaskDto dto) {
        return taskService.updateTask(id, dto);
    }

    @DeleteMapping(ID)
    @PreAuthorize(ONLY_OWNER_BY_ID)
    public void deleteTask(@PathVariable long id) {
        taskRepository.deleteById(id);
    }
}
