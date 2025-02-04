package aroundtheeurope.identityservice.service;

import aroundtheeurope.identityservice.model.dto.AuthResponse;
import aroundtheeurope.identityservice.model.dto.LogInRequest;
import aroundtheeurope.identityservice.model.entity.User;
import aroundtheeurope.identityservice.repository.UserRepository;
import aroundtheeurope.identityservice.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    public AuthResponse loginUser(LogInRequest loginRequest, int authKeyLifetime, int refreshKeyLifetime) {
        try {
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User loggedUser = userRepository.findByUsername(userDetails.getUsername());
            if (loggedUser == null) {
                throw new BadCredentialsException("User not found");
            }

            String accessToken = jwtTokenUtil.generateToken(
                    loggedUser.getId().toString(),
                    authKeyLifetime * 60L
            );
            String refreshToken = jwtTokenUtil.generateToken(
                    loggedUser.getId().toString(),
                    refreshKeyLifetime * 60L
            );

            return new AuthResponse(accessToken, refreshToken, loggedUser.getAvatarUrl());
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
