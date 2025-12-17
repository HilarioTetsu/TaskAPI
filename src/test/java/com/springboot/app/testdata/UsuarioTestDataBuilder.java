package com.springboot.app.testdata;

import java.util.Collections;
import java.util.List;


import com.github.javafaker.Faker;
import com.springboot.app.models.entities.Rol;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;

public class UsuarioTestDataBuilder {

    private static final Faker faker = new Faker();

    private Long id = faker.number().numberBetween(Long.MIN_VALUE, Long.MAX_VALUE); 
    private String email = faker.internet().emailAddress();
    private String username = faker.name().username();
    private String password ="$2a$10$/WG6.DlBIoDDv5dS1fnszuCEyBHODY8zIFmzB71fqF4WfVN5JMmRS";
    private Short status = Constants.STATUS_ACTIVE;

    private List<Rol> roles = Collections.emptyList();


    public UsuarioTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public UsuarioTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UsuarioTestDataBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UsuarioTestDataBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UsuarioTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }

    public UsuarioTestDataBuilder withRoles(List<Rol> roles) {
        this.roles = roles;
        return this;
    }

    public Usuario build() {
        Usuario usuario = new Usuario();

        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setStatus(status);
        usuario.setRoles(roles);


        return usuario;
    }
}
