package hexlet.code.service;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {
    private final TaskStatusRepository taskStatusRepository;

    @Override
    public TaskStatus createStatus(TaskStatusDto dto) {
        final TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(dto.getName());
        return taskStatusRepository.save(taskStatus);
    }

    @Override
    public TaskStatus updateStatus(long id, TaskStatusDto dto) {
        final TaskStatus taskStatusToUpdate = taskStatusRepository.findById(id).get();
        taskStatusToUpdate.setName(dto.getName());
        return taskStatusRepository.save(taskStatusToUpdate);
    }
}
