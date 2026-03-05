package com.library_management.library_management_artifact.service;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.request.ChangePasswordRequest;
import com.library_management.library_management_artifact.dto.request.CreateUserRequest;
import com.library_management.library_management_artifact.dto.request.LoginRequest;
import com.library_management.library_management_artifact.dto.request.RegisterRequest;
import com.library_management.library_management_artifact.dto.request.ResendVerificationRequest;
import com.library_management.library_management_artifact.dto.response.AuthResponse;
import com.library_management.library_management_artifact.dto.response.RegisterResponse;
import com.library_management.library_management_artifact.dto.response.UserResponse;
import com.library_management.library_management_artifact.entity.EmailVerificationToken;
import com.library_management.library_management_artifact.entity.RefreshToken;
import com.library_management.library_management_artifact.entity.Role;
import com.library_management.library_management_artifact.entity.User;
import com.library_management.library_management_artifact.exception.BadRequestException;
import com.library_management.library_management_artifact.exception.EmailNotVerifiedException;
import com.library_management.library_management_artifact.exception.ForbiddenException;
import com.library_management.library_management_artifact.exception.InvalidRefreshTokenException;
import com.library_management.library_management_artifact.exception.ResourceNotFoundException;
import com.library_management.library_management_artifact.mapper.UserMapper;
import com.library_management.library_management_artifact.repository.EmailVerificationTokenRepository;
import com.library_management.library_management_artifact.repository.RefreshTokenRepository;
import com.library_management.library_management_artifact.repository.UserRepository;
import com.library_management.library_management_artifact.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Value("${app.email-verification.expiry-minutes}")
    private long verificationExpiryMinutes;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    // Called by JwtAuthenticationFilter on every authenticated request
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(ApiMessage.EMAIL_ALREADY_EXISTS.getMessage());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.MEMBER);
        userRepository.save(user);

        issueVerificationToken(user);

        return RegisterResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken evt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException(
                        ApiMessage.INVALID_VERIFICATION_TOKEN.getMessage()));

        if (evt.isExpired()) {
            throw new BadRequestException(ApiMessage.INVALID_VERIFICATION_TOKEN.getMessage());
        }

        User user = evt.getUser();
        if (user.isVerified()) {
            throw new BadRequestException(ApiMessage.EMAIL_ALREADY_VERIFIED.getMessage());
        }

        user.setVerified(true);
        userRepository.save(user);
        verificationTokenRepository.delete(evt);
    }

    @Transactional
    public void resendVerificationEmail(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException(ApiMessage.INVALID_CREDENTIALS.getMessage()));

        if (user.isVerified()) {
            throw new BadRequestException(ApiMessage.EMAIL_ALREADY_VERIFIED.getMessage());
        }

        verificationTokenRepository.deleteByUser(user);
        issueVerificationToken(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new ForbiddenException(ApiMessage.ACCOUNT_DISABLED.getMessage());
        }

        if (!user.isVerified()) {
            throw new EmailNotVerifiedException(ApiMessage.EMAIL_NOT_VERIFIED.getMessage());
        }

        refreshTokenRepository.deleteByUser(user);
        String rawRefreshToken = createRefreshToken(user);
        String accessToken = jwtUtils.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiryMs())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException(
                        ApiMessage.INVALID_REFRESH_TOKEN.getMessage()));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new InvalidRefreshTokenException(ApiMessage.INVALID_REFRESH_TOKEN.getMessage());
        }

        User user = stored.getUser();

        // Rotate: delete old, issue new refresh token
        refreshTokenRepository.delete(stored);
        String newRawRefreshToken = createRefreshToken(user);
        String newAccessToken = jwtUtils.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiryMs())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByToken(rawRefreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    public UserResponse me(User user) {
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException(ApiMessage.INVALID_CURRENT_PASSWORD.getMessage());
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ApiMessage.NOT_FOUND.getMessage()));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(ApiMessage.EMAIL_ALREADY_EXISTS.getMessage());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .build();
        userRepository.save(user);
        issueVerificationToken(user);

        return userMapper.toResponse(user);
    }


    private void issueVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken evt = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(verificationExpiryMinutes))
                .build();
        verificationTokenRepository.save(evt);
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);
    }

    private String createRefreshToken(User user) {
        String raw = UUID.randomUUID().toString();
        RefreshToken rt = RefreshToken.builder()
                .token(raw)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();
        refreshTokenRepository.save(rt);
        return raw;
    }
}