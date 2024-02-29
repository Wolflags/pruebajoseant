package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.example.controladores.ArticuloControlador;
import org.example.controladores.UsuarioControlador;
import org.example.encapsulaciones.Usuario;
import org.example.encapsulaciones.Articulo;
import org.example.encapsulaciones.Etiqueta;
import org.example.servicios.ServiciosBootstrap;
import org.example.servicios.ServiciosEtiqueta;
import org.example.servicios.ServiciosUsuario;
import org.example.servicios.ServiciosArticulo;

import java.util.List;
import java.util.Map;

import static org.example.utils.Utilitario.modeloBase;

public class Main {
    public static void main(String[] args) {
        ServiciosBootstrap.getInstancia().startDb();
        ServiciosUsuario serviciosUsuario = ServiciosUsuario.getInstancia();

        Javalin app = Javalin.create().start(7071);


        app.get("/", ctx -> {
            Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);

            Map<String, Object> model = modeloBase(usuario != null ? usuario.getUsername() : null, serviciosUsuario);
            ServiciosArticulo serviciosArticulo = ServiciosArticulo.getInstancia();
            Etiqueta e = ServiciosEtiqueta.getInstancia().getEtiquetaPorNombre(ctx.queryParam("etiqueta"));
            int paginaActual = (ctx.queryParam("paginaActual") == null) ? 1 : Integer.parseInt(ctx.queryParam("paginaActual"));
            int numeroPaginas = (e != null) ? serviciosArticulo.getNumeroPaginasPorEtiqueta(e)
                                            : serviciosArticulo.getNumeroPaginas();

            if(paginaActual < 1) paginaActual = 1;
            else if (paginaActual > numeroPaginas) paginaActual = numeroPaginas;

            List<Articulo> articulos = (e != null) ? serviciosArticulo.getArticulosPorEtiqueta(paginaActual, e)
                                                   : serviciosArticulo.getArticulos(paginaActual);
            model.put("etiquetaSeleccionada", e);
            model.put("articulos", articulos);
            model.put("numeroPaginas", numeroPaginas);
            model.put("paginaActual", paginaActual);
            model.put("etiquetas", ServiciosEtiqueta.getInstancia().getEtiquetas());
            ctx.render("/vistas/index.html", model);
        });

        new UsuarioControlador(app).aplicarRutas();
        new ArticuloControlador(app).aplicarRutas();
    }

}