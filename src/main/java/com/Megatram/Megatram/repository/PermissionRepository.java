package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Permission;
import com.Megatram.Megatram.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
    public interface PermissionRepository extends JpaRepository<Permission, Long> {

}
