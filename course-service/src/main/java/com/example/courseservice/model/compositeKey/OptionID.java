package com.example.courseservice.model.compositeKey;

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

    UUID question_id;
    Integer option_order;

    public OptionID(UUID question_id, int Option_Order) {
        this.question_id = question_id;
        this.option_order = Option_Order;
    }
}
