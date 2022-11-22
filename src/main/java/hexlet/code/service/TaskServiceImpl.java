package hexlet.code.service;

import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;

    @Override
    public Task createTask(final TaskDto taskDto) {
        final Task task = new Task();
        task.setName(taskDto.getName());
        task.setDescription(taskDto.getDescription());
        task.setTaskStatus(taskStatusRepository.findById(taskDto.getTaskStatusId()).get());
        task.setAuthor(userService.getCurrentUser());
        task.setExecutor(userRepository.findById(taskDto.getExecutorId()).get());
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(final long id, final TaskDto taskDto) {
        final Task taskToUpdate = taskRepository.findById(id).get();
        taskToUpdate.setName(taskDto.getName());
        taskToUpdate.setDescription(taskDto.getDescription());
        taskToUpdate.setTaskStatus(taskStatusRepository.findById(taskDto.getTaskStatusId()).get());
        taskToUpdate.setAuthor(userService.getCurrentUser());
        taskToUpdate.setExecutor(userRepository.findById(taskDto.getExecutorId()).get());
        return taskRepository.save(taskToUpdate);
    }
}
