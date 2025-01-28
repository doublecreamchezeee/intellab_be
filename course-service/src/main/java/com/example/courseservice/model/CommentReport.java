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
    ReportID reportId;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @MapsId("reportOptionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_option_id")
    ReportOption reportOption;

    @MapsId("destinationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    Comment destination;

    @MapsId("ownerId")
    @JoinColumn(name = "owner_id")
    UUID userId;

}
