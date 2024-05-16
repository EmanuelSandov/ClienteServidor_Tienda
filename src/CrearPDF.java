import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class CrearPDF {

    public static void pdfcrear(String dest, HashMap<String, Integer> quantities, HashMap<String, Double> prices) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título
        Paragraph title = new Paragraph("Ticket de Compra")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(18);
        document.add(title);

        // Crear una tabla para mostrar los artículos comprados
        Table table = new Table(new float[]{1, 1, 1, 1}); // 4 columnas
        table.addCell("Producto");
        table.addCell("Cantidad");
        table.addCell("Precio Unitario");
        table.addCell("Subtotal");

        double total = 0;
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            if (entry.getValue() > 0) {
                double price = prices.get(entry.getKey());
                double subtotal = entry.getValue() * price;
                table.addCell(entry.getKey());
                table.addCell(String.valueOf(entry.getValue()));
                table.addCell("$" + price);
                table.addCell("$" + subtotal);
                total += subtotal;
            }
        }
        document.add(table);

        // Total
        Paragraph totalParagraph = new Paragraph("Total: $" + total)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(totalParagraph);

        // Pie de página
        Paragraph footer = new Paragraph("Muchas gracias por tu compra. ¡Te esperamos pronto!")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(footer);

        document.close();
    }
}
