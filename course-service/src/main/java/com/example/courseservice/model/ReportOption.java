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
    UUID report_option_id;

    // có ràng buộc miền giá trị
    @Column(columnDefinition = "VARCHAR(20)")
    String type;


    String report_reason;
    String handle_action;

    @OneToMany(mappedBy = "report_option")
    List<CommentReport> comment_reports;

    @OneToMany(mappedBy = "report_option")
    List<OtherObjectReport> other_object_reports;
}
