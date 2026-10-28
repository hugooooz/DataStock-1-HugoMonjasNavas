
package es.iesisidra.datastock.servicio;

public class ConsultarProductos {

    public static void listar(Inventario inventario) {

        if (inventario.getProductos().isEmpty()) {
            System.out.println("El inventario esta vacio.");
            return;
        }

        
    }
}
