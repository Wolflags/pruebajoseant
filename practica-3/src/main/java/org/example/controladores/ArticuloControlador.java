package org.example.controladores;

import io.javalin.Javalin;
import org.example.encapsulaciones.Articulo;
import org.example.encapsulaciones.Comentario;
import org.example.encapsulaciones.Usuario;
import org.example.servicios.ServiciosArticulo;
import org.example.servicios.ServiciosComentario;
import org.example.servicios.ServiciosEtiqueta;
import org.example.servicios.ServiciosUsuario;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;
import org.example.servicios.ServiciosEtiqueta.*;
import static org.example.utils.Utilitario.*;

public class ArticuloControlador extends BaseControlador {

    ServiciosArticulo serviciosArticulo = ServiciosArticulo.getInstancia();
    ServiciosUsuario serviciosUsuario = ServiciosUsuario.getInstancia();

    public ArticuloControlador(Javalin app) {
        super(app);
    }

    @Override
    public void aplicarRutas() {
        app.routes(() -> {
            path("/articulo/", () -> {

                before("{id}", ctx -> {
                    long id = Long.parseLong(ctx.pathParam("id"));
                    if(serviciosArticulo.getArticuloPorId(id) == null)
                        ctx.redirect("/");
                });
                get("{id}", ctx -> {
                    Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                    Map<String, Object> model = modeloBase(usuario != null ? usuario.getUsername() : null, serviciosUsuario);
                    long id = Long.parseLong(ctx.pathParam("id"));
                    Articulo articulo = serviciosArticulo.getArticuloPorId(id);
                    model.put("articulo", articulo);
                    model.put("fecha", serviciosArticulo.getFechaFormateada(articulo));
                    model.put("admin", usuario != null && usuario.isAdministrator()); //Se verifica si es null para evitar NullPointerException
                    ctx.render("/vistas/ver-articulo.html", model);
                });

                post("{id}/agregar-comentario", ctx -> {
                    Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                    if(usuario == null)
                        ctx.redirect("/login");

                    long id = Long.parseLong(ctx.pathParam("id"));
                    Articulo articulo = serviciosArticulo.getArticuloPorId(id);
                    Comentario comentario = new Comentario(
                            ctx.formParam("comentarioInput"),
                            usuario,
                            articulo);
                    ServiciosComentario.getInstancia().agregarComentario(comentario);
                    articulo.agregarComentario(comentario);
                    serviciosArticulo.editar(articulo);
                    ctx.redirect("/articulo/"+id);
                });

                before("{id}/eliminar", ctx -> {
                    Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                    Articulo articulo = serviciosArticulo.getArticuloPorId(Long.parseLong(ctx.pathParam("id")));
                    if(usuario == null || (!usuario.isAdministrator() && !articulo.getAutor().getUsername().equals(usuario.getUsername())))
                        ctx.redirect("/");
                });
                delete("{id}/eliminar", ctx -> {
                    long id = Long.parseLong(ctx.pathParam("id"));
                    serviciosArticulo.eliminarArticulo(id);
                });
                after("{id}/eliminar", ctx -> {
                    ctx.redirect("/");
                });

                before("{id}/editar", ctx -> {
                    Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                    Articulo articulo = serviciosArticulo.getArticuloPorId(Long.parseLong(ctx.pathParam("id")));
                    if(usuario == null || (!usuario.isAdministrator() && !articulo.getAutor().getUsername().equals(usuario.getUsername())))
                        ctx.redirect("/");
                });
                get("{id}/editar", ctx -> {
                    Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                    Map<String, Object> model = modeloBase(usuario != null ? usuario.getUsername() : null, serviciosUsuario);
                    long id = Long.parseLong(ctx.pathParam("id"));
                    Articulo articulo = serviciosArticulo.getArticuloPorId(id);
                    model.put("articulo", articulo);
                    model.put("etiquetas", ServiciosEtiqueta.getInstancia().unirPorComas(articulo.getEtiquetas()));
                    model.put("arrayEtiquetas", ServiciosEtiqueta.getInstancia().getEtiquetas());
                    ctx.render("/vistas/editar-articulo.html", model);
                });

                before("{id}/editar-registro", ctx -> {
                    Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                    Articulo articulo = serviciosArticulo.getArticuloPorId(Long.parseLong(ctx.pathParam("id")));
                    if(usuario == null || (!usuario.isAdministrator() && !articulo.getAutor().getUsername().equals(usuario.getUsername())))
                        ctx.redirect("/");
                });
                put("{id}/editar-registro", ctx -> {
                    long id = Long.parseLong(ctx.pathParam("id"));
                    Articulo articulo = serviciosArticulo.getArticuloPorId(id);
                    articulo.setTitulo(ctx.formParam("titulo"));
                    articulo.setCuerpo(ctx.formParam("cuerpo"));
                    articulo.setEtiquetas(ServiciosEtiqueta.getInstancia().separarPorComas(ctx.formParam("etiquetas")));
                    serviciosArticulo.editar(articulo);
                });
                after("{id}/editar-registro", ctx -> {
                    ctx.redirect("/articulo/"+ctx.pathParam("id"));
                });
            });

            before("/crear-articulo", ctx -> {
                Usuario user = UsuarioControlador.autenticarConCookie(ctx);
                if(user == null){
                    user = ServiciosUsuario.getInstancia().getUsuarioPorUsername(ctx.sessionAttribute("username"));
                }
                if(user == null || !user.isAutor())
                    ctx.redirect("/login");
            });
            app.get("/crear-articulo", ctx -> {
                Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                Map<String, Object> model = modeloBase(usuario != null ? usuario.getUsername() : null, serviciosUsuario);
                model.put("errorEmpty", ctx.queryParam("errorEmpty"));
                model.put("arrayEtiquetas", ServiciosEtiqueta.getInstancia().getEtiquetas());
                ctx.render("/vistas/crear-articulo.html", model);
            });

            before("/publicar-articulo", ctx -> {
                Usuario user = UsuarioControlador.autenticarConCookie(ctx);
                if(user == null || !user.isAutor())
                    ctx.redirect("/login");

                if(ctx.formParam("titulo").isBlank() || ctx.formParam("cuerpo").isBlank())
                    ctx.redirect("/crear-articulo?errorEmpty=true");
            });
            app.post("/publicar-articulo", ctx -> {
                Usuario autor = UsuarioControlador.autenticarConCookie(ctx);
                Articulo articulo = new Articulo(
                        ctx.formParam("titulo"),
                        ctx.formParam("cuerpo"),
                        autor,
                        new Date(),
                        ServiciosEtiqueta.getInstancia().separarPorComas(ctx.formParam("etiquetas")));
                serviciosArticulo.agregarArticulo(articulo);
                ctx.redirect("/");
            });
        });

    }
}
