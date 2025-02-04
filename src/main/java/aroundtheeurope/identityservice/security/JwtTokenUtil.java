package aroundtheeurope.identityservice.security;

import aroundtheeurope.identityservice.model.dto.AuthResponse;
import aroundtheeurope.identityservice.model.dto.RefreshRequest;
import aroundtheeurope.identityservice.model.dto.RefreshResponse;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    private final Set<String> refreshTokenBlacklist = new HashSet<>();

    public String generateToken(String userId, long expirationTime) {
        try {
            JWSSigner signer = new MACSigner(secret.getBytes());
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .expirationTime(new Date(new Date().getTime() + expirationTime))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            return claimsSet.getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing JWT", e);
        }
    }

    public boolean validateRefreshToken(String token) {
        if (refreshTokenBlacklist.contains(token)) {
            return false;
        }
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret.getBytes());
            return signedJWT.verify(verifier);
        } catch (JOSEException | ParseException e) {
            return false;
        }
    }

    public RefreshResponse refreshToken(String refreshToken, long expiration) {
        if (validateRefreshToken(refreshToken)) {
            String userId = getUserIdFromToken(refreshToken);
            String newAccessToken = generateToken(userId, expiration);
            return new RefreshResponse(newAccessToken, refreshToken);
        } else {
            return null;
        }
    }

    public void invalidateRefreshToken(String refreshToken) {
        refreshTokenBlacklist.add(refreshToken);
    }
}
