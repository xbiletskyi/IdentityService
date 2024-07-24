package aroundtheeurope.identityservice.Controller;

import aroundtheeurope.identityservice.DTO.LogoutRequest;
import aroundtheeurope.identityservice.DTO.UserRegistrationDto;
import aroundtheeurope.identityservice.DTO.AuthResponse;
import aroundtheeurope.identityservice.DTO.RefreshTokenRequest;
import aroundtheeurope.identityservice.Model.User;
import aroundtheeurope.identityservice.Security.JwtTokenUtil;
import aroundtheeurope.identityservice.Service.UserService;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${key.auth.lifetime.minutes}")
    private int authKeyLifetime;

    @Value("${key.refresh.lifetime.minutes}")
    private int refreshKeyLifetime;

    @Autowired
    public UserController(
            AuthenticationManager authenticationManager,
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtTokenUtil jwtTokenUtil
    ) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        if (userService.findByUsername(registrationDto.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        userService.registerUser(registrationDto);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenUtil.generateToken(userDetails.getUsername(), authKeyLifetime * 60L);
            String refreshToken = jwtTokenUtil.generateToken(userDetails.getUsername(), refreshKeyLifetime * 60L);
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshUser(@RequestBody RefreshTokenRequest request) {
        if (jwtTokenUtil.validateRefreshToken(request.getRefreshToken())){
            AuthResponse authResponse = jwtTokenUtil.refreshToken(
                    request.getRefreshToken(),
                    request.getExpiration()
            );
            return ResponseEntity.ok(authResponse);
        }
        else{
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody LogoutRequest logoutRequest) {
        String refreshToken = logoutRequest.getRefreshToken();
        if (refreshToken != null && jwtTokenUtil.validateRefreshToken(refreshToken)){
            jwtTokenUtil.invalidateRefreshToken(refreshToken);
            return ResponseEntity.ok("User logged out successfully");
        }
        else{
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }
}

