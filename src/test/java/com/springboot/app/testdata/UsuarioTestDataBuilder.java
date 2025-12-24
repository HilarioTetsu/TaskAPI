package com.springboot.app.testdata;



import java.util.List;


import com.github.javafaker.Faker;
import com.springboot.app.models.entities.Rol;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;

public class UsuarioTestDataBuilder {

    private static final Faker faker = new Faker();

    private Long id = faker.number().numberBetween(0, Long.MAX_VALUE); 
    private String email = faker.internet().emailAddress();
    private String username = faker.name().username();
    private String password ="$2a$10$/WG6.DlBIoDDv5dS1fnszuCEyBHODY8zIFmzB71fqF4WfVN5JMmRS";
    private Short status = Constants.STATUS_ACTIVE;
    private List<Tarea> tareasAsignadas = null;

    private List<Rol> roles = List.of(new Rol((short)1,"Basico",(short) 1,null) );


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
    
    public UsuarioTestDataBuilder withTareasAsigandas(List<Tarea> tareas) {
        this.tareasAsignadas = tareas;
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
        usuario.setTareasAsignadas(tareasAsignadas);

        return usuario;
    }
}
