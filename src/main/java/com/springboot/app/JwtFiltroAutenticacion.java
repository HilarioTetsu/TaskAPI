package com.springboot.app;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.springboot.app.models.services.UsuarioDetailsService;
import com.springboot.app.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFiltroAutenticacion extends OncePerRequestFilter {

    
    private JwtUtil jwtUtil;

    
    private UsuarioDetailsService usuarioDetailsService;
    
    

    public JwtFiltroAutenticacion(JwtUtil jwtUtil, UsuarioDetailsService usuarioDetailsService) {		
		this.jwtUtil = jwtUtil;
		this.usuarioDetailsService = usuarioDetailsService;
	}



	@Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String headerAuth = request.getHeader("Authorization");

        String username = null;
        String token = null;

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            token = headerAuth.substring(7); 
            try {
                username = jwtUtil.extraerUsuario(token);
            } catch (Exception e) {
                logger.error("Token inválido: " + e.getMessage());
                throw new IllegalArgumentException("Token inválido: ");
            }
        }

      
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(username);

            if (jwtUtil.validarToken(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
