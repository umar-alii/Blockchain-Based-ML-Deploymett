package ML.Deployment.ML.Deployment.payload;


import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer"; //default value


    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }


    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

}