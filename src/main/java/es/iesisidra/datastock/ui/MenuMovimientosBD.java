package es.iesisidra.datastock.ui;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/** Registra entradas y salidas de stock mediante MySQL */
public final class MenuMovimientosBD {

    private MenuMovimientosBD() {
    }

    private static void correcto(String mensaje) {
        System.out.println(ColoresConsola.pintar("[OK] " + mensaje, ColoresConsola.VERDE));
    }

    private static void error(String mensaje) {
        System.out.println(ColoresConsola.pintar("[ERROR] " + mensaje, ColoresConsola.ROJO));
    }

    public static void iniciar() {
        int opcion;

        do {
            System.out.println("\n===== MOVIMIENTOS DE STOCK =====");
            System.out.println("1. Registrar ENTRADA de stock");
            System.out.println("2. Registrar SALIDA de stock");
            System.out.println("0. Volver al menu MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opcion: ", 0, 2);

            switch (opcion) {
                case 1 -> registrarMovimiento("ENTRADA");
                case 2 -> registrarMovimiento("SALIDA");
                case 0 -> System.out.println("Volviendo al menu MySQL.");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }

        } while (opcion != 0);
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

    private static int pedirCantidad() {
        int cantidad;

        do {
            cantidad = LectorConsola.leerEntero("Cantidad de unidades: ");

            if (cantidad <= 0) {
                error("La cantidad debe ser mayor que cero.");
            }

        } while (cantidad <= 0);

        return cantidad;
    }

    private static void registrarMovimiento(String tipo) {
        System.out.println("\n===== REGISTRAR " + tipo + " =====");

        String codigo = pedirCodigo();

        try (Connection con = ConexionBD.conectar()) {

            String sqlProducto = "SELECT nombre, stock FROM producto WHERE codigo = ?";

            String nombre;
            int stockActual;

            try (PreparedStatement ps = con.prepareStatement(sqlProducto)) {

                ps.setString(1, codigo);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        error("No existe el producto " + codigo + ".");
                        return;
                    }

                    nombre = rs.getString("nombre");
                    stockActual = rs.getInt("stock");
                }
            }

            System.out.println("Producto: " + codigo + " - " + nombre);
            System.out.println("Stock actual: " + stockActual);

            int cantidad = pedirCantidad();

            System.out.println("Operacion: " + tipo);
            System.out.println("Cantidad: " + cantidad);

            if (!LectorConsola.confirmar("Confirmas el movimiento?")) {

                System.out.println("Movimiento cancelado.");
                return;
            }

            // MySQL actualiza el stock y registra el movimiento.
            String sqlMovimiento = "{CALL registrar_movimiento(?, ?, ?)}";

            try (CallableStatement cs = con.prepareCall(sqlMovimiento)) {

                cs.setString(1, codigo);
                cs.setString(2, tipo);
                cs.setInt(3, cantidad);

                cs.execute();
            }

            correcto("Movimiento registrado correctamente.");

            mostrarUltimoMovimiento(con, codigo);

        } catch (SQLException e) {
            if (e.getErrorCode() == 1644) {
                error(e.getMessage());
            } else {
                error("No se pudo registrar el movimiento: " + e.getMessage());
            }
        }
    }

    /**
     * Consulta el ultimo movimiento registrado para un producto
     */
    private static void mostrarUltimoMovimiento(
            Connection con, String codigo) throws SQLException {

        String sql = "SELECT tipo, cantidad, stock_anterior, "
                + "stock_posterior "
                + "FROM movimiento_stock "
                + "WHERE producto_codigo = ? "
                + "ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    System.out.println("----------------------------");
                    System.out.println("Tipo: " + rs.getString("tipo"));

                    System.out.println("Cantidad: " + rs.getInt("cantidad"));

                    System.out.println("Stock anterior: " + rs.getInt("stock_anterior"));

                    System.out.println("Stock posterior: " + rs.getInt("stock_posterior"));

                    System.out.println("-------------------------------");
                }
            }





        }
    }
}
