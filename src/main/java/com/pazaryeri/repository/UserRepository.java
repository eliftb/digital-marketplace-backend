package com.pazaryeri.repository;

import com.pazaryeri.entity.User;
import com.pazaryeri.enums.AccountStatus;
import com.pazaryeri.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndStatus(UserRole role, AccountStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:status IS NULL OR u.status = :status)")
    Page<User> searchUsers(@Param("search") String search,
                           @Param("role") UserRole role,
                           @Param("status") AccountStatus status,
                           Pageable pageable);

    long countByRole(UserRole role);
    long countByStatus(AccountStatus status);
}
