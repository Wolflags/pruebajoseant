package org.example.servicios;

import org.example.encapsulaciones.Comentario;

public class ServiciosComentario extends GestionDb<Comentario> {
    private static ServiciosComentario instancia;

    private ServiciosComentario() { super(Comentario.class); }

    public static ServiciosComentario getInstancia() {
        if (instancia == null) {
            instancia = new ServiciosComentario();
        }
        return instancia;
    }

    public void agregarComentario(Comentario comentario) {
        crear(comentario);
    }
}
