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
    ReportID reportId;

    @Column(columnDefinition = "TEXT")
    String content;

    // Success, Pending, Failed
    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @MapsId("reportOptionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_option_id")
    ReportOption reportOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("destinationId")
    @JoinColumn(name = "destination_id")
    Topic destination;

}
