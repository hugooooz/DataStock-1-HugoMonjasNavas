package es.iesisidra.datastock.complementarias;

import java.io.File;
import java.math.BigDecimal;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.iesisidra.datastock.util.LectorConsola;


//rf07
public class XPathActividad {

    public static void main(String[] args) {


        System.out.println("===== CONSULTAS XPATH ======");
        String ruta = LectorConsola.leerTexto("Ruta del XML: ");

        if (ruta.trim().isEmpty()) {
            System.out.println("No has introducido ninguna ruta.");
            return;
        }

        File archivo = new File(ruta.trim());

        if (!archivo.exists() || !archivo.isFile() || !archivo.canRead()) {
            System.out.println("El fichero no existe o no se puede leer.");
            return;
        }




        try {
            
            
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();


            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            fabrica.setXIncludeAware(false);
            fabrica.setExpandEntityReferences(false);

            DocumentBuilder lector = fabrica.newDocumentBuilder();
            Document documento = lector.parse(archivo);

            if (!documento.getDocumentElement().getTagName().equals("catalogo")) {
                System.out.println("El XML no tiene la raiz catalogo");
                return;
            }

            XPath xpath = XPathFactory.newInstance().newXPath();
            int opcion = -1;




            while (opcion != 0) {

                System.out.println("\n===== MENU XPATH =====");
                System.out.println("1. Buscar por codigo");
                System.out.println("2. Buscar por categoria");
                System.out.println("3. Buscar por precio superior.");
                System.out.println("4. Contar productos");
                System.out.println("0. Salir");

                opcion = LectorConsola.leerOpcion("Opcion: ", 0, 4);


                switch (opcion) {

                    case 1: {

                        String codigo = LectorConsola.leerTexto("Codigo: ").trim();

                        if (codigo.isEmpty()) {
                            System.out.println("El codigo no puede estar vacio");
                        } else {

                            NodeList productos = (NodeList) xpath.evaluate("/catalogo/producto", documento, XPathConstants.NODESET);

                            int encontrados = 0;
                            for (int i = 0; i < productos.getLength(); i++) {



                                Element producto = (Element) productos.item(i);

                                if (leerCampo(producto, "codigo").equalsIgnoreCase(codigo)) {
                                    mostrarProducto(producto);
                                    encontrados++;
                                }
                            }

                            System.out.println("Productos encontrados: " + encontrados);


                        }

                        break;
                    }

                    case 2: {

                        int categoria = LectorConsola.leerEntero("Categoria: ");

                        if (categoria <= 0) {
                            System.out.println("La categoria debe ser positiva.");
                        } else {

                            String consulta = "/catalogo/producto[categoriaId=" + categoria + "]";
                            NodeList productos = (NodeList) xpath.evaluate( consulta, documento, XPathConstants.NODESET);
                            mostrarResultados(productos);
                        }

                        break;
                    }

                    case 3: {

                        String texto = LectorConsola.leerTexto("Precio minimo: ").trim();

                        try {

                            BigDecimal precio = new BigDecimal(texto);

                            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                                System.out.println("El precio no puede ser negativo.");


                            } else {

                                String consulta = "/catalogo/producto[precio > " + precio.toPlainString() + "]";
                                NodeList productos = (NodeList) xpath.evaluate(consulta, documento, XPathConstants.NODESET);
                                mostrarResultados(productos);
                            }

                        } catch (NumberFormatException e) {
                            System.out.println("Introduce un precio valido.");
                        }
                        break;
                    }

                    case 4: {

                        Double total = (Double) xpath.evaluate("count(/catalogo/producto)", documento, XPathConstants.NUMBER);
                        System.out.println("Total de poductos: " + total.intValue());
                        break;
                    }

                    case 0:
                        System.out.println("Saliendo de XPath.");
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error al consultar XML: " + e.getMessage());
        }
    }

    private static void mostrarResultados(NodeList productos) {
        for (int i = 0; i < productos.getLength(); i++) {
            Element producto = (Element) productos.item(i);
            mostrarProducto(producto);
        }

        System.out.println("Productos encontrados: " + productos.getLength());


    }

    private static void mostrarProducto(Element producto) {

        System.out.println("-------------------------------");
        System.out.println("Codigo: " + leerCampo(producto, "codigo"));
        System.out.println("nombre: " + leerCampo(producto, "nombre"));
        System.out.println("Precio: " + leerCampo(producto, "precio"));
        System.out.println("Categoria: " + leerCampo(producto, "categoriaId"));
    }

    // Leer el contenido de una etiqueta XML
    private static String leerCampo(Element producto, String campo) {

        NodeList datos = producto.getElementsByTagName(campo);
        if (datos.getLength() == 0) {
            return "";
        }

        return datos.item(0).getTextContent().trim();
    }
}
