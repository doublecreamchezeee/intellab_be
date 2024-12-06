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
@Table(name = "\"other_object_reports\"")
public class OtherObjectReport {
    @EmbeddedId
    ReportID report_id;

    @Column(columnDefinition = "TEXT")
    String content;

    String status;

    @ManyToOne
    @MapsId("report_option_id")
    @JoinColumn(name = "report_option_id")
    ReportOption report_option;

    @ManyToOne
    @MapsId("destination_id")
    @JoinColumn(name = "destination_id")
    Topic destination;

    @MapsId("owner_id")
    @JoinColumn(name = "owner_id")
    UUID user_id;
}
