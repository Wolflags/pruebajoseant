package org.example.servicios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OneToMany;
import org.example.encapsulaciones.Usuario;

import java.util.ArrayList;
import java.util.List;

public class ServiciosUsuario extends GestionDb<Usuario> {
    private static ServiciosUsuario instancia;
    //@OneToMany //Cómo se resuelve este problema?

    /**
     * Crea el usuario administrador al iniciar el programa.
     * USERNAME: admin, PASSWORD: admin
     */
    private ServiciosUsuario(){
        super(Usuario.class);

        if(obtenerUsuarios().isEmpty()) {
            Usuario admin = new Usuario("admin", "Administrador", "admin",
                    true, true, true, null);
            agregarUsuario(admin);
        }
    }

    public static ServiciosUsuario getInstancia(){
        if(instancia==null){
            instancia = new ServiciosUsuario();
        }
        return instancia;
    }

    public void agregarUsuario(Usuario usuario) {
        crear(usuario);
    }

    public Usuario validarUsuario(String username, String password){
        Usuario usuario = find(username);
        if(usuario != null && usuario.getPassword().equals(password) && usuario.isActivo()){
            return usuario;
        }
        return null;
    }

    public Usuario getUsuarioPorUsername(String username){
        try{
            return find(username);
        }catch (Exception e){
            return null;
        }
    }
    public Usuario getUsuarioPorNombre(String nombre){
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT u FROM Usuario u WHERE u.nombre = :nombre";
            List<Usuario> usuarios = em.createQuery(jpql, Usuario.class)
                    .setParameter("nombre", nombre)
                    .getResultList();
            return usuarios.isEmpty() ? null : usuarios.get(0);
        }finally {
            em.close();
        }
    }

    public boolean usernameExiste(String username) {
        Usuario usuario = find(username);
        return usuario != null;
    }
    public void editarUsuario(Usuario usuario){
        editar(usuario);
    }
    public List<Usuario> obtenerUsuarios(){
        return findAll();
    }
}
