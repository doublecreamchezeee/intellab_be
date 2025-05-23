package com.example.problemservice.model;

import com.example.problemservice.model.composite.ProblemCommentReactionId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"problem_comment_reactions\"")
public class ProblemCommentReaction {
    @EmbeddedId
    ProblemCommentReactionId reactionId;

    Boolean isActive = true;

    @JsonIgnore
    @MapsId("commentId")
    @ManyToOne(fetch = FetchType.LAZY)
    ProblemComment  problemComment;
}
