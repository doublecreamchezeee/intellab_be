package com.example.courseservice.model;


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


    @CreationTimestamp
    Instant created = Instant.now();

    @Column(name = "last_modified")
    Instant lastModified = Instant.now();

    @Column(name = "number_of_likes")
    Long numberOfLikes = 0L;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    Topic topic;

    @JoinColumn(name = "user_id")
    UUID userId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    Comment parentComment;


    @JsonManagedReference
    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_comment_id")
    Comment repliedComment;

    @JsonIgnore
    @OneToMany(mappedBy = "repliedComment", fetch = FetchType.LAZY, orphanRemoval = true)
    List<Comment> replyingComments = new ArrayList<>();


    @JsonIgnore
    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<CommentReport> commentReports = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "comment", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    List<Reaction> reactions = new ArrayList<>();

}
