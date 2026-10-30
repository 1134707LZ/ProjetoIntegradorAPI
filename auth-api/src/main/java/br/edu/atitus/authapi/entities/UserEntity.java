package br.edu.atitus.authapi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_user")
public class UserEntity {
    public static final int ADMIN = 0;
    public static final int COMMON = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    private String phone;
    @Column(nullable = false)
    private String password;
    /** 0 = administrador, 1 = usuário comum */
    @Column(nullable = false)
    private int type = COMMON;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
}
