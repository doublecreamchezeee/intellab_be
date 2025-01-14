package com.example.courseservice.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

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
    @Column(name = "topic_id")
    UUID topicId;

    @Column(name = "title")
    String title;

    @Lob
    @Column(columnDefinition = "TEXT", name = "content")
    String content;

    @Column(name = "number_of_likes")
    Integer number_of_likes;

    //Ràng buộc miền giá trị dưới DB
    //('Public', 'Unlisted', 'Private')
    @Column(columnDefinition = "VARCHAR(10)")
    String post_reach;

    @JoinColumn(name = "user_id")
    String userUid = null;

    @OneToOne(mappedBy = "topic", fetch = FetchType.LAZY, optional = true)
    @JsonIgnore
    Course course;

//    @OneToOne(mappedBy = "topic", fetch = FetchType.LAZY)
//    Problem problem;

    @JsonIgnore
    @OneToMany(mappedBy = "topic", fetch = FetchType.LAZY)
    List<Comment> comments;

    @JsonIgnore
    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY)
    List<OtherObjectReport> otherObjectReports;
}
