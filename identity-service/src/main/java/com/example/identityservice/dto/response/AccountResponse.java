package com.example.identityservice.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class AccountResponse {
    String id;
    String username;
    String password;
    Set<RoleResponse> roles;
}
