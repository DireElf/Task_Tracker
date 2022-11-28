package hexlet.code.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
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

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Task> taskCriteriaQuery = criteriaBuilder.createQuery(Task.class);
        Root<Task> taskRoot = taskCriteriaQuery.from(Task.class);

        List<Predicate> predicateList = new ArrayList<>();

        if (requestParams.get("taskStatus") != null) {
            TaskStatus taskStatus = taskStatusRepository.findById(
                    Long.parseLong(requestParams.get("taskStatus"))
            ).get();
            Predicate predicateForTaskStatus = criteriaBuilder.equal(
                    taskRoot.get("taskStatus"), taskStatus);
            predicateList.add(predicateForTaskStatus);
        }

        if (requestParams.get("executorId") != null) {
            User executor = userRepository.findById(
                    Long.parseLong(requestParams.get("executorId"))
            ).get();
            Predicate predicateForExecutor = criteriaBuilder.equal(
                    taskRoot.get("executor"), executor);
            predicateList.add(predicateForExecutor);
        }

        if (requestParams.get("authorId") != null) {
            User author = userRepository.findById(
                    Long.parseLong(requestParams.get("authorId"))
            ).get();
            Predicate predicateForAuthor = criteriaBuilder.equal(
                    taskRoot.get("author"), author);
            predicateList.add(predicateForAuthor);
        }

        if (requestParams.get("labels") != null) {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            List<Long> labelIds = Arrays.asList(
                    objectMapper.readValue(requestParams.get("labels"), Long[].class)
            );

            labelIds
                    .forEach(x -> {
                        Label label = labelRepository.getById(x);
                        CriteriaBuilder.In<Label> inClause = criteriaBuilder.in(taskRoot.get("labels"));
                        inClause.value(label);
                        criteriaBuilder.and(inClause);
                    });
        }

        if (requestParams.get("isMyTasks") != null && Boolean.parseBoolean(requestParams.get("isMyTasks"))) {
            Predicate predicateForMyTasks = criteriaBuilder.equal(
                    taskRoot.get("author"), userService.getCurrentUser());
            predicateList.add(predicateForMyTasks);
        }

        if (predicateList.isEmpty()) {
            return taskRepository.findAll();
        }

        Predicate combinedQuery = criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        taskCriteriaQuery.where(combinedQuery);
        return entityManager.createQuery(taskCriteriaQuery).getResultList();
    }
}
