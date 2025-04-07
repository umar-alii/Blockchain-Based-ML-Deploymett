package ML.Deployment.ML.Deployment.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModelInteractRequest {

    @NotNull
    private Object inputData;
}