
package es.iesisidra.datastock.servicio;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;



public class ConsultarProductos {

    public static void listar(Inventario inventario) {

        if (inventario.getProductos().isEmpty()) {
            System.out.println("El inventario esta vacio.");
            return;
        }

        List<Producto> productos = new ArrayList<>(inventario.getProductos());
        productos.sort(Comparator.comparing(Producto::getCodigo));
        System.out.println("===== LISTA DE PRODUCTOS =====");

        for (Producto producto : productos) {
            mostrarProducto(producto);
        }
    }


    public static void buscarCodigo(Inventario inventario, String codigo) {

        codigo = codigo.trim();

        if (codigo.isEmpty()) {
            System.out.println("No has introducido ningun codigo.");
            return;
        }

        for (Producto producto : inventario.getProductos()) {


            if (producto.getCodigo().equalsIgnoreCase(codigo)) {


                System.out.println("Producto encontrado:");
                mostrarProducto(producto);
                return;
            }
        }

        System.out.println("No existe ningun producto con ese codigo.");


    }

    public static void buscarNombre(Inventario inventario, String nombre) {


        nombre = nombre.trim();

        if (nombre.isEmpty()) {
            System.out.println("No has introducido ningun nombre.");

            return;
        }

        int encontrados = 0;



        for (Producto producto : inventario.getProductos()) {

            if (producto.getNombre().toLowerCase().contains(nombre.toLowerCase())) {

                mostrarProducto(producto);
                encontrados++;
            }
        }



        if (encontrados == 0) {
            System.out.println("No se han encontrado productos con ese nombre.");
        } else {
            System.out.println("Productos encontrados: " + encontrados);
        }
    }

    private static void mostrarProducto(Producto producto) {

        System.out.println("------------------------------------");
        System.out.println("Codigo: " + producto.getCodigo());
        System.out.println("Nombre: " + producto.getNombre());
        System.out.println("Precio: " + producto.getPrecio() + " euros");
        System.out.println("Stock: " + producto.getStock());
        System.out.println("Categoria: " + producto.getCategoriaId());
        System.out.println("Proveedor: " + producto.getProveedorId());
        System.out.println("----------------------------------------");
    }
}
