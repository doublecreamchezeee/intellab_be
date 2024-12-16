package com.example.problemservice.model.composite;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class testCaseOutputId implements Serializable {
    UUID submission_id;
    UUID testcase_id;

    // Override equals and hashCode for proper comparison in composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        testCaseOutputId that = (testCaseOutputId) o;
        return Objects.equals(submission_id, that.submission_id) &&
                Objects.equals(testcase_id, that.testcase_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submission_id, testcase_id);
    }
}
