package ML.Deployment.ML.Deployment.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelInteractResponse {
    private Object result;
    private String message;
}