package org.example.encapsulaciones;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.io.Serializable;

@Entity
public class Usuario implements Serializable {
    @Id
    private String username;
    private String nombre;
    private String password;
    private boolean administrator;
    private boolean autor;
    private boolean activo;
    @OneToOne
    private Foto foto;

    public Usuario() {}
    public Usuario(String username, String nombre, String password, boolean administrator, boolean autor, boolean activo, Foto foto) {
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.administrator = administrator;
        this.autor = autor;
        this.activo = activo;
        this.foto = foto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public boolean isAutor() {
        return autor;
    }

    public void setAutor(boolean autor) {
        this.autor = autor;
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Foto getFoto() { return foto; }
    public void setFoto(Foto foto) { this.foto = foto; }

}
