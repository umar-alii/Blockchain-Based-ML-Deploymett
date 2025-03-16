package ML.Deployment.ML.Deployment.payload;

public class ApiResponse {
    private boolean success;
    private String message;

    // Default constructor
    public ApiResponse() {
    }

    // Constructor with parameters
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}