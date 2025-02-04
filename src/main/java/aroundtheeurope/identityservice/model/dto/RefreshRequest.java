package aroundtheeurope.identityservice.model.dto;

public class RefreshRequest {

    private String refreshToken;
    private long expiration;

    public RefreshRequest(
            String refreshToken,
            long expiration
    ) {
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }

    // Getters and setters

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
