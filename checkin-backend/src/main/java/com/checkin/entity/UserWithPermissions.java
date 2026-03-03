package com.checkin.entity;

import java.util.List;

/**
 * 用户信息与权限信息的组合模型
 */
public class UserWithPermissions {
    private User user;
    private List<String> roles;
    private List<String> permissions;

    public UserWithPermissions(User user, List<String> roles, List<String> permissions) {
        this.user = user;
        this.roles = roles;
        this.permissions = permissions;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
