package aroundtheeurope.identityservice.Security;

import aroundtheeurope.identityservice.DTO.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    private final Set<String> refreshTokenBlacklist = new HashSet<>();

    public String generateToken(String username, long expirationTime) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateRefreshToken(String token) {
        if (refreshTokenBlacklist.contains(token)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public AuthResponse refreshToken(String refreshToken, long expiration){
        if (validateRefreshToken(refreshToken)){
            String username = getUsernameFromToken(refreshToken);
            String newAccessToken = generateToken(username, expiration);
            return new AuthResponse(newAccessToken, refreshToken);
        }
        else{
            return null;
        }
    }

    public void invalidateRefreshToken(String refreshToken) {
        refreshTokenBlacklist.add(refreshToken);
    }
}
