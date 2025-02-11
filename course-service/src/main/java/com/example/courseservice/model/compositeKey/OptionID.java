package com.example.courseservice.model.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class OptionID implements Serializable {
    @Column(name = "question_id")
    UUID questionId;
    @Column(name = "option_order")
    Integer optionOrder;

    public OptionID(UUID questionId, int optionOrder) {
        this.questionId = questionId;
        this.optionOrder = optionOrder;
    }
}
