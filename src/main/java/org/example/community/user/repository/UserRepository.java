package org.example.community.user.repository;

import org.example.community.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    void updateProfile(Long userId, String nickname, String profileImage);

    void updatePassword(Long userId, String encodedPassword);

    void deleteById(Long userId);
}