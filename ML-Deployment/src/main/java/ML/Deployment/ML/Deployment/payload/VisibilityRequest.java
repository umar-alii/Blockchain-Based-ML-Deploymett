package ML.Deployment.ML.Deployment.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VisibilityRequest {
    @NotNull(message = "Public status cannot be null")
    private Boolean isPublic;
}