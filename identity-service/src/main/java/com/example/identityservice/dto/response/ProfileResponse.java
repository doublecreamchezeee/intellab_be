package com.example.identityservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
//public class ProfileResponse {
//    String id;
//    String identityNumber;
//    String phoneNumber;
//    String fullName;
//    String username;
////    List<Account> accounts;
//}

public class ProfileResponse {
    String id;
    String userId;
    String firstName;
    String lastName;
    String city;
    LocalDate dob;
}
