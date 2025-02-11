package com.example.courseservice.model;



import com.example.courseservice.model.compositeKey.AssignmentDetailID;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"assignment_details\"")
public class AssignmentDetail {
    @EmbeddedId
    AssignmentDetailID assignmentDetailId;

    @Column(columnDefinition = "DECIMAL(4,2)", name = "unit_score")
    Float unitScore;

    // câu trả lời có thể là single choice hoặc multi-choice
    // có thêm ràng buộc cho câu trả lời
    @Column(columnDefinition = "VARCHAR(20)")
    String answer;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id",nullable = false)
    Question question;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("assignmentId")
    @JoinColumn(name = "assignment_id")
    Assignment assignment;

}
