package com.example.courseservice.dto.response.profile;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleProfileInformationResponse {
    String userId;
    String displayName;
    String email;
    String phoneNumber;
    String photoUrl;
}
