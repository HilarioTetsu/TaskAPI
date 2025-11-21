package com.springboot.app.utils;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	
	
	 
	
    private final SecretKey clave;

	

    public JwtUtil(@Value("${app.jwt.secretkey}") String key) {
		
		this.clave = Keys.hmacShaKeyFor(key.getBytes());
	}


	public String generarToken(String subject, Collection<? extends GrantedAuthority> auths) {
        String roles = auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        
        return Jwts.builder()
            .setSubject(subject)
            .claim("roles", roles)            
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora
            .signWith(clave)
            .compact();
    }
	

	public String extraerUsuario(String token) {

		return Jwts.parserBuilder()
				.setSigningKey(clave)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
				
	}
	
	
	public boolean validarToken(String token,String usuarioEsperado) {
		
		try {
			
			Claims claims = Jwts.parserBuilder()
				    .setSigningKey(clave)
				    .build()
				    .parseClaimsJws(token)
				    .getBody();

            
            return usuarioEsperado.equals(claims.getSubject())
                    && claims.getExpiration().after(new Date());
			
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
		
		
	}
	
	public Instant getExpirationToken (String token) {
		
		Claims claims = Jwts.parserBuilder()
			    .setSigningKey(clave)
			    .build()
			    .parseClaimsJws(token)
			    .getBody();
		
		


		return claims.getExpiration().toInstant();
	}
	
}
