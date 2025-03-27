package ML.Deployment.ML.Deployment.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AdminUserDto {
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private String imageUrl;

    private Boolean emailVerified;

    // Changed from Set<Role> to Set<String> to match controller implementation
    private Set<String> roles = new HashSet<>();

    private boolean profileComplete;
    
    // Added missing fields for model information
    private Integer modelCount;
    private List<AdminModelDto> models;

    public AdminUserDto(){}
    
    public AdminUserDto(String id, String name, String email, String imageUrl, Boolean emailVerified, Set<String> roles, boolean profileComplete) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.emailVerified = emailVerified;
        this.roles = roles;
        this.profileComplete = profileComplete;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }
    
    public Integer getModelCount() {
        return modelCount;
    }
    
    public void setModelCount(Integer modelCount) {
        this.modelCount = modelCount;
    }
    
    public List<AdminModelDto> getModels() {
        return models;
    }
    
    public void setModels(List<AdminModelDto> models) {
        this.models = models;
    }
}
