package com.bzone.repository;

import com.bzone.model.Role;
import com.bzone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);

    @Query(value = "select * from user where email like %?1%", nativeQuery = true)
    List<User> findAllByEmail(String email);

    @Query(value = "select * from user where email like %?1% and role_id=?2", nativeQuery = true)
    List<User> findAllByEmailAndRoleId(String email, int id);
    User findByUsernameOrEmail(String username, String email);

    List<User> findAllByRole(Role role);

    User findById(long id);
}
