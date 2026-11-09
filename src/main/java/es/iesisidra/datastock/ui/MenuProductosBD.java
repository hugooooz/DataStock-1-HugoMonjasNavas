
package es.iesisidra.datastock.ui;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/** Operaciones CRUD de productos almacenados en MySQL. */
public final class MenuProductosBD {

    private MenuProductosBD() {
    }

    private static void correcto(String mensaje) {
        System.out.println(ColoresConsola.pintar(
                "[OK] " + mensaje, ColoresConsola.VERDE));
    }

    private static void error(String mensaje) {
        System.out.println(ColoresConsola.pintar(
                "[ERROR] " + mensaje, ColoresConsola.ROJO));
    }

    private static void errorSQL(SQLException e) {
        if (e.getErrorCode() == 1062) {
            error("Ya existe un producto con ese codigo.");
        } else if (e.getErrorCode() == 1451) {
            error("No se puede eliminar: el producto tiene movimientos registrados.");
        } else if (e.getErrorCode() == 1452) {
            error("La categoria o el proveedor no existe en MySQL.");
        } else {
            error("No se pudo realizar la operacion en MySQL: " + e.getMessage());
        }
    }

    private static String pedirCodigo() {
        String codigo;

        do {
            codigo = LectorConsola.leerTexto("Codigo (A-Z, 0-9, guion, maximo 12): ").trim().toUpperCase();

            if (!codigo.matches("[A-Z0-9-]{1,12}")) {
                error("El codigo debe tener entre 1 y 12 caracteres: A-Z, 0-9 o guion.");
            }

        } while (!codigo.matches("[A-Z0-9-]{1,12}"));

        return codigo;
    }

    private static String pedirNombre() {
        String nombre;

        do {
            nombre = LectorConsola.leerTexto(
                    "Nombre (3-100 caracteres): ").trim();

            if (nombre.length() < 3 || nombre.length() > 100) {
                error("El nombre debe tener entre 3 y 100 caracteres.");
            }

        } while (nombre.length() < 3 || nombre.length() > 100);

        return nombre;
    }


    private static BigDecimal convertirPrecio(String texto) {
        try {

            BigDecimal precio = new BigDecimal(texto.trim());

            if (precio.compareTo(BigDecimal.ZERO) > 0
                    && precio.scale() <= 2
                    && precio.precision() - precio.scale() <= 8) {
                return precio;

            }

        } catch (NumberFormatException e) {
            // El texto no contiene un numero valido.
        }


        return null;
    }

    private static BigDecimal pedirPrecio() {
        BigDecimal precio;

        do {
            String texto = LectorConsola.leerTexto("Precio (positivo, maximo 2 decimales, usa punto): ");

            precio = convertirPrecio(texto);

            if (precio == null) {
                error("Precio no valido. Ejemplos validos: 19.95 o 120.");
            }

        } while (precio == null);

        return precio;
    }

    private static int pedirStock() {
        int stock;

        do {
            stock = LectorConsola.leerEntero("Stock inicial (0 o mayor): ");

            if (stock < 0) {
                error("El stock no puede ser negativo.");
            }

        } while (stock < 0);

        return stock;
    }

    private static int pedirId(String nombre) {
        int id;

        do {
            id = LectorConsola.leerEntero("ID de " + nombre + ": ");

            if (id <= 0) {
                error("El ID debe ser mayor que cero.");
            }

        } while (id <= 0);

        return id;
    }

    private static boolean existe(Connection con, String tabla, int id)
            throws SQLException {

        // La tabla solo puede ser categoria o proveedor.
        String sql = "SELECT id FROM " + tabla + " WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        }
    }

    private static boolean comprobarReferencias(Connection con, int categoria, int proveedor) throws SQLException {

        if (!existe(con, "categoria", categoria)) {
            error("No existe la categoria con ID " + categoria + ".");
            return false;
        }

        if (!existe(con, "proveedor", proveedor)) {
            error("No existe el proveedor con ID " + proveedor + ".");
            return false;
        }

        return true;
    }

    private static void mostrarProducto(ResultSet rs)throws SQLException {

        System.out.println("Codigo: " + rs.getString("codigo"));
        System.out.println("Nombre: " + rs.getString("nombre"));
        System.out.println("Precio: " + rs.getBigDecimal("precio"));
        System.out.println("Stock: " + rs.getInt("stock"));

        System.out.println("Categoria: " + rs.getInt("categoria_id") + " - " + rs.getString("categoria_nombre"));

        System.out.println("Proveedor: " + rs.getInt("proveedor_id") + " - " + rs.getString("proveedor_nombre"));
    }

    private static final String CONSULTA_PRODUCTO =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, "
            + "p.proveedor_id, c.nombre AS categoria_nombre, "
            + "pr.nombre AS proveedor_nombre "
            + "FROM producto p "
            + "JOIN categoria c ON p.categoria_id = c.id "
            + "JOIN proveedor pr ON p.proveedor_id = pr.id "
            + "WHERE p.codigo = ?";

    public static void iniciar() {
        int opcion;


        do {
            System.out.println("\n===== MENU DE PRODUCTOS MYSQL =====");
            System.out.println("1. Dar de alta un producto");
            System.out.println("2. Consultar producto por codigo");
            System.out.println("3. Modificar un producto");
            System.out.println("4. Eliminar un producto");
            System.out.println("0. Volver al menu MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opcion: ", 0, 4);


            switch (opcion) {
                case 1 -> alta();
                case 2 -> consultar();
                case 3 -> modificar();
                case 4 -> eliminar();
                case 0 -> System.out.println("Volviendo al menu MySQL.");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }

        } while (opcion != 0);

    }

    private static void alta() {
        System.out.println("\n===== ALTA DE PRODUCTO =====");

        String codigo = pedirCodigo();
        String nombre = pedirNombre();
        BigDecimal precio = pedirPrecio();
        int stock = pedirStock();
        int categoria = pedirId("categoria");
        int proveedor = pedirId("proveedor");

        String sql = "INSERT INTO producto "
                + "(codigo, nombre, precio, stock, categoria_id, proveedor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar()) {

            if (!comprobarReferencias(con, categoria, proveedor)) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, codigo);
                ps.setString(2, nombre);
                ps.setBigDecimal(3, precio);
                ps.setInt(4, stock);
                ps.setInt(5, categoria);
                ps.setInt(6, proveedor);

                if (ps.executeUpdate() == 1) {
                    correcto("Producto " + codigo
                            + " guardado correctamente.");
                }
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void consultar() {
        System.out.println("\n===== CONSULTAR PRODUCTO =====");

        String codigo = pedirCodigo();

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(CONSULTA_PRODUCTO)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mostrarProducto(rs);
                } else {
                    System.out.println("No existe el producto "
                            + codigo + ".");
                }
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void modificar() {
        System.out.println("\n===== MODIFICAR PRODUCTO =====");

        String codigo = pedirCodigo();

        try (Connection con = ConexionBD.conectar()) {

            String nombre;
            BigDecimal precio;
            int categoria;
            int proveedor;

            try (PreparedStatement buscar =
                         con.prepareStatement(CONSULTA_PRODUCTO)) {

                buscar.setString(1, codigo);

                try (ResultSet rs = buscar.executeQuery()) {

                    if (!rs.next()) {
                        error("No existe el producto " + codigo + ".");
                        return;
                    }

                    mostrarProducto(rs);

                    nombre = rs.getString("nombre");
                    precio = rs.getBigDecimal("precio");
                    categoria = rs.getInt("categoria_id");
                    proveedor = rs.getInt("proveedor_id");
                }
            }

            System.out.println(
                    "Pulsa Intro para conservar cada dato actual.");

            String nuevoNombre = LectorConsola.leerTexto(
                    "Nuevo nombre: ").trim();

            if (!nuevoNombre.isEmpty()) {
                if (nuevoNombre.length() < 3
                        || nuevoNombre.length() > 100) {
                    error("El nombre debe tener entre 3 y 100 caracteres.");
                    return;
                }

                nombre = nuevoNombre;
            }

            String textoPrecio = LectorConsola.leerTexto(
                    "Nuevo precio (punto decimal): ").trim();

            if (!textoPrecio.isEmpty()) {
                BigDecimal nuevoPrecio = convertirPrecio(textoPrecio);

                if (nuevoPrecio == null) {
                    error("Precio no valido. Debe ser positivo "
                            + "y tener maximo 2 decimales.");
                    return;
                }

                precio = nuevoPrecio;
            }

            String textoCategoria = LectorConsola.leerTexto(
                    "Nuevo ID de categoria: ").trim();

            if (!textoCategoria.isEmpty()) {
                try {
                    int nuevoId = Integer.parseInt(textoCategoria);

                    if (nuevoId <= 0) {
                        error("El ID debe ser mayor que cero.");
                        return;
                    }

                    categoria = nuevoId;

                } catch (NumberFormatException e) {
                    error("El ID de categoria debe ser un entero.");
                    return;
                }
            }

            String textoProveedor = LectorConsola.leerTexto("Nuevo ID de proveedor: ").trim();

            if (!textoProveedor.isEmpty()) {
                try {
                    int nuevoId = Integer.parseInt(textoProveedor);

                    if (nuevoId <= 0) {
                        error("El ID debe ser mayor que cero.");
                        return;
                    }

                    proveedor = nuevoId;

                } catch (NumberFormatException e) {
                    error("El ID de proveedor debe ser un entero.");
                    return;
                }
            }

            System.out.println("El codigo y el stock no se pueden modificar aqui.");

            if (!comprobarReferencias(con, categoria, proveedor)) {
                return;
            }

            if (!LectorConsola.confirmar("Confirmas la modificacion?")) {
                System.out.println("Modificacion cancelada.");
                return;
            }

            String sql = "UPDATE producto SET nombre = ?, precio = ?, " + "categoria_id = ?, proveedor_id = ? WHERE codigo = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setBigDecimal(2, precio);
                ps.setInt(3, categoria);
                ps.setInt(4, proveedor);
                ps.setString(5, codigo);

                if (ps.executeUpdate() == 1) {
                    correcto("Producto modificado correctamente.");
                } else {
                    error("El producto ya no existe.");
                }
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }

    private static void eliminar() {
        System.out.println("\n===== ELIMINAR PRODUCTO =====");

        String codigo = pedirCodigo();

        try (Connection con = ConexionBD.conectar()) {

            try (PreparedStatement buscar = con.prepareStatement(CONSULTA_PRODUCTO)) {

                buscar.setString(1, codigo);

                try (ResultSet rs = buscar.executeQuery()) {
                    if (!rs.next()) {
                        error("No existe el producto " + codigo + ".");
                        return;
                    }

                    mostrarProducto(rs);
                }
            }

            if (!LectorConsola.confirmar("Confirmas la eliminacion?")) {
                System.out.println("Eliminacion cancelada.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement( "DELETE FROM producto WHERE codigo = ?")) {

                ps.setString(1, codigo);

                if (ps.executeUpdate() == 1) {
                    correcto("Producto eliminado correctamente.");
                } else {
                    error("El producto ya no existe.");
                }
            }

        } catch (SQLException e) {
            errorSQL(e);
        }
    }
}
