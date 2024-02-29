package org.example.controladores;



import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.example.encapsulaciones.Foto;
import org.example.encapsulaciones.Usuario;
import org.example.servicios.ServiciosFoto;
import org.example.servicios.ServiciosUsuario;
import org.example.utils.UtilitarioEncriptacion;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;
import static org.example.utils.Utilitario.*;

public class UsuarioControlador extends BaseControlador{

    ServiciosUsuario serviciosUsuario = ServiciosUsuario.getInstancia();
    public UsuarioControlador(Javalin app) {
        super(app);
    }

    @Override
    public void aplicarRutas() {

        app.get("/login", ctx -> {
            Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
            if (usuario != null) {
                ctx.redirect("/");
            } else {
                Map<String, Object> model = new HashMap<>();
                model.put("loginError", Boolean.parseBoolean(ctx.queryParam("loginError")));
                ctx.render("/vistas/login.html", model);
            }
        });

        app.before("/verificacion", ctx -> {
            Usuario user = serviciosUsuario.validarUsuario(ctx.formParam("username"), ctx.formParam("password"));
            if(user == null)
                ctx.redirect("/login?loginError=true");
        });
        app.post("/verificacion", ctx -> {
            String nombreUsuario = ctx.formParam("username");
            Usuario user = serviciosUsuario.validarUsuario(ctx.formParam("username"), ctx.formParam("password"));
            if (user != null && user.isActivo()) {
                ctx.sessionAttribute("username", user.getUsername());
                ctx.sessionAttribute("password", user.getPassword());
                ctx.sessionAttribute("admin", user.isAdministrator());
                ctx.sessionAttribute("autor", user.isAutor());
                if (ctx.formParam("remember-me") != null) {
                    CrearCookie(ctx, nombreUsuario);
                }
                ctx.sessionAttribute("usuario", user);
                insertarLog(user.getUsername());
                ctx.redirect("/");
            }else{
                ctx.redirect("/login?loginError=true");
            }
        });

            app.get("cerrar-sesion", ctx -> {
                ctx.req().getSession().invalidate();
                ctx.removeCookie("DatosUsuario");
                ctx.redirect("/");
            });

            app.before("/crear-usuario", ctx ->{
                Usuario user = UsuarioControlador.autenticarConCookie(ctx);
                if(user == null){
                    user = ServiciosUsuario.getInstancia().getUsuarioPorUsername(ctx.sessionAttribute("username"));
                }
                if(user == null || !user.isAdministrator())
                    ctx.redirect("/login");
            });
            app.get("crear-usuario", ctx ->{
                Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                boolean errorEmpty = Boolean.parseBoolean(ctx.queryParam("errorEmpty"));
                boolean errorUsername = Boolean.parseBoolean(ctx.queryParam("errorUsername"));
                boolean completed = Boolean.parseBoolean(ctx.queryParam("completed"));

                Map<String, Object> model = modeloBase(usuario != null ? usuario.getUsername() : null, serviciosUsuario);
                model.put("errorEmpty", errorEmpty);
                model.put("errorUsername", errorUsername);
                model.put("completed", completed);
                ctx.render("/vistas/crear-usuario.html", model);
            });

            app.before("/procesar-registro", ctx ->{
                Usuario user = UsuarioControlador.autenticarConCookie(ctx);
                if(user == null){
                    user = ServiciosUsuario.getInstancia().getUsuarioPorUsername(ctx.sessionAttribute("username"));
                }
                if(user == null || !user.isAdministrator())
                    ctx.redirect("/login");

                if(ctx.formParam("username").isBlank() || ctx.formParam("name").isBlank() ||
                        ctx.formParam("password").isBlank())
                    ctx.redirect("/crear-usuario?errorEmpty=true");

                if(serviciosUsuario.usernameExiste(ctx.formParam("username")))
                    ctx.redirect("/crear-usuario?errorUsername=true");

            });
            app.post("/procesar-registro", ctx ->{
                UploadedFile file = ctx.uploadedFile("foto");
                Foto foto = null;
                try {
                    byte[] bytes = file.content().readAllBytes();
                    String encodedString = Base64.getEncoder().encodeToString(bytes);
                    foto = new Foto(file.filename(), file.contentType(), encodedString);
                    ServiciosFoto.getInstancia().crear(foto);
                }catch (Exception e){
                    e.printStackTrace();
                }

                Usuario usuario = new Usuario(ctx.formParam("username"), ctx.formParam("name"), ctx.formParam("password"),
                        parseCheckbox(ctx.formParam("admin")), parseCheckbox(ctx.formParam("autor")), parseCheckbox(ctx.formParam("activo")), foto);
                serviciosUsuario.agregarUsuario(usuario);
                ctx.redirect("/crear-usuario?completed=true");
            });

            app.get("/listar-usuario", ctx ->{
                Usuario usuario = UsuarioControlador.autenticarConCookie(ctx);
                Map<String, Object> model = modeloBase(usuario != null ? usuario.getUsername() : null, serviciosUsuario);
                model.put("usuarios", serviciosUsuario.obtenerUsuarios());
                ctx.render("/vistas/listar-usuario.html", model);
            });
            app.routes(() -> {
                path("/usuario/", () -> {
                    before("{nombre}", ctx -> {
                        String nombre = (ctx.pathParam("nombre"));
                        if (serviciosUsuario.getUsuarioPorNombre(nombre) == null)
                            ctx.redirect("/");
                    });
                    get("{nombre}", ctx -> {
                        Usuario usuarioCookie = UsuarioControlador.autenticarConCookie(ctx);
                        Map<String, Object> model = modeloBase(usuarioCookie != null ? usuarioCookie.getUsername() : null, serviciosUsuario);
                        String nombre = (ctx.pathParam("nombre"));
                        Usuario usuario = serviciosUsuario.getUsuarioPorNombre(nombre);
                        model.put("usuario", usuario);
                        model.put("admin", usuario.isAdministrator());
                        model.put("autor", usuario.isAutor());
                        model.put("activo", usuario.isActivo());
                        model.put("foto", usuario.getFoto());
                        ctx.render("/vistas/ver-usuario.html", model);
                    });

                    before("{nombre}/editar", ctx -> {
                        Usuario user = UsuarioControlador.autenticarConCookie(ctx);
                        if(user == null){
                            user = ServiciosUsuario.getInstancia().getUsuarioPorUsername(ctx.sessionAttribute("username"));
                        }
                        if(user == null || !user.isAdministrator())
                            ctx.redirect("/login");
                    });
                    get("{nombre}/editar", ctx -> {
                        Usuario usuarioCookie = UsuarioControlador.autenticarConCookie(ctx);
                        Map<String, Object> model = modeloBase(usuarioCookie != null ? usuarioCookie.getUsername() : null, serviciosUsuario);
                        String nombre = (ctx.pathParam("nombre"));
                        Usuario usuario = serviciosUsuario.getUsuarioPorNombre(nombre);
                        model.put("nombre", usuario.getNombre());
                        model.put("password", usuario.getPassword());
                        model.put("admin", usuario.isAdministrator());
                        model.put("autor", usuario.isAutor());
                        model.put("activo", usuario.isActivo());
                        model.put("foto", usuario.getFoto());
                        ctx.render("/vistas/editar-usuario.html", model);
                    });
                    before("{nombre}/editar-usuario", ctx -> {
                        Usuario user = UsuarioControlador.autenticarConCookie(ctx);
                        if(user == null){
                            user = ServiciosUsuario.getInstancia().getUsuarioPorUsername(ctx.sessionAttribute("username"));
                        }
                        if(user == null)
                            ctx.redirect("/login");

                        boolean admin = Boolean.parseBoolean(ctx.sessionAttribute("admin").toString());
                        if (!admin)
                            ctx.redirect("/");
                    });
                    put("{nombre}/editar-usuario", ctx -> {
                        String nombre = (ctx.pathParam("nombre"));
                        Usuario usuario = serviciosUsuario.getUsuarioPorNombre(nombre);
                        usuario.setNombre(ctx.formParam("nombre"));
                        usuario.setPassword(ctx.formParam("password"));
                        usuario.setAdministrator(parseCheckbox(ctx.formParam("admin")));
                        usuario.setAutor(parseCheckbox(ctx.formParam("autor")));
                        usuario.setActivo(parseCheckbox(ctx.formParam("activo")));
                        UploadedFile nuevaFoto = ctx.uploadedFile("nuevaFoto");
                        if (nuevaFoto != null) {
                            try {
                                byte[] bytes = nuevaFoto.content().readAllBytes();
                                String encodedString = Base64.getEncoder().encodeToString(bytes);
                                Foto foto = new Foto(nuevaFoto.filename(), nuevaFoto.contentType(), encodedString);
                                ServiciosFoto.getInstancia().crear(foto);
                                usuario.setFoto(foto);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        serviciosUsuario.editarUsuario(usuario);
                    });
                    after("{nombre}/editar-usuario", ctx -> {
                        ctx.redirect("/listar-usuario");
                    });
                });
            });

    }
    public void CrearCookie (Context ctx, String DatosUsuario){
        String encryptedUserData = UtilitarioEncriptacion.encrypt(DatosUsuario);
        ctx.cookie("DatosUsuario", encryptedUserData, 604800);
    }

    public static Usuario autenticarConCookie(Context ctx){
        String cookie = ctx.cookie("DatosUsuario");
        if(cookie != null){
            String datosDelUsuario = UtilitarioEncriptacion.decrypt(cookie);
            Usuario usuario = ServiciosUsuario.getInstancia().getUsuarioPorUsername(datosDelUsuario);
            if(usuario != null && usuario.isActivo()){
                return usuario;
            } else {
                ctx.redirect("/login");
                return null; // Necesitamos retornar null aquí para evitar cualquier otro flujo de ejecución
            }
        }
        Usuario user = ctx.sessionAttribute("usuario");
        return user;
    }

}
