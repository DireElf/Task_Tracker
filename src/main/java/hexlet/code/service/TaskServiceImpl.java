package hexlet.code.service;

import hexlet.code.dto.TaskDto;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    @Override
    public Task createTask(TaskDto dto) {
        final Task task = new Task();
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setTaskStatus(taskStatusRepository.findById(dto.getTaskStatusId()).get());
        task.setAuthor(userService.getCurrentUser());
        task.setExecutor(userRepository.findById(dto.getExecutorId()).get());
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(long id, TaskDto dto) {
        final Task taskToUpdate = taskRepository.findById(id).get();
        taskToUpdate.setName(dto.getName());
        taskToUpdate.setDescription(dto.getDescription());
        taskToUpdate.setTaskStatus(taskStatusRepository.findById(dto.getTaskStatusId()).get());
        taskToUpdate.setAuthor(userService.getCurrentUser());
        taskToUpdate.setExecutor(userRepository.findById(dto.getExecutorId()).get());
        if (dto.getLabelIds() != null) {
            taskToUpdate.setLabels(getLabelsByIds(dto));
        }
        return taskRepository.save(taskToUpdate);
    }

    private List<Label> getLabelsByIds(TaskDto dto) {
        return dto.getLabelIds().stream()
                .map(id -> labelRepository.findById(id).get())
                .toList();
    }
}
