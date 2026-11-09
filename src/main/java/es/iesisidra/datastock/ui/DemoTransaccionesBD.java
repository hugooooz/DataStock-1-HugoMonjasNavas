package es.iesisidra.datastock.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;


public final class DemoTransaccionesBD {

    
    private DemoTransaccionesBD() {
    }

    private static void correcto(String mensaje) {
        System.out.println(ColoresConsola.pintar("[OK] " + mensaje, ColoresConsola.VERDE));
    }

    private static void error(String mensaje) {
        System.out.println(ColoresConsola.pintar("[ERROR] " + mensaje, ColoresConsola.ROJO));
    }

    private static int pedirId(String mensaje) {
        int id;


        do {
            id = LectorConsola.leerEntero(mensaje);

            if (id <= 0) {
                error("El ID debe ser mayor que cero.");
            }

        } while (id <= 0);

        return id;

    }

    private static boolean existeCategoria(Connection con, int id)throws SQLException {

        String sql = "SELECT id FROM categoria WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void insertarCategoria(Connection con, int id, String nombre)throws SQLException {

        String sql = "INSERT INTO categoria(id, nombre) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, nombre);

            ps.executeUpdate();
        }

    }

    public static void iniciar() {

        System.out.println("\n===== DEMOSTRACION DE TRANSACCIONES =====");
        System.out.println("Vamos a probar COMMIT y ROLLBACK.");
        System.out.println("Se crearan categorias de prueba en MySQL.");
        System.out.println("Utiliza IDs nuevos que no tengan datos reales.");

        int id1 = pedirId("ID de prueba 1: ");
        int id2 = pedirId("ID de prueba 2: ");
        int id3 = pedirId("ID de prueba 3: ");

        if (id1 == id2 || id1 == id3 || id2 == id3) {
            error("Los tres IDs deben ser diferentes.");
            return;
        }

        try (Connection con = ConexionBD.conectar()) {

            // Comprobar que los IDs no estan siendo utilizados
            if (existeCategoria(con, id1) || existeCategoria(con, id2) || existeCategoria(con, id3)) {

                error("Alguno de los IDs ya existe en MySQL.");
                System.out.println("Elige otros IDs para la demostracion.");
                return;
            }

            System.out.println("\nESCENARIO A - COMMIT");
            System.out.println("Se insertaran dos categorias:");
            System.out.println(id1 + " - Prueba commit A");
            System.out.println(id2 + " - Prueba commit B");

            System.out.println("\nESCENARIO B - ROLLBACK");
            System.out.println("Se intentara insertar:");
            System.out.println(id3 + " - Prueba rollback");
            System.out.println("Despues se repetira el ID " + id1);
            System.out.println("El duplicado provocara un error esperado.");

            if (!LectorConsola.confirmar("Confirmas la demostracion?")) {

                System.out.println("Demostracion cancelada.");
                return;
            }

            boolean autoCommitOriginal = con.getAutoCommit();

            try {
                con.setAutoCommit(false);

                //COMMIT

                System.out.println("\n===== ESCENARIO A: COMMIT =====");

                try {
                    
                    insertarCategoria(con, id1, "Prueba commit A " + id1);
                    insertarCategoria(con, id2, "Prueba commit B " + id2);

                    con.commit();

                    correcto("COMMIT realizado correctamente.");

                } catch (SQLException e) {
                    con.rollback();
                    error("Fallo en el escenario COMMIT: " + e.getMessage());
                    return;
                }

                if (existeCategoria(con, id1) && existeCategoria(con, id2)) {

                    correcto("Las dos categorias existen en MySQL.");
                } else {
                    error("No se han encontrado las dos categorias.");
                }

                // ROLLBACK
                

                System.out.println("\n===== ESCENARIO B: ROLLBACK =====");

                try {

                    insertarCategoria(con, id3, "Prueba rollback " + id3);
                    System.out.println("Primera categoria insertada temporalmente.");

                    
                    // El duplicado provoca el error de forma intencionada
                    insertarCategoria(con, id1, "Prueba duplicada");

                    con.commit();

                    error("No se produjo el duplicado esperado.");

                } catch (SQLException e) {

                    con.rollback();

                    if (e.getErrorCode() == 1062) {
                        System.out.println("[AVISO] Error de clave duplicada esperado.");
                        correcto("ROLLBACK realizado correctamente.");
                    } else {
                        error("Error SQL diferente al esperado: " + e.getMessage());
                    }
                }

                // COMPROBACIONES

                System.out.println("\n===== COMPROBACION FINAL =====");

                boolean existe1 = existeCategoria(con, id1);
                boolean existe2 = existeCategoria(con, id2);
                boolean existe3 = existeCategoria(con, id3);

                if (existe1 && existe2 && !existe3) {

                    correcto("COMMIT correcto: las dos primeras existen.");
                    correcto("ROLLBACK correcto: la tercera no existe.");

                } else {

                    error("El resultado no coincide con lo esperado.");
                }

                System.out.println("\nIDs utilizados:");
                System.out.println("COMMIT: " + id1 + " y " + id2);
                System.out.println("ROLLBACK: " + id3);

                System.out.println("Las dos categorias del COMMIT siguen guardadas.");
                System.out.println("Puedes eliminarlas desde el menu de categorias " + "si no tienen productos asociados.");

            } finally {

                // Restaurar el comportamiento original de la conexion
                con.setAutoCommit(autoCommitOriginal);
            }

        } catch (SQLException e) {
            error("No se pudo completar la demostracion: " + e.getMessage());
        }


    }

}
