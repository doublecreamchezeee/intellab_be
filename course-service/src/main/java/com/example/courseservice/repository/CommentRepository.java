package com.example.courseservice.repository;


import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByTopicAndParentCommentIsNull(Topic topic, Pageable pageable);

    Page<Comment> findByParentComment(Comment comment, Pageable childrenPageable);
}
