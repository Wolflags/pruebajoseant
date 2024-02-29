package org.example.servicios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.example.encapsulaciones.Articulo;
import org.example.encapsulaciones.Etiqueta;

import java.text.SimpleDateFormat;
import java.util.List;

public class ServiciosArticulo extends GestionDb<Articulo>{

    private static ServiciosArticulo instancia;

    private ServiciosArticulo() {
        super(Articulo.class);
    }

    public static ServiciosArticulo getInstancia(){
        if(instancia==null){
            instancia = new ServiciosArticulo();
        }
        return instancia;
    }

    public void agregarArticulo(Articulo articulo) {
        crear(articulo);
    }

    public List<Articulo> getArticulos(int paginaActual) {
        int articulosPorPagina = 5;
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Articulo> criteriaQuery = criteriaBuilder.createQuery(Articulo.class);
            Root<Articulo> from = criteriaQuery.from(Articulo.class);
            CriteriaQuery<Articulo> select = criteriaQuery.select(from).
                    orderBy(criteriaBuilder.desc(from.get("fecha")));

            TypedQuery<Articulo> typedQuery = em.createQuery(select);
            typedQuery.setFirstResult(Math.max(0, (paginaActual - 1) * articulosPorPagina));
            typedQuery.setMaxResults(articulosPorPagina);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Articulo> getArticulosPorEtiqueta(int paginaActual, Etiqueta e) {
        int articulosPorPagina = 5;
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Articulo> criteriaQuery = cb.createQuery(Articulo.class);
            Root<Articulo> from = criteriaQuery.from(Articulo.class);

            Join<Articulo, Etiqueta> joinArticulo = from.join("listaEtiquetas", JoinType.INNER);
            criteriaQuery.select(from).where(cb.equal(joinArticulo.get("id"), e.getId())).
                    orderBy(cb.desc(from.get("fecha")));

            TypedQuery<Articulo> typedQuery = em.createQuery(criteriaQuery);
            typedQuery.setFirstResult(Math.max(0, (paginaActual - 1) * articulosPorPagina));
            typedQuery.setMaxResults(articulosPorPagina);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    public Articulo getArticuloPorId(long id){
        return find(id);
    }

    public String getFechaFormateada(Articulo articulo){
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        return formato.format(articulo.getFecha());
    }

    public void eliminarArticulo(long id) { eliminar(id); }

    public int getNumeroPaginas() {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
            Root<Articulo> articuloRoot = criteriaQuery.from(Articulo.class);
            criteriaQuery.select(builder.count(articuloRoot));

            long numeroArticulos = em.createQuery(criteriaQuery).getSingleResult();
            return (int) Math.ceil((double) numeroArticulos / 5);
        }finally {
            em.close();
        }
    }

    public int getNumeroPaginasPorEtiqueta(Etiqueta e) {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
            Root<Articulo> articuloRoot = criteriaQuery.from(Articulo.class);

            Join<Articulo, Etiqueta> joinArticulo = articuloRoot.join("listaEtiquetas", JoinType.INNER);
            criteriaQuery.select(cb.count(articuloRoot)).where(cb.equal(joinArticulo.get("id"), e.getId()));

            long numeroArticulos = em.createQuery(criteriaQuery).getSingleResult();
            return (int) Math.ceil((double) numeroArticulos / 5);
        }finally {
            em.close();
        }
    }
}
