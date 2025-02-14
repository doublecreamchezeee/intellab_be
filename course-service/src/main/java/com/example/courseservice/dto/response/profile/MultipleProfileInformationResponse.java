package com.example.courseservice.dto.response.profile;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultipleProfileInformationResponse {
    List<SingleProfileInformationResponse> profiles;
}
