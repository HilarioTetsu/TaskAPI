package com.springboot.app;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.springboot.app.models.services.UsuarioDetailsService;
import com.springboot.app.utils.Constants;

@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

	private final UsuarioDetailsService usuarioDetailsService;
	private final JwtFiltroAutenticacion jwtFiltroAutenticacion;

	public SeguridadConfig(UsuarioDetailsService uds, JwtFiltroAutenticacion jwtFiltroAutenticacion) {
		this.usuarioDetailsService = uds;
		this.jwtFiltroAutenticacion = jwtFiltroAutenticacion;
	}

	@Bean
	public SecurityFilterChain seguridad(HttpSecurity http) throws Exception {

		return http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(auth -> auth.requestMatchers(Constants.URL_BASE_API_V1 + "/auth/**").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers("/public/**").permitAll()
						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.userDetailsService(usuarioDetailsService)
				.addFilterBefore(jwtFiltroAutenticacion, UsernamePasswordAuthenticationFilter.class).build();

	}

	@Bean
	public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {

		return config.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// Orígenes permitidos
		config.setAllowedOrigins(List.of("http://localhost:5173", "https://tu-dominio.com" // Front en producción
		));

		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));

		config.setAllowedHeaders(List.of("*"));

		config.setExposedHeaders(List.of("Authorization", "Content-Type"));

		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

}
