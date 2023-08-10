package dev.tarcisio.minebox.repositories;

import dev.tarcisio.minebox.entities.RefreshToken;
import dev.tarcisio.minebox.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserId(String userId);

    @Modifying
    int deleteByUser(User user);
}