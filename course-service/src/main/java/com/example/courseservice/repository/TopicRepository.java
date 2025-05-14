package com.example.courseservice.repository;

import com.example.courseservice.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
}
