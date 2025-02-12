package com.example.problemservice.core;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemStructure {
    String problemName;
    String functionName;
    List<DataField> inputStructure;
    List<DataField> outputStructure;
}
