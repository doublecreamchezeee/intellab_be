package com.example.identityservice.model;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class User {
    String uid;
    String firstName;
    String lastName;
    String role;
    Boolean isPublic;
}
