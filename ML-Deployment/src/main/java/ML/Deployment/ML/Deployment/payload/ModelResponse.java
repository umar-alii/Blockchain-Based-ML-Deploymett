package ML.Deployment.ML.Deployment.payload;

import ML.Deployment.ML.Deployment.model.ModelInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// DTO to control what model information is sent back to the client
@Data
@NoArgsConstructor
public class ModelResponse {
    private String id;
    private String ownerId;
    private String modelName;
    private String description;
    private String algorithm;
    private String inputParametersDescription;
    private String ipfsCid;
    private String originalFilename;
    private Boolean isPublic;
    private Instant createdAt;
    private Instant updatedAt;

    // Manually added getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getInputParametersDescription() {
        return inputParametersDescription;
    }

    public void setInputParametersDescription(String inputParametersDescription) {
        this.inputParametersDescription = inputParametersDescription;
    }

    public String getIpfsCid() {
        return ipfsCid;
    }

    public void setIpfsCid(String ipfsCid) {
        this.ipfsCid = ipfsCid;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static ModelResponse fromModelInfo(ModelInfo modelInfo) {
        ModelResponse dto = new ModelResponse();
        dto.setId(modelInfo.getId());
        dto.setOwnerId(modelInfo.getOwnerId());
        dto.setModelName(modelInfo.getModelName());
        dto.setDescription(modelInfo.getDescription());
        dto.setAlgorithm(modelInfo.getAlgorithm());
        dto.setInputParametersDescription(modelInfo.getInputParametersDescription());
        dto.setIpfsCid(modelInfo.getIpfsCid());
        dto.setOriginalFilename(modelInfo.getOriginalFilename());
        dto.setIsPublic(modelInfo.getIsPublic());
        dto.setCreatedAt(modelInfo.getCreatedAt());
        dto.setUpdatedAt(modelInfo.getUpdatedAt());
        return dto;
    }
}