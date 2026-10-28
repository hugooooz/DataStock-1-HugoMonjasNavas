package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.iesisidra.datastock.modelo.Producto;




public class ImportadorXML {

    public static void importar(String ruta, Inventario inventario) {

        if (ruta.trim().isEmpty()) {
            System.out.println("No has introducido ninguna ruta.");
            return;
        }



        File archivo = new File(ruta.trim());

        if (!archivo.exists()) {
            System.out.println("El fichero no existe.");
            return;
        }

        if (!archivo.isFile()) {
            System.out.println("La ruta no es un fichero.");
            return;
        }

        if (!archivo.canRead()) {
            System.out.println("No se puede leer el fichero.");
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
            Element raiz = documento.getDocumentElement();


            if (raiz == null || !raiz.getTagName().equals("catalogo")) {
                System.out.println("El XML no tiene la raiz catalogo.");
                return;
            }

            NodeList productos = raiz.getElementsByTagName("producto");
            int importados = 0;
            int rechazados = 0;
            System.out.println("Productos encontrados: " + productos.getLength());



            for (int i = 0; i < productos.getLength(); i++) {

                try {

                    Element producto = (Element) productos.item(i);

                    String codigo = leerCampo(producto, "codigo").trim().toUpperCase();
                    String nombre = leerCampo(producto, "nombre").trim();


                    BigDecimal precio = new BigDecimal(leerCampo(producto, "precio"));

                    int stock = Integer.parseInt(leerCampo(producto, "stock"));
                    int categoriaId = Integer.parseInt(leerCampo(producto, "categoriaId"));
                    int proveedorId = Integer.parseInt(leerCampo(producto, "proveedorId"));



                    if (codigo.isEmpty() || codigo.length() > 12 || !codigo.matches("[A-Z0-9-]+")) {
                        throw new IllegalArgumentException("Codigo incorrecto.");
                    }

                    if (nombre.length() < 3 || nombre.length() > 100) {
                        throw new IllegalArgumentException("Nombre incorrecto.");
                    }
                    if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("El precio debe ser mayor que 0.");
                    }

                    if (stock < 0) {
                        throw new IllegalArgumentException("El stock no puede ser negativo.");
                    }


                    if (categoriaId <= 0 || proveedorId <= 0) {
                        throw new IllegalArgumentException("Categoria o proveeedor incorrecto.");
                    }

                    boolean duplicado = false;




                    for (Producto existente : inventario.getProductos()) {

                        if (existente.getCodigo().equalsIgnoreCase(codigo)) {
                            duplicado = true;
                        }

                    }

                    if (duplicado) {
                        throw new IllegalArgumentException("Codigo duplicado: " + codigo);
                    }

                    Producto nuevo = new Producto(
                            codigo,
                            nombre,
                            precio,
                            stock,
                            categoriaId,
                            proveedorId
                    );

                    inventario.getProductos().add(nuevo);
                    importados++;

                } catch (RuntimeException e) {

                    rechazados++;

                    System.out.println("Producto " + (i + 1) + " rechazado: " + e.getMessage());
                }




            }

            System.out.println("-=----------------------------------=-");
            System.out.println("Prroductos encontrados: " + productos.getLength());
            System.out.println("Productos importados: " + importados);
            System.out.println("Prodctos rechazados: " + rechazados);
            System.out.println("Importacion finalizada.");

        } catch (ParserConfigurationException | SAXException | IOException e) {

            System.out.println("Error al leer el XML: " + e.getMessage());

        } catch (SecurityException | IllegalArgumentException e) {

            System.out.println("No se puede procesar el XML: " + e.getMessage());

        }
    }



    private static String leerCampo(Element producto, String campo) {

        NodeList datos = producto.getElementsByTagName(campo);
        if (datos.getLength() == 0) {
            throw new IllegalArgumentException("Falta el campo: " + campo);
        }

        return datos.item(0).getTextContent().trim();
    }


}
