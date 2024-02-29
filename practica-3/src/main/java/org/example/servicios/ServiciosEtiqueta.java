package org.example.servicios;

import jakarta.persistence.EntityManager;
import org.example.encapsulaciones.Etiqueta;

import java.util.*;

public class ServiciosEtiqueta extends GestionDb<Etiqueta> {
    private static ServiciosEtiqueta instancia;

    private ServiciosEtiqueta() { super(Etiqueta.class); }
    public static ServiciosEtiqueta getInstancia() {
        if (instancia == null) {
            instancia = new ServiciosEtiqueta();
        }
        return instancia;
    }

    public void agregarEtiqueta(Etiqueta etiqueta) { crear(etiqueta); }

    public List<Etiqueta> getEtiquetas() { return findAll(); }

    public List<Etiqueta> separarPorComas(String etiquetas){
        Map<Long,Etiqueta> etiquetasSeparadas = new HashMap<>();
        String[] etiquetasArray = etiquetas.split(",");

        for (String etiqueta : etiquetasArray) {
            etiqueta = etiqueta.trim();
            if(etiqueta.isBlank()) continue;

            Etiqueta e = getEtiquetaPorNombre(etiqueta);
            if(e == null) {
                e = new Etiqueta(etiqueta);
                agregarEtiqueta(e);
            }
            if(!etiquetasSeparadas.containsKey(e.getId()))
                etiquetasSeparadas.put(e.getId(), e);
        }

        return new ArrayList<>(etiquetasSeparadas.values());
    }

    public String unirPorComas(List<Etiqueta> listaEtiquetas){
        StringBuilder etiquetas = new StringBuilder();
        for (Etiqueta etiqueta : listaEtiquetas) {
            etiquetas.append(etiqueta.getEtiqueta()).append(", ");
        }

        if(etiquetas.isEmpty()) return "";
        return etiquetas.substring(0, etiquetas.length() - 2);
    }

    public Etiqueta getEtiquetaPorNombre(String nombre){
        EntityManager em = getEntityManager();
        try{
            String jpql = "SELECT e FROM Etiqueta e WHERE e.etiqueta = :nombre";
            List<Etiqueta> etiquetas = em.createQuery(jpql, Etiqueta.class)
                    .setParameter("nombre", nombre)
                    .getResultList();

            return etiquetas.isEmpty() ? null : etiquetas.get(0);
        } finally {
            em.close();
        }
    }
}
