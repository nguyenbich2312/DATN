package com.bzone.repository;

import com.bzone.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Set<Notification> findByUser_Id(Long id);

    Notification findById(long id);
}