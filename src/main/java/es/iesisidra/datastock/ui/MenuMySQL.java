
package es.iesisidra.datastock.ui;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.servicio.ImportadorMemoriaBD;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

public final class MenuMySQL {

    private MenuMySQL() {
    }

    public static void iniciar(Inventario inventario) {
        int opcion;

        do {
            System.out.println("\n===== GESTION MYSQL - UT2 =====");
            System.out.println("1. Comprobar conexion y esquema");
            System.out.println("2. Gestionar categorias");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostracion de commit y rollback");
            System.out.println("0. Volver al menu principal");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opcion: ", 0, 9);

            switch (opcion) {
                case 1 -> {
                    comprobarConexion();
                    LectorConsola.pausar();
                }
                case 2 -> MenuCatalogos.categorias();
                case 3 -> MenuCatalogos.proveedores();
                case 4 -> MenuProductosBD.iniciar();
                case 5 -> MenuConsultasBD.iniciar();
                case 6 -> MenuMovimientosBD.iniciar();
                case 7 -> MenuHistorialBD.iniciar();
                case 8 -> {
                    ImportadorMemoriaBD.importar(inventario);
                    LectorConsola.pausar();
                }
                case 9 -> {
                    DemoTransaccionesBD.iniciar();
                    LectorConsola.pausar();
                }
                case 0 -> System.out.println("Volviendo al menu principal.");
            }

        } while (opcion != 0);
    }

    /** Comprueba la conexion y las tablas obligatorias de MySQL. */
    private static void comprobarConexion() {
        System.out.println("\n===== COMPROBAR CONEXION =====");

        try (Connection conexion = ConexionBD.conectar()) {

            DatabaseMetaData datos = conexion.getMetaData();

            System.out.println(ColoresConsola.pintar(
                    "[OK] Conexion establecida correctamente.",
                    ColoresConsola.VERDE));

            System.out.println("Base de datos: " + conexion.getCatalog());
            System.out.println("Gestor: "
                    + datos.getDatabaseProductName());
            System.out.println("Version: "
                    + datos.getDatabaseProductVersion());

            String[] tablas = {
                "categoria",
                "proveedor",
                "producto",
                "movimiento_stock"
            };

            boolean esquemaCorrecto = true;

            for (String tabla : tablas) {
                try (ResultSet resultado = datos.getTables(
                        conexion.getCatalog(),
                        null,
                        tabla,
                        new String[]{"TABLE"})) {

                    if (!resultado.next()) {
                        System.out.println(ColoresConsola.pintar(
                                "[ERROR] Falta la tabla: " + tabla,
                                ColoresConsola.ROJO));

                        esquemaCorrecto = false;
                    }
                }
            }

            if (esquemaCorrecto) {

                String sql = "SELECT COUNT(*) FROM producto";

                try (PreparedStatement consulta =
                             conexion.prepareStatement(sql);
                     ResultSet resultado = consulta.executeQuery()) {

                    if (resultado.next()) {
                        System.out.println("Productos almacenados: "
                                + resultado.getInt(1));
                    }
                }

                System.out.println(ColoresConsola.pintar(
                        "[OK] Esquema comprobado.",
                        ColoresConsola.VERDE));
            }

        } catch (SQLException e) {

            System.out.println(ColoresConsola.pintar(
                    "[ERROR] No se pudo comprobar la base de datos.",
                    ColoresConsola.ROJO));

            System.out.println(
                    "Revisa MySQL Server, config.properties "
                    + "y el esquema datastock.");

            System.out.println("Detalle: " + e.getMessage());
        }
    }
}
