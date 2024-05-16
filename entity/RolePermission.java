package com.optum.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "rx_role_permission")
public class RolePermission {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rp_rid")
    private int rolePermissionRid;

	 @Column(name = "r_rid")
	    private Integer RoleId;
	 
	 @Column(name = "p_rid")
	    private Integer permissionId;
    
    @Column(name = "rp_created_by")
    private Integer createdBy;

    @Column(name = "rp_modified_by")
    private Integer modifiedBy;

    @Column(name = "rp_create_datetime")
    private Date createdDate;

    @Column(name = "rp_modify_datetime")
    private Date modifiedDate;

	public int getId() {
		return rolePermissionRid;
	}

	public void setId(int id) {
		this.rolePermissionRid = id;
	}
	

	public Integer getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Integer modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public int getRolePermissionRid() {
		return rolePermissionRid;
	}

	public void setRolePermissionRid(int rolePermissionRid) {
		this.rolePermissionRid = rolePermissionRid;
	}

	public Integer getRoleId() {
		return RoleId;
	}

	public void setRoleId(Integer roleId) {
		RoleId = roleId;
	}

	public Integer getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Integer permissionId) {
		this.permissionId = permissionId;
	}

    

}
