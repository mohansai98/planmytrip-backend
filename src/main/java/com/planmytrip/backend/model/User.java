package com.planmytrip.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String email;
    private String name;
    @Column(nullable = false)
    private String password;
    private boolean enabled;
    @Transient
    private int code;

}
