package com.springboot.app.utils;

import java.util.Arrays;

public enum ProjectRole {
	 OWNER, EDITOR, VIEWER;
	
	public static boolean existeRol(String nombre) {
	    return Arrays.stream(ProjectRole.values())
	                 .anyMatch(r -> r.name().equalsIgnoreCase(nombre));
	}

	 
	 
}
