package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "tasks")
public class Task extends BaseEntity {

    @NotBlank
    @Column(unique = true)
    @Size(min = 3, max = 1000)
    private String name;

    private String description;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "task_status_id", foreignKey = @ForeignKey(name = "FK_TASKS_TASK_STATUSES_ID_COL"))
    private TaskStatus taskStatus;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "FK_TASKS_AUTHORS_ID_COL"))
    private User author;

    @ManyToOne
    @JoinColumn(name = "executor_id", foreignKey = @ForeignKey(name = "FK_TASKS_EXECUTORS_ID_COL"))
    private User executor;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "label_id", foreignKey = @ForeignKey(name = "FK_TASK_LABELS_LABELS_ID_COL"))
    private Set<Label> labels;
}
