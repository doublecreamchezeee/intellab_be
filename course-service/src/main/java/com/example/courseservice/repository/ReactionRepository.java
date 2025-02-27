package com.example.courseservice.repository;


import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Reaction;
import com.example.courseservice.model.compositeKey.ReactionID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, ReactionID> {
    Reaction findByReactionID_UserIdAndComment(UUID userId, Comment comment);
}
