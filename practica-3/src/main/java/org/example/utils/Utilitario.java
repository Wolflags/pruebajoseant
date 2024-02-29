package org.example.utils;

import org.example.encapsulaciones.Usuario;
import org.example.servicios.ServiciosUsuario;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Utilitario {

    //Método para agregar el user en la navbar
    public static Map<String, Object> modeloBase(String username, ServiciosUsuario serviciosUsuario){
        Map<String, Object> model = new HashMap<>();
        Usuario user = serviciosUsuario.getUsuarioPorUsername(username);
        if(user == null) return model;

        model.put("username", user.getUsername());
        if(user.isAutor()) model.put("autor", true);
        if(user.isAdministrator()) model.put("admin", true);
        return model;
    }

    public static boolean parseCheckbox(String checkbox){
        if(checkbox != null) return checkbox.equals("on");
        return false;
    }

    public static boolean falseIfNull(Boolean bool){
        if(bool == null) return false;
        return bool;
    }

    public static Connection conexionLogsDb() throws SQLException {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(System.getenv("JDBC_DATABASE_URL"));
        return ds.getConnection();
    }

    public static void insertarLog(String username){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            Connection conn = conexionLogsDb();
            PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS log_autenticacion (username VARCHAR(255), time_stamp TIMESTAMP);" +
                                                              "INSERT INTO log_autenticacion VALUES (?, ?);");
            ps.setString(1, username);
            ps.setTimestamp(2, timestamp);
            ps.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
