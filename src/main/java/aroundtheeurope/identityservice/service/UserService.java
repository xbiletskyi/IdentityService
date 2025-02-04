package aroundtheeurope.identityservice.service;

import aroundtheeurope.identityservice.model.dto.AuthResponse;
import aroundtheeurope.identityservice.model.dto.LogInRequest;
import aroundtheeurope.identityservice.model.dto.RegistrationRequest;
import aroundtheeurope.identityservice.model.entity.User;
import aroundtheeurope.identityservice.repository.UserRepository;
import aroundtheeurope.identityservice.security.JwtTokenUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Transient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AzureBlobStorageService azureBlobStorageService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AzureBlobStorageService azureBlobStorageService,
            JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.azureBlobStorageService = azureBlobStorageService;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void registerUser(RegistrationRequest registrationDto) {
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());
        user.setFullName(registrationDto.getFullName());

        user.setAvatarUrl(null);
        user.setLastLogin(null);
        user.setDeletedAt(null);
        user.setIsDeleted(false);

        userRepository.save(user);
    }

    public String uploadUserAvatar(MultipartFile file, UUID userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String avatarUrl = azureBlobStorageService.uploadFile(file);

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    @Transactional
    public void deleteUserAvatar(UUID userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getAvatarUrl() != null) {
            azureBlobStorageService.deleteFile(user.getAvatarUrl());

            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }

    public String getUserAvatar(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getAvatarUrl();
    }
}
