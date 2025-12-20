package com.springboot.app.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;

public class Utils {

	
	  public static String extensionFromName(String name) {
		    int i = name.lastIndexOf('.');
		    return (i > 0) ? name.substring(i + 1) : "bin";
		  }
	  
	    public static int convertirAHorasMinutosASegundos(int horas, int minutos) {
	        int segundos = (horas * 3600) + (minutos * 60);
	        return segundos;
	    }
	  
		public static Sort parseSortParams(String sorts) {
			
			String[] arraySorts=sorts.split(";");
			
			List<Sort.Order> orders= new ArrayList<>();
			
			
			Sort sort = Sort.unsorted();
			
			if (arraySorts.length==0) {
				return sort;
			}
			
			for (int i = 0; i < arraySorts.length; i++) {
				
				String[] parts = arraySorts[i].split(",");
				
				if (parts.length != 2) { 
					
					throw new IllegalArgumentException( "Formato invalido en parámetro de ordenamiento. Se espera 'campo,direccion;'" ); 
					
				}
				
				String campo=parts[0];
					
				Sort.Direction dir = parts.length>1 && parts[1].equalsIgnoreCase("desc")? Sort.Direction.DESC : Sort.Direction.ASC;
				
				orders.add(new Sort.Order(dir, campo));
				
			}
			
			sort=Sort.by(orders);
			
			return sort;
		}
	  
	
}
