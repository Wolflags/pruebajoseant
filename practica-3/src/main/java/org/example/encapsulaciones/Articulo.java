package org.example.encapsulaciones;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Articulo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String titulo;
    private String cuerpo;
    @ManyToOne(optional = false)
    private Usuario autor;
    private Date fecha;
    @OneToMany(mappedBy = "articulo", fetch = FetchType.EAGER, cascade = CascadeType.ALL) //TODO: Pensar en cómo hacer un fetch lazy
    private List<Comentario> listaComentarios = new ArrayList<Comentario>();
    @ManyToMany(fetch=FetchType.EAGER)
    private List<Etiqueta> listaEtiquetas;

    public Articulo() {}
    public Articulo(String titulo, String cuerpo, Usuario autor, Date fecha, List<Etiqueta> etiquetas) {
        this.titulo = titulo;
        this.cuerpo = cuerpo;
        this.autor = autor;
        this.fecha = fecha;
        this.listaEtiquetas = etiquetas;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public List<Etiqueta> getListaEtiquetas() {
        return listaEtiquetas;
    }

    public List<Comentario> getListaComentarios() { return listaComentarios; }

    public void agregarComentario(Comentario comentario) { listaComentarios.add(comentario); }

    public void setListaComentarios(ArrayList<Comentario> listaComentarios) { this.listaComentarios = listaComentarios; }

    public void setEtiquetas(List<Etiqueta> etiquetas) {
        this.listaEtiquetas = etiquetas;
    }

    public List<Etiqueta> getEtiquetas() {
        return listaEtiquetas;
    }
}
