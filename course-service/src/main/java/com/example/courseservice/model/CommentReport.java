package com.example.courseservice.model;

import com.example.courseservice.model.compositeKey.ReportID;
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
@Table(name = "\"comment_reports\"")
public class CommentReport {
    @EmbeddedId
    ReportID report_id;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @ManyToOne
    @MapsId("report_option_id")
    @JoinColumn(name = "report_option_id")
    ReportOption report_option;

    @ManyToOne
    @MapsId("destination_id")
    @JoinColumn(name = "destination_id")
    Comment destination;

    @MapsId("owner_id")
    @JoinColumn(name = "owner_id")
    UUID user_id;

}
