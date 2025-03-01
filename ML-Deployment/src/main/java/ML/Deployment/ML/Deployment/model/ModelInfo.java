package ML.Deployment.ML.Deployment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "models")
public class ModelInfo {

    @Id
    private String id;

    @NotBlank
    @Indexed // Index for faster lookup by owner
    private String ownerId; // Reference to User.id

    @NotBlank
    private String modelName;

    private String description;

    @NotBlank
    private String algorithm;

    @NotBlank // Store as JSON string or similar structured text
    private String inputParametersDescription; // Describes needed input params

    @NotBlank
    private String ipfsCid; // IPFS Content Identifier

    @NotBlank
    private String originalFilename; // Original name of the uploaded file

    @NotNull
    private Boolean isPublic = false; // Default to private

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public ModelInfo(String ownerId, String modelName, String description, String algorithm, String inputParametersDescription, String ipfsCid, String originalFilename, Boolean isPublic) {
        this.ownerId = ownerId;
        this.modelName = modelName;
        this.description = description;
        this.algorithm = algorithm;
        this.inputParametersDescription = inputParametersDescription;
        this.ipfsCid = ipfsCid;
        this.originalFilename = originalFilename;
        this.isPublic = isPublic != null ? isPublic : false; // Ensure non-null, default false
    }

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

    public void setIsPublic(Boolean aPublic) {
        isPublic = aPublic;

    }
}