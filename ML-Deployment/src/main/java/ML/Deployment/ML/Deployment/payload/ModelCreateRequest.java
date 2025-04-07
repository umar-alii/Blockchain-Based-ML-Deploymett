package ML.Deployment.ML.Deployment.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModelCreateRequest {

    @NotBlank(message = "Model name cannot be blank")
    private String modelName;

    private String description; // Optional

    @NotBlank(message = "Algorithm description cannot be blank")
    private String algorithm;

    @NotBlank(message = "Input parameters description cannot be blank")
    private String inputParametersDescription;

    @NotNull(message = "Public/private status must be specified")
    private Boolean isPublic;
}