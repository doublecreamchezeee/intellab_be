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
@Table(name = "\"report_options\"")
public class ReportOption {
    @Id
    @GeneratedValue
    @Column(name = "report_option_id")
    UUID reportOptionId;

    // có ràng buộc miền giá trị
    @Column(columnDefinition = "VARCHAR(20)")
    String type;

    @Column(name = "report_reason")
    String reportReason;

    @Column(name = "handle_action")
    String handleAction;

    @OneToMany(mappedBy = "reportOption", fetch = FetchType.LAZY)
    List<CommentReport> commentReports;

    @OneToMany(mappedBy = "reportOption", fetch = FetchType.LAZY)
    List<OtherObjectReport> otherObjectReports;
}
