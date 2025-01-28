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
public class TestCaseOutputID implements Serializable {
    UUID submissionId;
    UUID testcaseId;

    // Override equals and hashCode for proper comparison in composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseOutputID that = (TestCaseOutputID) o;
        return Objects.equals(submissionId, that.submissionId) &&
                Objects.equals(testcaseId, that.testcaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submissionId, testcaseId);
    }
}
