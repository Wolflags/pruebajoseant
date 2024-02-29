package org.example.servicios;

public class ServiciosBootstrap {
    private static ServiciosBootstrap instancia;

    public static ServiciosBootstrap getInstancia(){
        if(instancia==null){
            instancia = new ServiciosBootstrap();
        }
        return instancia;
    }

    public void startDb(){
        try {
            //Modo servidor H2.
            org.h2.tools.Server.createTcpServer("-tcpPort",
                    "9092",
                    "-tcpAllowOthers",
                    "-tcpDaemon",
                    "-ifNotExists").start();
            //Abriendo el cliente web. El valor 0 representa puerto aleatorio.
            String status = org.h2.tools.Server.createWebServer("-trace", "-webPort", "0").start().getStatus();
            System.out.println("Status Web: "+status);
        }catch (java.sql.SQLException ex){
            System.out.println("Problema con la base de datos: "+ex.getMessage());
        }
    }
}
