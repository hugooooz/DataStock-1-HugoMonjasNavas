package es.iesisidra.datastock.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/** Copia productos de la memoria de UT1 a MySQL. */
public final class ImportadorMemoriaBD {

    private ImportadorMemoriaBD() {
    }

    private static void correcto(String mensaje) {
        System.out.println(ColoresConsola.pintar("[OK] " + mensaje, ColoresConsola.VERDE));
    }

    private static void error(String mensaje) {
        System.out.println(ColoresConsola.pintar("[ERROR] " + mensaje, ColoresConsola.ROJO));
    }

    public static void importar(Inventario inventario) {

        System.out.println("\n===== IMPORTAR MEMORIA A MYSQL =====");

        int total = inventario.getProductos().size();


        System.out.println("Productos en memoria: " + total);

        if (total == 0) {
            System.out.println("No hay productos para importar.");
            return;

        }

        if (!LectorConsola.confirmar( "Quieres copiar los productos a MySQL?")) {
            System.out.println("Importacion cancelada.");
            return;
        }

        int insertados = 0;
        int duplicados = 0;
        int rechazados = 0;

        try (Connection con = ConexionBD.conectar()) {

            // Una sola transaccion para todo el lote.
            con.setAutoCommit(false);

            String sqlInsertar = "INSERT INTO producto "
                    + "(codigo, nombre, precio, stock, "
                    + "categoria_id, proveedor_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try {
                try (PreparedStatement insertar = con.prepareStatement(sqlInsertar)) {

                    for (Producto producto : inventario.getProductos()) {

                        if (producto == null) {
                            error("El producto esta vacio.");
                            rechazados++;
                            continue;
                        }

                        String codigo = producto.getCodigo();

                        if (codigo == null) {
                            codigo = "";
                        }

                        codigo = codigo.trim().toUpperCase();

                        System.out.println("\nProducto: " + codigo);
                        String motivo = validarProducto(producto);

                        if (motivo != null) {
                            error(motivo);
                            rechazados++;
                            continue;
                        }

                        if (existeCodigo(con, codigo)) {
                            System.out.println("[AVISO] Producto duplicado. Se omite.");
                            duplicados++;
                            continue;
                        }

                        if (!existeId(con, "categoria", producto.getCategoriaId())) {

                            error("No existe la categoria con ID " + producto.getCategoriaId() + ".");

                            rechazados++;
                            continue;
                        }

                        if (!existeId(con, "proveedor", producto.getProveedorId())) {

                            error("No existe el proveedor con ID " + producto.getProveedorId() + ".");

                            rechazados++;
                            continue;
                        }

                        insertar.setString(1, codigo);
                        insertar.setString(2, producto.getNombre().trim());
                        insertar.setBigDecimal(3, producto.getPrecio());
                        insertar.setInt(4, producto.getStock());
                        insertar.setInt(5, producto.getCategoriaId());
                        insertar.setInt(6, producto.getProveedorId());

                        int filas = insertar.executeUpdate();

                        if (filas != 1) {
                            throw new SQLException("No se pudo insertar " + codigo);
                        }
                      
                        insertados++;

                        correcto("Producto " + codigo + " preparado para guardar.");
                    }
                }


                con.commit();

                System.out.println("\n===== RESUMEN DE IMPORTACION =====");
                correcto("Transaccion confirmada.");
                System.out.println("Insertados: " + insertados);
                System.out.println("Omitidos por duplicado: " + duplicados);
                System.out.println("Rechazados: " + rechazados);
                System.out.println("Productos en memoria: " + inventario.getProductos().size());

            } catch (SQLException e) {

                try {
                    con.rollback();
                    System.out.println("[AVISO] Se ha ejecutado ROLLBACK.");
                } catch (SQLException errorRollback) {
                    error("No se pudo confirmar el resultado del rollback.");
                }


                error("Fallo SQL durante la importacion: " + e.getMessage());

                System.out.println("No se ha confirmado ninguna insercion del lote.");
            }


        } catch (SQLException e) {
            error("No se pudo conectar o preparar la transaccion: " + e.getMessage());
        }

        System.out.println("El inventario en memoria no se ha modificado.");
    }

    /**
     * Comprueba las reglas del producto antes de insertarlo.
     * Devuelve null si el producto es valido.
     */
    private static String validarProducto(Producto producto) {

        if (producto == null) {

            return "El producto esta vacio.";
        }

        String codigo = producto.getCodigo();

        if (codigo == null) {
            return "Falta el codigo del producto.";
        }

        codigo = codigo.trim().toUpperCase();

        if (!codigo.matches("[A-Z0-9-]{1,12}")) {
            return "Codigo no valido: " + codigo;
        }

        String nombre = producto.getNombre();

        if (nombre == null || nombre.trim().length() < 3 || nombre.trim().length() > 100) {

            return "El nombre debe tener entre 3 y 100 caracteres.";
        }

        BigDecimal precio = producto.getPrecio();

        if (precio == null) {
            return "El precio es obligatorio.";
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            return "El precio debe ser mayor que cero.";
        }

        if (precio.scale() > 2 || precio.precision() - precio.scale() > 8) {

            return "El precio no cumple DECIMAL(10,2).";
        }

        if (producto.getStock() < 0) {
            return "El stock no puede ser negativo.";
        }

        if (producto.getCategoriaId() <= 0) {
            return "El ID de categoria debe ser positivo.";
        }

        if (producto.getProveedorId() <= 0) {
            return "El ID de proveedor debe ser positivo.";
        }

        return null;
    }

    private static boolean existeCodigo(Connection con, String codigo)throws SQLException {

        String sql = "SELECT codigo FROM producto WHERE codigo = ?";


        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean existeId(Connection con, String tabla, int id)throws SQLException {

        // El nombre de la tabla se decide aqui
        String sql;

        if (tabla.equals("categoria")) {
            sql = "SELECT id FROM categoria WHERE id = ?";
        } else {
            sql = "SELECT id FROM proveedor WHERE id = ?";
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

            
        }
    }
}
