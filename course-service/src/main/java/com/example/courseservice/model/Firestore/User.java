package com.example.courseservice.model.Firestore;

import lombok.Getter;
import lombok.Setter;

import java.rmi.server.UID;


@Setter
@Getter
public class User {
    UID userId;
    String firstName;
    String lastName;
    String role;
}
