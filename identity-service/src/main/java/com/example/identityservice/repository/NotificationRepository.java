package com.example.identityservice.repository;

import com.example.identityservice.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByRecipientIdOrRecipientIdIsNull(UUID recipientId, Pageable pageable);

    List<Notification> findAllByRecipientIdAndMarkAsRead(UUID userId, boolean markAsRead);

    Page<Notification> findAllByRecipientId(UUID userId, Pageable pageable);
}
