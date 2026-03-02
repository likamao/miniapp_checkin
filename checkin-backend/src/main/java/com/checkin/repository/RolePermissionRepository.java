package com.checkin.repository;

import com.checkin.entity.Permission;
import com.checkin.entity.Role;
import com.checkin.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRole(Role role);
    List<RolePermission> findByPermission(Permission permission);
}