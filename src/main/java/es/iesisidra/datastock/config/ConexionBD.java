package es.iesisidra.datastock.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {

    /**
     * Lee la configuracion local y abre una conexion con MySQL.
     */
    public static Connection conectar() throws SQLException {

        Properties propiedades = new Properties();

        try (FileInputStream archivo = new FileInputStream("config.properties")) {
            propiedades.load(archivo);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer config.properties.");
        }

        String url = propiedades.getProperty("db.url");
        String usuario = propiedades.getProperty("db.user");
        String contrasena = propiedades.getProperty("db.password");

        if (url == null || usuario == null || contrasena == null
                || url.isBlank() || usuario.isBlank()) {
            throw new SQLException("Faltan datos en config.properties.");
        }

        return DriverManager.getConnection(url, usuario, contrasena);
    }
}