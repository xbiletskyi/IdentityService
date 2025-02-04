package aroundtheeurope.identityservice.controller;

import aroundtheeurope.identityservice.model.dto.*;
import aroundtheeurope.identityservice.security.JwtTokenUtil;
import aroundtheeurope.identityservice.service.AuthService;
import aroundtheeurope.identityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/")
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthService authService;

    @Value("${key.auth.lifetime.minutes}")
    private int authKeyLifetime;

    @Value("${key.refresh.lifetime.minutes}")
    private int refreshKeyLifetime;

    @Autowired
    public UserController(
            UserService userService,
            JwtTokenUtil jwtTokenUtil,
            AuthService authService) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authService = authService;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest registrationDto) {
        if (userService.findByUsername(registrationDto.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        userService.registerUser(registrationDto);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@RequestBody LogInRequest logInRequest) {
        try {
            AuthResponse authResponse = authService.loginUser(logInRequest, authKeyLifetime, refreshKeyLifetime);
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refreshUser(@RequestBody RefreshRequest request) {
        if (jwtTokenUtil.validateRefreshToken(request.getRefreshToken())){
            RefreshResponse refreshResponse = jwtTokenUtil.refreshToken(
                    request.getRefreshToken(),
                    request.getExpiration()
            );
            return ResponseEntity.ok(refreshResponse);
        } else {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logoutUser(@RequestBody LogoutRequest logoutRequest) {
        String refreshToken = logoutRequest.getRefreshToken();
        if (refreshToken != null && jwtTokenUtil.validateRefreshToken(refreshToken)){
            jwtTokenUtil.invalidateRefreshToken(refreshToken);
            return ResponseEntity.ok("User logged out successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }

    @DeleteMapping(value = "/users/{id}/avatar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAvatar(@PathVariable UUID id){
        try {
            userService.deleteUserAvatar(id);
            return ResponseEntity.ok("Avatar deleted successfully");
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("Failed to delete avatar: " + e.getMessage());
        }
    }

    @GetMapping(value = "/users/{id}/avatar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAvatar(@PathVariable UUID id){
        try{
            String avatarUrl = userService.getUserAvatar(id);
            return ResponseEntity.ok(avatarUrl);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("Failed to load avatar: " + e.getMessage());
        }
    }

    @PostMapping(value = "/users/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@PathVariable UUID id, @RequestParam("file") MultipartFile file){
        try{
            String avatarUrl = userService.uploadUserAvatar(file, id);
            return ResponseEntity.ok(avatarUrl);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload avatar: " + e.getMessage());
        }
    }
}
