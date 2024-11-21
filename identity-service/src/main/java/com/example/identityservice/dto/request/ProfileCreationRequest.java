package com.example.identityservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
//public class ProfileCreationRequest {
//    String username;
//    String identityNumber;
//    String phoneNumber;
//    String fullName;
//}

public class ProfileCreationRequest {
    String username;
    String firstName;
    String lastName;
    String city;
    LocalDate dob;
}
