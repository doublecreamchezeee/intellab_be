package com.example.identityservice.utility;

import com.google.cloud.Timestamp;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Component
public class DateUtility {

    public Date convert(@NonNull final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    public LocalDateTime convert(@NonNull final Timestamp timestamp) {
        return timestamp.toDate().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}
