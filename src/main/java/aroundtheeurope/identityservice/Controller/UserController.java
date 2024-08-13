package aroundtheeurope.identityservice.Controller;

import aroundtheeurope.identityservice.DTO.*;
import aroundtheeurope.identityservice.Model.User;
import aroundtheeurope.identityservice.Security.JwtTokenUtil;
import aroundtheeurope.identityservice.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${key.auth.lifetime.minutes}")
    private int authKeyLifetime;

    @Value("${key.refresh.lifetime.minutes}")
    private int refreshKeyLifetime;

    @Autowired
    public UserController(
            AuthenticationManager authenticationManager,
            UserService userService,
            JwtTokenUtil jwtTokenUtil
    ) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        if (userService.findByUsername(registrationDto.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        userService.registerUser(registrationDto);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@RequestBody LogInRequest user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User loggedInUser = userService.findByUsername(userDetails.getUsername());
            String accessToken = jwtTokenUtil.generateToken(loggedInUser.getId().toString(), authKeyLifetime * 60L);
            String refreshToken = jwtTokenUtil.generateToken(loggedInUser.getId().toString(), refreshKeyLifetime * 60L);
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refreshUser(@RequestBody RefreshTokenRequest request) {
        if (jwtTokenUtil.validateRefreshToken(request.getRefreshToken())){
            AuthResponse authResponse = jwtTokenUtil.refreshToken(
                    request.getRefreshToken(),
                    request.getExpiration()
            );
            return ResponseEntity.ok(authResponse);
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
}
