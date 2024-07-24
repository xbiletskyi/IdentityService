package aroundtheeurope.identityservice.DTO;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    public AuthResponse(
            String accessToken,
            String refreshToken
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
