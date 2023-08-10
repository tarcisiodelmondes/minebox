package dev.tarcisio.minebox.services;

import dev.tarcisio.minebox.entities.RefreshToken;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.TokenRefreshException;
import dev.tarcisio.minebox.payload.response.TokenRefreshResponse;
import dev.tarcisio.minebox.repositories.RefreshTokenRepository;
import dev.tarcisio.minebox.repositories.UserRepository;
import dev.tarcisio.minebox.security.jwt.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(String userId) {
        RefreshToken refreshToken = new RefreshToken();

        User user = userRepository.findById(userId).get();

        Optional<RefreshToken> isRefreshTokenExists = refreshTokenRepository.findByUserId(user.getId());

        if (isRefreshTokenExists.isPresent()) {
            deleteByUserId(user.getId());
        }

        refreshTokenRepository.flush();


        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(
                Instant.now().plusMillis(refreshTokenExpiration));


        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(
                    token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    public TokenRefreshResponse refreshToken(String refreshToken) {
        var isRefreshTokenExists = findByToken(refreshToken);

        if (!isRefreshTokenExists.isPresent()) {
            throw new TokenRefreshException(refreshToken, "Refresh token is not in database!");
        }

        var refreshTokenData = isRefreshTokenExists.get();
        verifyExpiration(refreshTokenData);

        String newToken = jwtUtils.generateTokenFromUsername(refreshTokenData.getUser().getEmail());
        return new TokenRefreshResponse(newToken, refreshToken);
    }

    @Transactional
    public int deleteByUserId(String userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}