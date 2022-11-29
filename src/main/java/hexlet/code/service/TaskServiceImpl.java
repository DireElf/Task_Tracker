package hexlet.code.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Label;
import hexlet.code.model.QTask;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Task createTask(TaskDto dto) {
        final Task task = new Task();
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setTaskStatus(taskStatusRepository.findById(dto.getTaskStatusId()).get());
        task.setAuthor(userService.getCurrentUser());
        task.setExecutor(userRepository.findById(dto.getExecutorId()).get());
        if (dto.getLabelIds() != null) {
            task.setLabels(getLabelsByIds(dto));
        }
        return taskRepository.save(task);
    }

    private Set<Label> getLabelsByIds(TaskDto dto) {
        return dto.getLabelIds().stream()
                .map(id -> labelRepository.findById(id).get())
                .collect(Collectors.toSet());
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

    @Override
    public Iterable<Task> getFilteredTasks(Map<String, String> requestParams) throws JsonProcessingException {

        QTask task = QTask.task;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);
        JPAQuery<Task> query = factory.selectFrom(task);

        if (requestParams.get("taskStatus") != null) {
            long taskStatusId = Long.parseLong(requestParams.get("taskStatus"));
            booleanBuilder.and(task.taskStatus.id.eq(taskStatusId));
        }

        if (requestParams.get("executorId") != null) {
            long executorId = Long.parseLong(requestParams.get("executorId"));
            booleanBuilder.and(task.executor.id.eq(executorId));
        }

        if (requestParams.get("authorId") != null) {
            long authorId = Long.parseLong(requestParams.get("authorId"));
            booleanBuilder.and(task.author.id.eq(authorId));
        }

        if (requestParams.get("labels") != null) {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            List<Long> labelIds = Arrays.asList(
                    objectMapper.readValue(requestParams.get("labels"), Long[].class)
            );
            booleanBuilder.and(task.labels.any().id.in(labelIds));
        }

        if (requestParams.get("isMyTasks") != null && requestParams.get("isMyTasks").equals("true")) {
            long currentUserId = userService.getCurrentUser().getId();
            booleanBuilder.and(task.author.id.eq(currentUserId));
        }

        if (!booleanBuilder.hasValue()) {
            return taskRepository.findAll();
        }

        return query
                .where(booleanBuilder)
                .fetch();
    }
}
