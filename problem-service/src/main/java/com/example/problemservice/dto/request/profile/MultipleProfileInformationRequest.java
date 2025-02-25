package com.example.problemservice.dto.request.profile;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleProfileInformationRequest {
    List<String> userIds;
}
