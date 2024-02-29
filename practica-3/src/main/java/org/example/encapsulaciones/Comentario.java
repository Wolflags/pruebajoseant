package org.example.encapsulaciones;

import jakarta.persistence.*;

import javax.annotation.processing.Generated;
import java.io.Serializable;

@Entity
public class Comentario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String comentario;

    @ManyToOne(optional = false)
    private Usuario autor;

    @ManyToOne(optional = false)
    @JoinColumn(name="articulo_id")
    private Articulo articulo;


    public Comentario() {}
    public Comentario(String comentario, Usuario autor, Articulo articulo) {
        this.comentario = comentario;
        this.autor = autor;
        this.articulo = articulo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }
}
