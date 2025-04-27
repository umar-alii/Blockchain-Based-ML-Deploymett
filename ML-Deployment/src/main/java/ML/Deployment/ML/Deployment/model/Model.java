package ML.Deployment.ML.Deployment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "models")
public class Model {
    @Id
    private String id;
    private String name;
    private String description;
    private String filename;
    private String originalFilename;
    private String contentType;
    private long size;
    private Instant uploadTimestamp;
    private boolean isPublic;
    private String ownerId;
    private String ownerUsername;
    private String deploymentPlatform;
    private String metadata;
    private String predictionParametersJson;
    private String endpointUrl;
    private String spaceId;

    // Default constructor
    public Model() {
    }

    // Constructor with essential fields
    public Model(String name, String description, String filename, String originalFilename,
                 String contentType, long size, String ownerId, String ownerUsername, boolean isPublic) {
        this.name = name;
        this.description = description;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.isPublic = isPublic;
        this.uploadTimestamp = Instant.now();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Instant getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(Instant uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getDeploymentPlatform() {
        return deploymentPlatform;
    }

    public void setDeploymentPlatform(String deploymentPlatform) {
        this.deploymentPlatform = deploymentPlatform;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getPredictionParametersJson() {
        return predictionParametersJson;
    }

    public void setPredictionParametersJson(String predictionParametersJson) {
        this.predictionParametersJson = predictionParametersJson;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
}