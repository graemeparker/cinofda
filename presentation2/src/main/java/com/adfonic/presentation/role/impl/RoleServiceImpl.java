package com.adfonic.presentation.role.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Role;
import com.adfonic.presentation.role.RoleService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.service.UserManager;

@Service("roleService")
public class RoleServiceImpl extends GenericServiceImpl implements RoleService {
	
	@Autowired
	private UserManager userManager;
	
	public Role getRoleDto(String name) {
		Role rol = userManager.getRoleByName(name);
		return rol;
	}
	
}
