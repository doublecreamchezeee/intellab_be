package com.example.courseservice.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name ="\"Cart\"")
public class Cart {
    @Id
    @Column(name = "cart_id")
    @GeneratedValue
    UUID cartId;

    @Column(name = "user_uuid")
    UUID userUuid;

    @Column(name = "user_uid")
    String userUid;

}
