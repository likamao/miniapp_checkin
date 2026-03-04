package com.checkin.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "openid", unique = true, nullable = false, length = 100)
    private String openid;

    @Column(name = "unionid", length = 100)
    private String unionid;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "allow_stats_display", nullable = false)
    private Boolean allowStatsDisplay = false;

    @Column(name = "profile_setup_completed", nullable = false)
    private Boolean profileSetupCompleted = false;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getAllowStatsDisplay() {
        return allowStatsDisplay;
    }

    public void setAllowStatsDisplay(Boolean allowStatsDisplay) {
        this.allowStatsDisplay = allowStatsDisplay;
    }

    public Boolean getProfileSetupCompleted() {
        return profileSetupCompleted;
    }

    public void setProfileSetupCompleted(Boolean profileSetupCompleted) {
        this.profileSetupCompleted = profileSetupCompleted;
    }
}