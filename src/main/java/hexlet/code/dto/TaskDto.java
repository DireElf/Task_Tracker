package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskDto {
    @NotBlank
    @Size(min = 3, max = 1000)
    private String name;

    @Nullable
    private String description;

    @NotNull
    private Long taskStatusId;

    @Nullable
    private Long executorId;

    @Nullable
    private Set<Long> labelIds;
}
