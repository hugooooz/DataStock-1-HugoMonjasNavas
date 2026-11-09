
package es.iesisidra.datastock.ui;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/** Consultas de productos guardados en MySQL. */
public final class MenuConsultasBD {

    private MenuConsultasBD() {
    }

    private static final String SELECT_PRODUCTOS =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, "
            + "c.nombre AS categoria, pr.nombre AS proveedor "
            + "FROM producto p "
            + "JOIN categoria c ON p.categoria_id = c.id "
            + "JOIN proveedor pr ON p.proveedor_id = pr.id ";

    // Representa los datos de una fila obtenida de MySQL.
    private static class ProductoConsulta {

        String codigo;
        String nombre;
        BigDecimal precio;
        int stock;
        String categoria;
        String proveedor;

        ProductoConsulta(ResultSet rs) throws SQLException {
            codigo = rs.getString("codigo");
            nombre = rs.getString("nombre");
            precio = rs.getBigDecimal("precio");
            stock = rs.getInt("stock");
            categoria = rs.getString("categoria");
            proveedor = rs.getString("proveedor");
        }

        void mostrar() {
            System.out.println("----------------------------");
            System.out.println("Codigo: " + codigo);
            System.out.println("Nombre: " + nombre);
            System.out.println("Precio: " + precio);
            System.out.println("Stock: " + stock);
            System.out.println("Categoria: " + categoria);
            System.out.println("Proveedor: " + proveedor);
        }
    }

    private static void error(String mensaje) {
        System.out.println(ColoresConsola.pintar("[ERROR] " + mensaje, ColoresConsola.ROJO));
    }



    public static void iniciar() {
        int opcion;

        do {
            System.out.println("\n===== CONSULTAS MYSQL =====");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por codigo");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("4. Productos con stock inferior a un valor");
            System.out.println("0. Volver al menu MySQL");

            opcion = LectorConsola.leerOpcion(
                    "Selecciona una opcion: ", 0, 4);

            switch (opcion) {
                case 1 -> listar();
                case 2 -> buscarCodigo();
                case 3 -> buscarNombre();
                case 4 -> buscarStock();
                case 0 -> System.out.println("Volviendo al menu MySQL.");
            }

            if (opcion != 0) {
                LectorConsola.pausar();
            }

        } while (opcion != 0);
    }

    /**
     * Ejecuta un SELECT y convierte las filas en objetos Java.
     */
    private static void consultar(String sql, String parametro) {

        ArrayList<ProductoConsulta> productos = new ArrayList<>();

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (parametro != null) {
                ps.setString(1, parametro);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new ProductoConsulta(rs));
                }
            }

            mostrarResultados(productos);

        } catch (SQLException e) {
            error("No se pudo realizar la consulta: " + e.getMessage());
        }
    }

    private static void mostrarResultados(
            ArrayList<ProductoConsulta> productos) {

        if (productos.isEmpty()) {
            System.out.println("No se han encontrado productos.");
        } else {
            for (ProductoConsulta producto : productos) {
                producto.mostrar();
            }

            System.out.println("------------------------------");
            System.out.println("Total de productos: " + productos.size());
        }
    }

    private static void listar() {
        System.out.println("\n===== LISTADO DE PRODUCTOS =====");

        String sql = SELECT_PRODUCTOS + "ORDER BY p.codigo";

        consultar(sql, null);
    }

    private static void buscarCodigo() {
        System.out.println("\n==== BUSCAR POR CODIGO =====");

        String codigo = LectorConsola.leerTexto("Codigo del producto: ").trim().toUpperCase();

        if (codigo.isEmpty()) {
            error("El codigo no puede estar vacio.");
            return;
        }

        String sql = SELECT_PRODUCTOS + "WHERE p.codigo = ? ORDER BY p.codigo";

        consultar(sql, codigo);
    }

    private static void buscarNombre() {
        System.out.println("\n===== BUSCAR POR NOMBRE =====");

        String nombre = LectorConsola.leerTexto("Texto del nombre: ").trim();

        if (nombre.isEmpty()) {
            error("Debes escribir una parte del nombre.");
            return;
        }

        String sql = SELECT_PRODUCTOS + "WHERE p.nombre LIKE ? ORDER BY p.codigo";

        consultar(sql, "%" + nombre + "%");
    }

    private static void buscarStock() {
        System.out.println("\n===== STOCK INFERIOR A UN VALOR =====");

        int limite;

        do {
            limite = LectorConsola.leerEntero("Mostrar productos con stock menor que: ");

            if (limite < 0) {
                error("El limite no puede ser negativo.");
            }

        } while (limite < 0);

        ArrayList<ProductoConsulta> productos = new ArrayList<>();

        String sql = SELECT_PRODUCTOS + "WHERE p.stock < ? " + "ORDER BY p.stock, p.codigo";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new ProductoConsulta(rs));
                }
            }

            mostrarResultados(productos);

        } catch (SQLException e) {
            error("No se pudo consultar el stock: " + e.getMessage());
        }
    }
}
