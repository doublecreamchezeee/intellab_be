package com.example.courseservice.model;



import com.example.courseservice.model.compositeKey.assignmentDetailID;
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
    assignmentDetailID assignmentDetail_id;

    @Column(columnDefinition = "DECIMAL(4,2)")
    Float unit_score;

    // câu trả lời có thể là single choice hoặc multi-choice
    // có thêm ràng buộc cho câu trả lời
    @Column(columnDefinition = "VARCHAR(20)")
    String answer;

    @ManyToOne
    @JoinColumn(name = "question_id",nullable = false)
    Question question;

    @ManyToOne
    @MapsId("assignment_id")
    @JoinColumn(name = "assignment_id")
    Assignment assignment;

}
