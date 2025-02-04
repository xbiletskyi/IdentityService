package aroundtheeurope.identityservice.model.dto;

public class RefreshResponse {
    private String accessToken;
    private String refreshToken;

    public RefreshResponse() {}

    public RefreshResponse(
            String accessToken,
            String refreshToken
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters and setters

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
