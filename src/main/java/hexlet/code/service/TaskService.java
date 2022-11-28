package hexlet.code.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;

import java.util.Map;

public interface TaskService {
    Task createTask(TaskDto dto);
    Task updateTask(long id, TaskDto dto);
    Iterable<Task> getFilteredTasks(Map<String, String> params) throws JsonProcessingException;
}
