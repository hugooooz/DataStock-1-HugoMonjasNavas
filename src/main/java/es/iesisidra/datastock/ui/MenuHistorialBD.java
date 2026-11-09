package es.iesisidra.datastock.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/** Permite consultar el historial de movimientos de stock. */
public final class MenuHistorialBD {

    private MenuHistorialBD() {
    }

    private static void error(String mensaje) {
        System.out.println(ColoresConsola.pintar("[ERROR] " + mensaje, ColoresConsola.ROJO));
    }

    public static void iniciar() {
        int opcion;

        do {
            System.out.println("\n===== HISTORIAL DE MOVIMIENTOS =====");
            System.out.println("1. Consultar todos los movimientos");
            System.out.println("2. Consultar movimientos de un producto");
            System.out.println("0. Volver al menu MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opcion: ", 0, 2);

            switch (opcion) {
                case 1 -> consultarTodos();
                case 2 -> consultarPorProducto();
                case 0 -> System.out.println("Volviendo al menu MySQL.");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }

        } while (opcion != 0);

    }

    /**
     * Muestra los datos de un movimiento obtenido de MySQL.
     */
    private static void mostrarMovimiento(ResultSet rs)throws SQLException {

        long id = rs.getLong("id");
        Timestamp fecha = rs.getTimestamp("fecha");
        String codigo = rs.getString("producto_codigo");
        String tipo = rs.getString("tipo");
        int cantidad = rs.getInt("cantidad");
        int anterior = rs.getInt("stock_anterior");
        int posterior = rs.getInt("stock_posterior");

        System.out.println("---------------------------------");
        System.out.println("ID movimiento: " + id);
        System.out.println("Fecha: " + fecha);
        System.out.println("Producto: " + codigo);
        System.out.println("Tipo: " + tipo);
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Stock anterior: " + anterior);
        System.out.println("Stock posterior: " + posterior);
    }

    private static void consultarTodos() {
        System.out.println("\n===== HISTORIAL COMPLETO =====");

        String sql = "SELECT id, fecha, producto_codigo, tipo, "
                + "cantidad, stock_anterior, stock_posterior "
                + "FROM movimiento_stock "
                + "ORDER BY fecha ASC, id ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int total = 0;

            while (rs.next()) {
                mostrarMovimiento(rs);
                total++;
            }

            if (total == 0) {
                System.out.println("No hay movimientos registrados.");
            } else {
                System.out.println("-----------------------------------");
                System.out.println("Total de movimientos: " + total);
            }

        } catch (SQLException e) {
            error("No se pudo consultar el historial: " + e.getMessage());
        }
    }

    private static String pedirCodigo() {
        String codigo;

        do {
            codigo = LectorConsola.leerTexto("Codigo del producto: ").trim().toUpperCase();

            if (!codigo.matches("[A-Z0-9-]{1,12}")) {
                error("Codigo no valido. Usa letras, numeros o guion.");
            }

        } while (!codigo.matches("[A-Z0-9-]{1,12}"));

        return codigo;
    }

    private static boolean existeProducto(Connection con, String codigo)throws SQLException {

        String sql = "SELECT codigo FROM producto WHERE codigo = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void consultarPorProducto() {
        System.out.println("\n===== HISTORIAL POR PRODUCTO =====");


        String codigo = pedirCodigo();

        String sql = "SELECT id, fecha, producto_codigo, tipo, "
                + "cantidad, stock_anterior, stock_posterior "
                + "FROM movimiento_stock "
                + "WHERE producto_codigo = ? "
                + "ORDER BY fecha ASC, id ASC";

        try (Connection con = ConexionBD.conectar()) {

            if (!existeProducto(con, codigo)) {
                error("No existe el producto " + codigo + ".");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, codigo);

                try (ResultSet rs = ps.executeQuery()) {

                    int total = 0;

                    while (rs.next()) {
                        mostrarMovimiento(rs);
                        total++;
                    }

                    if (total == 0) {
                        System.out.println("Sin movimientos registrados para " + codigo + ".");
                    } else {
                        System.out.println("-------------------------------");
                        System.out.println("Total de movimientos: " + total);
                    }
                    
                }
            }

        } catch (SQLException e) {
            error("No se pudo consultar el historial: " + e.getMessage());
        }
    }


}
