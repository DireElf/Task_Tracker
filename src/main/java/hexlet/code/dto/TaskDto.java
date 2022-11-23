package hexlet.code.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskDto {
    @NotBlank
    @Size(min = 3, max = 1000)
    private String name;

    private String description;

    @NotNull
    private Long taskStatusId;

    private Long executorId;

    private List<Long> labelIds;
}
