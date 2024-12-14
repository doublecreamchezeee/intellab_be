package com.example.courseservice.model;


import com.example.courseservice.model.compositeKey.OptionID;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"options\"")
public class Option {
    @EmbeddedId
    OptionID option_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("question_id")
    @JoinColumn(name = "question_id", nullable = false)
    Question question;

    String content;
}


//-- Tạo bảng Options
//CREATE TABLE Options (
//      Question_ID VARCHAR(255),
//      Option_Order INT,
//      Content TEXT,
//      PRIMARY KEY (Question_ID, Option_Order),
//      CONSTRAINT FK_OPTIONS_QUESTIONS FOREIGN KEY (Question_ID) REFERENCES Questions(Question_ID)
//);
