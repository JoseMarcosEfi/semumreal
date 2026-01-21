package com.jmarcos.semumreal.adapter.out.persistence.entity;

import com.jmarcos.semumreal.domain.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column( nullable = false, length = 120)
    private String name;
    @Column( nullable = false, unique = true, length = 255)
    private String email;
    @Column( nullable = false, length = 255)
    private String password;
    @Column( nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Role role;
}
