package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
