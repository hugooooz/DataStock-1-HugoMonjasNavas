
package es.iesisidra.datastock.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/** Gestiona las categorias y los proveedores de MySQL. */
public final class MenuCatalogos {

    private MenuCatalogos() {
    }

    private static void correcto(String texto) {
        System.out.println(ColoresConsola.pintar(
                "[OK] " + texto, ColoresConsola.VERDE));
    }

    private static void error(String texto) {
        System.out.println(ColoresConsola.pintar(
                "[ERROR] " + texto, ColoresConsola.ROJO));
    }

    private static void errorSQL(SQLException e) {
        if (e.getErrorCode() == 1062) {
            error("Ya existe un registro con ese identificador, nombre o email.");
        } else if (e.getErrorCode() == 1451) {
            error("No se puede eliminar porque tiene productos asociados.");
        } else {
            error("No se pudo realizar la operacion en MySQL: " + e.getMessage());
        }
    }

    private static int leerId(String mensaje) {
        int id;

        do {
            id = LectorConsola.leerEntero(mensaje);

            if (id <= 0) {
                error("El identificador debe ser mayor que cero.");
            }
        } while (id <= 0);

        return id;
    }

    private static String leerNombre(String mensaje, int minimo, int maximo) {
        String nombre;

        do {
            nombre = LectorConsola.leerTexto(mensaje).trim();

            if (nombre.length() < minimo || nombre.length() > maximo) {
                error("El nombre debe tener entre "
                        + minimo + " y " + maximo + " caracteres.");
            }

        } while (nombre.length() < minimo || nombre.length() > maximo);

        return nombre;
    }

    private static boolean emailValido(String email) {
        return email.isEmpty()
                || (email.length() <= 120
                && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"));
    }

    private static String leerEmail(String mensaje) {
        String email;

        do {
            email = LectorConsola.leerTexto(mensaje).trim();

            if (!emailValido(email)) {
                error("Email no valido. Ejemplo: contacto@empresa.com");
            }

        } while (!emailValido(email));

        return email;
    }

    // ==========================
    // MENU DE CATEGORIAS
    // ==========================

    public static void categorias() {
        int opcion;

        do {
            System.out.println("\n===== MENU DE CATEGORIAS =====");
            System.out.println("1. Dar de alta una categoria");
            System.out.println("2. Listar categorias");
            System.out.println("3. Modificar nombre");
            System.out.println("4. Eliminar categoria");
            System.out.println("0. Volver al menu MySQL");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opcion: ", 0, 4);

            switch (opcion) {
                case 1 -> altaCategoria();
                case 2 -> listarCategorias();
                case 3 -> modificarCategoria();
                case 4 -> eliminarCategoria();
                case 0 -> System.out.println("Volviendo al menu MySQL.");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }

        } while (opcion != 0);
    }

    private static void altaCategoria() {
        System.out.println("\n===== ALTA DE CATEGORIA =====");

        int id = leerId("ID de categoria: ");
        String nombre = leerNombre("Nombre: ", 1, 60);

        String sql = "INSERT INTO categoria(id, nombre) VALUES (?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, nombre);

            int filas = ps.executeUpdate();

            if (filas == 1) {
                correcto("Categoria registrada correctamente.");
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void listarCategorias() {
        System.out.println("\n===== LISTADO DE CATEGORIAS =====");

        String sql = "SELECT id, nombre FROM categoria ORDER BY id";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                System.out.println(
                        rs.getInt("id") + " - " + rs.getString("nombre"));
            }

            if (!hay) {
                System.out.println("No hay categorias registradas.");
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void modificarCategoria() {
        System.out.println("\n===== MODIFICAR CATEGORIA =====");

        int id = leerId("ID de categoria: ");

        try (Connection con = ConexionBD.conectar()) {

            String sqlBuscar = "SELECT nombre FROM categoria WHERE id = ?";

            try (PreparedStatement buscar = con.prepareStatement(sqlBuscar)) {
                buscar.setInt(1, id);

                try (ResultSet rs = buscar.executeQuery()) {

                    if (!rs.next()) {
                        error("No existe la categoria con ID " + id + ".");
                        return;
                    }

                    System.out.println("Nombre actual: "
                            + rs.getString("nombre"));
                }
            }

            String nombre = leerNombre("Nuevo nombre: ", 1, 60);

            if (!LectorConsola.confirmar("Confirmas la modificacion?")) {
                System.out.println("Modificacion cancelada.");
                return;
            }

            String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setInt(2, id);

                int filas = ps.executeUpdate();

                if (filas == 1) {
                    correcto("Categoria modificada correctamente.");
                } else {
                    error("La categoria ya no existe.");
                }
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void eliminarCategoria() {
        System.out.println("\n===== ELIMINAR CATEGORIA =====");

        int id = leerId("ID de categoria: ");

        try (Connection con = ConexionBD.conectar()) {

            String sqlBuscar = "SELECT nombre FROM categoria WHERE id = ?";

            try (PreparedStatement buscar = con.prepareStatement(sqlBuscar)) {
                buscar.setInt(1, id);

                try (ResultSet rs = buscar.executeQuery()) {

                    if (!rs.next()) {
                        error("No existe la categoria con ID " + id + ".");
                        return;
                    }

                    System.out.println("Categoria: " + id
                            + " - " + rs.getString("nombre"));
                }
            }

            if (!LectorConsola.confirmar("Confirmas la eliminacion?")) {
                System.out.println("Eliminacion cancelada.");
                return;
            }

            String sql = "DELETE FROM categoria WHERE id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);

                int filas = ps.executeUpdate();

                if (filas == 1) {
                    correcto("Categoria eliminada correctamente.");
                } else {
                    error("La categoria ya no existe.");
                }
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    // ==========================
    // MENU DE PROVEEDORES
    // ==========================

    public static void proveedores() {
        int opcion;

        do {
            System.out.println("\n===== MENU DE PROVEEDORES =====");
            System.out.println("1. Dar de alta un proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar proveedor");
            System.out.println("4. Eliminar proveedor");
            System.out.println("0. Volver al menu MySQL");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opcion: ", 0, 4);

            switch (opcion) {
                case 1 -> altaProveedor();
                case 2 -> listarProveedores();
                case 3 -> modificarProveedor();
                case 4 -> eliminarProveedor();
                case 0 -> System.out.println("Volviendo al menu MySQL.");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }

        } while (opcion != 0);
    }

    private static void altaProveedor() {
        System.out.println("\n===== ALTA DE PROVEEDOR =====");

        int id = leerId("ID de proveedor: ");
        String nombre = leerNombre("Nombre: ", 1, 100);
        String email = leerEmail("Email (Intro si no tiene): ");

        String sql = "INSERT INTO proveedor(id, nombre, email) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, nombre);

            if (email.isEmpty()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, email);
            }

            int filas = ps.executeUpdate();

            if (filas == 1) {
                correcto("Proveedor registrado correctamente.");
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void listarProveedores() {
        System.out.println("\n===== LISTADO DE PROVEEDORES =====");

        String sql = "SELECT id, nombre, email FROM proveedor ORDER BY id";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;

            while (rs.next()) {
                hay = true;

                String email = rs.getString("email");

                if (email == null) {
                    email = "-";
                }

                System.out.println(
                        rs.getInt("id") + " - "
                        + rs.getString("nombre") + " - "
                        + email);
            }

            if (!hay) {
                System.out.println("No hay proveedores registrados.");
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void modificarProveedor() {
        System.out.println("\n===== MODIFICAR PROVEEDOR =====");

        int id = leerId("ID de proveedor: ");

        try (Connection con = ConexionBD.conectar()) {

            String nombreActual;
            String emailActual;

            String sqlBuscar =
                    "SELECT nombre, email FROM proveedor WHERE id = ?";

            try (PreparedStatement buscar = con.prepareStatement(sqlBuscar)) {
                buscar.setInt(1, id);

                try (ResultSet rs = buscar.executeQuery()) {

                    if (!rs.next()) {
                        error("No existe el proveedor con ID " + id + ".");
                        return;
                    }

                    nombreActual = rs.getString("nombre");
                    emailActual = rs.getString("email");
                }
            }

            System.out.println("Nombre actual: " + nombreActual);
            System.out.println("Email actual: "
                    + (emailActual == null ? "-" : emailActual));

            String nombre = LectorConsola.leerTexto(
                    "Nuevo nombre (Intro para conservar): ").trim();

            if (nombre.isEmpty()) {
                nombre = nombreActual;
            }

            if (nombre.length() > 100) {
                error("El nombre no puede superar 100 caracteres.");
                return;
            }

            String email = LectorConsola.leerTexto(
                    "Nuevo email (Intro conserva, - elimina): ").trim();

            if (email.isEmpty()) {
                email = emailActual;
            } else if (email.equals("-")) {
                email = null;
            }

            if (email != null && !emailValido(email)) {
                error("Email no valido.");
                return;
            }

            if (!LectorConsola.confirmar("Confirmas la modificacion?")) {
                System.out.println("Modificacion cancelada.");
                return;
            }

            String sql = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);

                if (email == null) {
                    ps.setNull(2, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(2, email);
                }

                ps.setInt(3, id);

                int filas = ps.executeUpdate();

                if (filas == 1) {
                    correcto("Proveedor modificado correctamente.");
                } else {
                    error("El proveedor ya no existe.");
                }
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void eliminarProveedor() {
        System.out.println("\n===== ELIMINAR PROVEEDOR =====");

        int id = leerId("ID de proveedor: ");

        try (Connection con = ConexionBD.conectar()) {

            String sqlBuscar =
                    "SELECT nombre, email FROM proveedor WHERE id = ?";

            try (PreparedStatement buscar = con.prepareStatement(sqlBuscar)) {
                buscar.setInt(1, id);

                try (ResultSet rs = buscar.executeQuery()) {

                    if (!rs.next()) {
                        error("No existe el proveedor con ID " + id + ".");
                        return;
                    }

                    System.out.println("Proveedor: " + id
                            + " - " + rs.getString("nombre")
                            + " - " + (rs.getString("email") == null
                                    ? "-" : rs.getString("email")));
                }
            }

            if (!LectorConsola.confirmar("Confirmas la eliminacion?")) {
                System.out.println("Eliminacion cancelada.");
                return;
            }

            String sql = "DELETE FROM proveedor WHERE id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);

                int filas = ps.executeUpdate();

                if (filas == 1) {
                    correcto("Proveedor eliminado correctamente.");
                } else {
                    error("El proveedor ya no existe.");
                }
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }
}
