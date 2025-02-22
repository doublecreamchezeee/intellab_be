package com.example.problemservice.dto.request.profile;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleProfileInformationRequest {
    String userId;
}
