package com.example.identityservice.dto.response;
import com.example.identityservice.dto.response.auth.UserInfoResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardTableResponse {
    UserInfoResponse user;
    Date date;
    Double amount;
    String status;
    String type;

}
