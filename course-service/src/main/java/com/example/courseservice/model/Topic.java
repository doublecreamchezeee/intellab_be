package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"topics\"")
public class Topic {
    @Id
    @GeneratedValue
    UUID topic_id;

    @Lob
    @Column(columnDefinition = "TEXT")
    String content;

    Integer number_of_likes;

    //Ràng buộc miền giá trị dưới DB
    //('Public', 'Unlisted', 'Private')
    String post_reach;


    @JoinColumn(name = "user_id")
    String userUid = null;

    @OneToOne(mappedBy = "topic")
    Course course;

    @OneToOne(mappedBy = "topic")
    Problem problem;

    @OneToMany(mappedBy = "topic")
    List<Comment> comments;

    @OneToMany(mappedBy = "destination")
    List<OtherObjectReport> otherObjectReports;
}
