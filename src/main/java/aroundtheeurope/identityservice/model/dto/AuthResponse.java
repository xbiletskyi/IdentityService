package aroundtheeurope.identityservice.model.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String avatarUrl;

    public AuthResponse(
            String accessToken,
            String refreshToken,
            String avatarUrl
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.avatarUrl = avatarUrl;
    }

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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
