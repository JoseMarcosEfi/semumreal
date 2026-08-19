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
    private String googleSubject;

    public static User create(String name, String email, String password){
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.role=Role.USER;
        return user;
    }

    public static User createFromGoogle(String name, String email, String googleSubject) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.linkGoogleSubject(googleSubject);
        user.role = Role.USER;
        return user;
    }

    public static User reconstitute(Long id, String name, String email, String password, Role role){
        return reconstitute(id, name, email, password, role, null);
    }

    public static User reconstitute(
            Long id,
            String name,
            String email,
            String password,
            Role role,
            String googleSubject) {
        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        user.password = password;
        user.role = role;
        user.googleSubject = googleSubject;
        return user;
    }

    public void linkGoogleSubject(String googleSubject) {
        if (googleSubject == null || googleSubject.isBlank()) {
            throw new IllegalArgumentException("Google subject is required");
        }
        if (this.googleSubject != null && !this.googleSubject.equals(googleSubject)) {
            throw new IllegalStateException("User is already linked to another Google account");
        }
        this.googleSubject = googleSubject;
    }

    //setters
    public void setName(String name){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name;
    }
    
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase();
    }

    public void setEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (!normalizedEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Email is invalid");
        }
        this.email = normalizedEmail;
    }

    public void changePasswordHash(String passowordHash){
        if(passowordHash == null || passowordHash.isBlank()){
            throw new IllegalArgumentException("Password hash is required");
        }
        this.password = passowordHash;
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

    public void promoteToAdmin(){
        if(isAdmin()){
            throw new IllegalStateException("User is already an admin");
        }
        this.role = Role.ADMIN;
    }
    public void demoteFromAdmin(){
        if(!isAdmin()){
            throw new IllegalStateException("User is not an admin");
        }
        this.role = Role.USER;
    }

}
