import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Catalogo extends JDialog {
    private HashMap<String, Integer> addedQuantities;
    private HashMap<String, Double> productPrices;
    private HashMap<String, Integer> stockQuantities;
    private JPanel contentPanel = new JPanel(new GridLayout(0, 1, 5, 5));
    private JButton closeButton = new JButton("Eliminar");
    private JLabel totalLabel = new JLabel("Total: $0.00");

    private String databaseFilePath;
    private InterfaceGrafica parentInterface;

    public Catalogo(JFrame parent, HashMap<String, Integer> quantities, HashMap<String, Double> prices, HashMap<String, Integer> stockQuantities, String dbFilePath) {
        super(parent, "Carrito de compras", true);
        this.addedQuantities = new HashMap<>(quantities); // Copiar las cantidades iniciales
        this.productPrices = prices;
        this.stockQuantities = stockQuantities;
        this.databaseFilePath = dbFilePath;
        if (parent instanceof InterfaceGrafica) {
            this.parentInterface = (InterfaceGrafica) parent;
        }
        setSize(300, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setupCartItems();
        setResizable(false);
        setupTotalAndButtons();
    }

    private void setupCartItems() {
        contentPanel.removeAll();  // Limpia el panel para evitar duplicados si se llama múltiples veces
        for (Map.Entry<String, Integer> entry : addedQuantities.entrySet()) {
            if (entry.getValue() == 0) {
                continue; // Omitir productos con cantidad 0
            }

            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            JLabel nameLabel = new JLabel(entry.getKey() + ": ");
            JTextField quantityField = new JTextField(entry.getValue().toString(), 3);
            quantityField.setPreferredSize(new Dimension(30, 30));
            quantityField.setHorizontalAlignment(JTextField.CENTER);

            JButton increaseButton = new JButton("+");
            increaseButton.addActionListener(e -> {
                int currentValue = Integer.parseInt(quantityField.getText());
                if (currentValue < stockQuantities.get(entry.getKey())) {
                    quantityField.setText(String.valueOf(currentValue + 1));
                    addedQuantities.put(entry.getKey(), currentValue + 1);
                    updateTotal();
                } else {
                    JOptionPane.showMessageDialog(this, "No se puede añadir más de la cantidad en existencia", "Cantidad excedida", JOptionPane.WARNING_MESSAGE);
                }
            });

            JButton decreaseButton = new JButton("-");
            decreaseButton.addActionListener(e -> {
                int currentValue = Integer.parseInt(quantityField.getText());
                if (currentValue > 1) { // Cambiado a > 1 para que no baje a 0
                    quantityField.setText(String.valueOf(currentValue - 1));
                    addedQuantities.put(entry.getKey(), currentValue - 1);
                    updateTotal();
                }
            });

            itemPanel.add(nameLabel);
            itemPanel.add(quantityField);
            itemPanel.add(increaseButton);
            itemPanel.add(decreaseButton);
            contentPanel.add(itemPanel);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);  // Añade el scrollPane en el centro del BorderLayout

        revalidate();
        repaint();
    }

    private void removeProductFromCart(String productKey) {
        addedQuantities.remove(productKey);  // Eliminar producto del mapa de cantidades
        setupCartItems();  // Refrescar el carrito después de eliminar un producto
        revalidate();
        repaint();
    }

    private void setupTotalAndButtons() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        totalLabel = new JLabel("Total: $0.00");
        updateTotal();

        JButton payButton = new JButton("Pagar");
        payButton.addActionListener(e -> {
            if (isCartEmpty()) {
                JOptionPane.showMessageDialog(this, "El carrito está vacío. Agregue productos antes de pagar.", "Carrito vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                for (Map.Entry<String, Integer> entry : addedQuantities.entrySet()) {
                    SQLiteBase.updateProductQuantity(databaseFilePath, "Tienda", entry.getKey(), entry.getValue());
                }
                CrearPDF.pdfcrear("ticket.pdf", addedQuantities, productPrices);
                JOptionPane.showMessageDialog(null, "Compra realizada exitosamente y ticket generado.");

                // Enviar base de datos al servidor
                sendDatabaseToServer();

                addedQuantities.clear(); // Limpiar el carrito
                setupCartItems(); // Actualizar la interfaz del carrito
                updateTotal(); // Actualizar el total a $0.00
                if (parentInterface != null) {
                    parentInterface.updateQuantities(addedQuantities);
                    parentInterface.updateInterface(); // Actualiza la interfaz principal
                }
                setVisible(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> {
            if (isCartEmpty()) {
                JOptionPane.showMessageDialog(this, "El carrito está vacío. No hay nada para eliminar.", "Carrito vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Restablecer todas las cantidades a 0
            for (Map.Entry<String, Integer> entry : addedQuantities.entrySet()) {
                addedQuantities.put(entry.getKey(), 0);
            }
            updateTotal();
            setupCartItems(); // Refrescar la interfaz del carrito

            if (parentInterface != null) {
                parentInterface.updateQuantities(addedQuantities);
                parentInterface.updateInterface(); // Actualiza la interfaz principal
            }
            setVisible(false);
        });

        bottomPanel.add(totalLabel);
        bottomPanel.add(payButton);
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private boolean isCartEmpty() {
        for (int quantity : addedQuantities.values()) {
            if (quantity > 0) {
                return false;
            }
        }
        return true;
    }

    private void updateTotal() {
        double total = 0;
        for (Map.Entry<String, Integer> entry : addedQuantities.entrySet()) {
            Double price = productPrices.get(entry.getKey());
            if (price != null) {
                total += price * entry.getValue();
            }
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void sendDatabaseToServer() {
        try (Socket socket = new Socket("127.0.0.1", 6040);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(databaseFilePath)) {

            // Enviar el tipo de conexión al servidor
            dos.writeUTF("CONECCION3");

            // Enviar el nombre del archivo
            dos.writeUTF(new File(databaseFilePath).getName());

            // Enviar el tamaño del archivo
            long fileSize = new File(databaseFilePath).length();
            dos.writeLong(fileSize);

            // Enviar el contenido del archivo
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            System.out.println("Base de datos enviada: " + databaseFilePath);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
