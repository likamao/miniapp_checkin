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

    // 直接获取用户的个人资料设置完成状态
    public Boolean getProfileSetupCompleted() {
        return user.getProfileSetupCompleted();
    }

    // 直接获取用户的昵称
    public String getNickname() {
        return user.getNickname();
    }

    // 直接获取用户的ID
    public Long getId() {
        return user.getId();
    }

    // 直接获取用户的创建时间
    public java.util.Date getCreatedAt() {
        return user.getCreatedAt();
    }

    // 直接获取用户的更新时间
    public java.util.Date getUpdatedAt() {
        return user.getUpdatedAt();
    }

    // 直接获取用户的隐私设置
    public Boolean getAllowStatsDisplay() {
        return user.getAllowStatsDisplay();
    }
}