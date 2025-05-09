package ML.Deployment.ML.Deployment.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"password"})
@ToString(exclude = {"password"})
@Document(collection = "users")
public class User {


    @Id
    private String id;

    @NotNull
    private String name;

    @Email
    @Indexed(unique = true)
    private String email;

    public String getImageUrl() {
        return imageUrl;

    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public @NotEmpty Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }

    private String imageUrl;

    private Boolean emailVerified = false;

    @JsonIgnore
    private String password;

    @NotNull
    private AuthProvider provider;

    private String providerId;

    @NotEmpty
    @Field("roles")
    private Set<Role> roles = new HashSet<>();
    public User() {
        // Default constructor
    }


    private boolean profileComplete = false;


    public User(String name, String email, String imageUrl, AuthProvider provider, String providerId) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.provider = provider;
        this.providerId = providerId;
        this.emailVerified = true;
        this.roles.add(Role.ROLE_USER);
        this.profileComplete = false;
    }


    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password; // Will be encoded later
        this.provider = AuthProvider.local;
        this.roles.add(Role.ROLE_USER);
        this.profileComplete = false; // Requires explicit update
    }

}
