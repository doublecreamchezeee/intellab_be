package com.example.problemservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"problem_comments\"")
public class ProblemComment {
    @Id
    @Column(name = "comment_id")
    @GeneratedValue
    UUID commentId;

    @Column(columnDefinition = "TEXT")
    String content;

    /*@Column(name = "reply_level")
    Integer replyLevel;
    @Column(name = "number_of_react")
    Long numberOfReact;*/

    @Column(name = "is_modified")
    Boolean isModified;

    @Column(name = "number_of_likes")
    Integer numberOfLikes;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @Column(name = "last_modified_at")
    @UpdateTimestamp
    Instant lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    Problem problem;

    @JoinColumn(name = "user_uuid", nullable = false)
    UUID userUuid; // PostgreSQL UUID

    @Column(name = "user_uid", columnDefinition = "VARCHAR", nullable = false)
    String userUid; // Firebase UID

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    ProblemComment parentComment;

    @JsonManagedReference
    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY, orphanRemoval = true)
    List<ProblemComment> childrenComments  = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_comment_id")
    ProblemComment repliedComment;

    @JsonIgnore
    @OneToMany(mappedBy = "repliedComment", fetch = FetchType.LAZY, orphanRemoval = true)
    List<ProblemComment> replyingComments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "problemComment", fetch = FetchType.LAZY, orphanRemoval = true)
    List<ProblemCommentReaction> reactions = new ArrayList<>();

}
