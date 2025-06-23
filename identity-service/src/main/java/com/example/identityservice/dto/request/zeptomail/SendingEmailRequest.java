package com.example.identityservice.dto.request.zeptomail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SendingEmailRequest {
    From from;
    List<To> to;
    String subject;
    String htmlbody;

    // Inner classes for nested objects
    @NoArgsConstructor
    @AllArgsConstructor
    public static class From {
        private String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class To {
        private EmailAddress email_address;

        public EmailAddress getEmail_address() {
            return email_address;
        }

        public void setEmail_address(EmailAddress email_address) {
            this.email_address = email_address;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAddress {
        private String address;
        private String name;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
