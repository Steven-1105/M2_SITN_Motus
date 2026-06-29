package com.motus.player.dto;

import com.motus.player.entity.Player;
import com.motus.player.entity.Role;

public class PlayerResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;

    public PlayerResponse() {
    }

    public PlayerResponse(Long id, String username, String email, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public static PlayerResponse fromEntity(Player player) {
        return new PlayerResponse(player.getId(), player.getUsername(), player.getEmail(), player.getRole());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
