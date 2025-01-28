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
    @Column(name = "comment_id")
    @GeneratedValue
    UUID commentId;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(name = "reply_level")
    Integer replyLevel;

    @Column(name = "number_of_likes")
    Long numberOfLikes;

    @CreationTimestamp
    Instant created;

    @Column(name = "last_modified")
    @UpdateTimestamp
    Instant lastModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    Topic topic;

    @JoinColumn(name = "user_id")
    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    Comment parentComment;

    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY, orphanRemoval = true)
    List<Comment> comments;

    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY)
    List<CommentReport> commentReports;
}
