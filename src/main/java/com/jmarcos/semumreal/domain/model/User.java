package com.jmarcos.semumreal.domain.model;

import com.jmarcos.semumreal.domain.enums.Role;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;

    public static User create(String name, String email, String password){
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.role=Role.USER;
        return user;
    }
    //setters
    public void setName(String name){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name;
    }
    public void setEmail(String email){
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("Email is required");
        }
        if(!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")){
            throw new IllegalArgumentException("Email is invalid");
        }
        this.email = email;
    }
    public void setPassword(String password){
        if(password == null || password.isBlank()){
            throw new IllegalArgumentException("Password is required");
        }
        if(password.length() < 8){
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        this.password = password;
    }
    //metodos pra role
    public boolean isAdmin(){
        return role == Role.ADMIN;
    }
    public boolean canManageUsers(){
        return isAdmin();
    }

}
