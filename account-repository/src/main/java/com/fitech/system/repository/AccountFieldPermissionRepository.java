package com.fitech.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fitech.domain.system.Role;

public interface AccountFieldPermissionRepository extends JpaRepository<Role, Long>{

	/**
     * 根据角色Id查询所有角色信息
     * @param roleId
     * @return
     */
    @Query(value = "select distinct r from Role r where r.id=:roleId")
    public Role findById(@Param("roleId") Long roleId);
}
