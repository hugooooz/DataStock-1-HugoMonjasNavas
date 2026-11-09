
package es.iesisidra.datastock.complementarias;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Demostracion de una base de datos SQLite con JDBC.
 * Se ejecuta de forma independiente a DataStock.
 */
public class DemoSQLite {

    public static void main(String[] args) {

        String ruta = "datos/sqlite/productos_demo.db";
        String url = "jdbc:sqlite:" + ruta;

        System.out.println("===== DEMOSTRACION SQLITE =====");

        try {
            // Crear la carpeta si todavia no existe.
            Files.createDirectories(Path.of("datos/sqlite"));

            try (Connection conexion = DriverManager.getConnection(url)) {

                System.out.println("[OK] Conexion SQLite abierta: " + ruta);

                crearTabla(conexion);

                int insertados = 0;

                insertados = insertados + insertarProducto(conexion, "SQL-001", "Producto demo A");

                insertados = insertados + insertarProducto(conexion, "SQL-002", "Producto demo B");

                int existentes = 2 - insertados;

                System.out.println("Insertados: " + insertados + " | Ya existentes: " + existentes);

                listarProductos(conexion);
            }

            System.out.println("[OK] Conexion cerrada correctamente.");

            if (Files.exists(Path.of(ruta))) {
                System.out.println("[OK] Fichero SQLite guardado: " + ruta);
            }

        } catch (IOException e) {
            System.out.println("[ERROR] No se pudo crear la carpeta SQLite.");
        } catch (SQLException e) {
            System.out.println("[ERROR] Problema con SQLite: " + e.getMessage());
        }
    }

    private static void crearTabla(Connection conexion)throws SQLException {

        String sql = "CREATE TABLE IF NOT EXISTS producto_demo ("
                + "codigo TEXT PRIMARY KEY NOT NULL, "
                + "nombre TEXT NOT NULL)";

        try (Statement sentencia = conexion.createStatement()) {
            sentencia.executeUpdate(sql);
        }

        System.out.println("[OK] Tabla producto_demo disponible.");
    }

    private static int insertarProducto(
            Connection conexion, String codigo, String nombre)
            throws SQLException {

        String sql = "INSERT OR IGNORE INTO producto_demo " + "(codigo, nombre) VALUES (?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);

            return ps.executeUpdate();
        }
    }

    private static void listarProductos(Connection conexion)throws SQLException {

        System.out.println("\n===== PRODUCTOS GUARDADOS =====");

        String sql = "SELECT codigo, nombre " + "FROM producto_demo ORDER BY codigo";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.println(rs.getString("codigo") + " | " + rs.getString("nombre"));
            }
        }
    }
}
