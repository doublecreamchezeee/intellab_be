package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    String content;
    Integer number_of_likes;

    //Ràng buộc miền giá trị dưới DB
    //('Public', 'Unlisted', 'Private')
    String post_reach;


    @JoinColumn(name = "user_id", nullable = false)
    String userUid = null;

    @OneToOne(mappedBy = "topic")
    Course course;

}
