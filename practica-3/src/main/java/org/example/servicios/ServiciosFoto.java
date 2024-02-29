package org.example.servicios;

import org.example.encapsulaciones.Foto;

public class ServiciosFoto extends GestionDb<Foto> {

    private static ServiciosFoto instancia;

    private ServiciosFoto(){
        super(Foto.class);
    }

    public static ServiciosFoto getInstancia(){
        if(instancia==null){
            instancia = new ServiciosFoto();
        }
        return instancia;
    }

}
