package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"comments\"")
public class Comment {
    @Id
    @GeneratedValue
    UUID comment_id;

    @Column(columnDefinition = "TEXT")
    String content;

    Integer reply_level;
    Long number_of_likes;

    @CreationTimestamp
    Instant created;

    @UpdateTimestamp
    Instant last_modified;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    Topic topic;

    @JoinColumn(name = "owner_id")
    UUID user_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    Comment parent_comment;

    @OneToMany(mappedBy = "parent_comment", fetch = FetchType.LAZY, orphanRemoval = true)
    List<Comment> comments;

    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY)
    List<CommentReport> comment_reports;

}
