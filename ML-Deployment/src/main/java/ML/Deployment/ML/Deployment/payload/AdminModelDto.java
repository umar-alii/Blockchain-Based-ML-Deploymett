package ML.Deployment.ML.Deployment.payload; // Ensure this package exists


import java.time.Instant;


public class AdminModelDto {
    private String id;
    private String name;
    private String description;
    private String filename;
    private String contentType;
    private long size;
    private Instant uploadTimestamp;
    private boolean isPublic;
    private String ownerId;
    private String ownerEmail;

    public AdminModelDto() {

    }

    public AdminModelDto(String id, String name, String description, String filename, String contentType, long size, Instant uploadTimestamp, boolean isPublic, String ownerId, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.uploadTimestamp = uploadTimestamp;
        this.isPublic = isPublic;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
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

    public Instant getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(Instant uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
    // Added for convenience
}