package com.example.courseservice.model;

import com.example.courseservice.model.compositeKey.ReactionID;
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
@Table(name = "\"comment_reactions\"")
public class Reaction {
    @EmbeddedId
    ReactionID reactionID;

    @JsonIgnore
    @MapsId("commentId")
    @ManyToOne(fetch = FetchType.LAZY)
    Comment comment;

    Boolean active = true;
}
