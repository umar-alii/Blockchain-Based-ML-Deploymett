package ML.Deployment.ML.Deployment.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "models")
@Data
public class Model {

    @Id
    private String id;
    private String name;
    private String description;
    private String filename;
    private String storagePath;
    private String contentType;
    private long size;
    private Instant uploadTimestamp;
    private boolean isPublic = true;
    private String ownerId;


    public Model() {

    }

    public Model(String id, String name, String description, String filename, String storagePath, String contentType, long size, Instant uploadTimestamp, boolean isPublic, String ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.filename = filename;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.size = size;
        this.uploadTimestamp = uploadTimestamp;
        this.isPublic = isPublic;
        this.ownerId = ownerId;
    }

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

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public Instant getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(Instant uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

}