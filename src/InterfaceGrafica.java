import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class InterfaceGrafica extends JFrame {
    // HashMap para almacenar las cantidades añadidas
    private HashMap<String, Integer> addedQuantities;
    private HashMap<String, Integer> tempQuantities;
    private HashMap<String, Double> productPrices = new HashMap<>();
    private HashMap<String, Integer> stockQuantities = new HashMap<>();
    private String databaseFilePath;

    public InterfaceGrafica(String databaseFilePath) {
        this.databaseFilePath = databaseFilePath;
        setTitle("Tienda de animales exóticos");
        setSize(1000, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        addedQuantities = new HashMap<>();
        tempQuantities = new HashMap<>();

        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Tienda de aves exóticos", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Boton de compras
        JButton cartButton = new JButton("Carrito");
        cartButton.addActionListener(event -> {
            Catalogo cartDialog = new Catalogo(this, addedQuantities, productPrices, stockQuantities, databaseFilePath);
            cartDialog.setVisible(true);
        });

        // Botón de recargar
        JButton recarButton = new JButton("Recargar");
        recarButton.addActionListener(event -> {
            enviarCambiosAlCliente();
            updateInterface();
        });

        titlePanel.add(recarButton, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(cartButton, BorderLayout.EAST);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath)) {
            String query = "SELECT Producto, Cantidad, Imagen, Precio FROM Tienda";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                boolean isFirst = true;
                while (rs.next()) {
                    if (!isFirst) {
                        mainPanel.add(Box.createHorizontalStrut(20)); // Añadir espacio entre columnas
                    }
                    JPanel productPanel = new JPanel();
                    productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
                    productPanel.setAlignmentY(Component.TOP_ALIGNMENT); // Alinear contenido en la parte superior

                    String nombreProducto = rs.getString("Producto");
                    int cantidad = rs.getInt("Cantidad");
                    String imagePath = rs.getString("Imagen");
                    double precio = rs.getDouble("Precio");
                    productPrices.put(nombreProducto, precio);  // Almacenar el precio en el HashMap
                    stockQuantities.put(nombreProducto, cantidad);  // Almacenar la cantidad en stock en el HashMap
                    tempQuantities.put(nombreProducto, 0);  // Inicializar las cantidades temporales

                    JLabel nameLabel = new JLabel(nombreProducto);
                    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productPanel.add(nameLabel);

                    try {
                        BufferedImage img = ImageIO.read(new File(imagePath));
                        ImageIcon icon = new ImageIcon(img.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
                        JLabel imageLabel = new JLabel(icon);
                        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        productPanel.add(imageLabel);
                    } catch (IOException e) {
                        JLabel errorLabel = new JLabel("Imagen no disponible");
                        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        productPanel.add(errorLabel);
                    }

                    JLabel priceLabel = new JLabel(String.format("Precio: $%.2f", precio));
                    priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productPanel.add(priceLabel);

                    JLabel quantityLabel = new JLabel("Existencia: " + cantidad);
                    quantityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productPanel.add(quantityLabel);

                    JPanel buttonPanel = new JPanel();
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

                    JLabel addedQuantityLabel = new JLabel("Añadido: 0");
                    addedQuantityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productPanel.add(addedQuantityLabel);

                    // Botón para incrementar la cantidad
                    JButton increaseButton = new JButton("+");
                    increaseButton.addActionListener(event -> {
                        int currentAdded = Integer.parseInt(addedQuantityLabel.getText().split(": ")[1]);
                        if (currentAdded < stockQuantities.get(nombreProducto)) {
                            addedQuantityLabel.setText("Añadido: " + (currentAdded + 1));
                            tempQuantities.put(nombreProducto, currentAdded + 1); // Actualizar HashMap temporal
                        } else {
                            JOptionPane.showMessageDialog(this, "No se puede añadir más de la cantidad en existencia", "Cantidad excedida", JOptionPane.WARNING_MESSAGE);
                        }
                    });
                    buttonPanel.add(increaseButton);

                    // Botón para decrementar la cantidad
                    JButton decreaseButton = new JButton("-");
                    decreaseButton.addActionListener(event -> {
                        int currentAdded = Integer.parseInt(addedQuantityLabel.getText().split(": ")[1]);
                        if (currentAdded > 0) {
                            addedQuantityLabel.setText("Añadido: " + (currentAdded - 1));
                            tempQuantities.put(nombreProducto, currentAdded - 1); // Actualizar HashMap temporal
                        }
                    });
                    buttonPanel.add(decreaseButton);

                    // Botón "Añadir" para guardar la cantidad
                    JButton addButton = new JButton("Añadir");
                    addButton.addActionListener(event -> {
                        int addedAmount = tempQuantities.get(nombreProducto);
                        if (addedAmount == 0) {
                            JOptionPane.showMessageDialog(this, "Agrega una cantidad mayor a 0 para añadir al carrito", "Cantidad inválida", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        addedQuantities.put(nombreProducto, addedAmount);
                        JOptionPane.showMessageDialog(this, "Cantidad añadida: " + addedAmount + " para " + nombreProducto);
                    });
                    buttonPanel.add(addButton);

                    productPanel.add(buttonPanel);
                    mainPanel.add(productPanel);
                    isFirst = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setPreferredSize(new Dimension(750, 550));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    public HashMap<String, Integer> getStockQuantities() {
        return stockQuantities;
    }

    public void updateInterface() {
        getContentPane().removeAll(); // Limpiar el contenido actual

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath)) {
            String query = "SELECT Producto, Cantidad, Imagen, Precio FROM Tienda";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                boolean isFirst = true;
                while (rs.next()) {
                    if (!isFirst) {
                        mainPanel.add(Box.createHorizontalStrut(20)); // Añadir espacio entre columnas
                    }
                    JPanel productPanel = new JPanel();
                    productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
                    productPanel.setAlignmentY(Component.TOP_ALIGNMENT); // Alinear contenido en la parte superior

                    String nombreProducto = rs.getString("Producto");
                    int cantidad = rs.getInt("Cantidad");
                    String imagePath = rs.getString("Imagen");
                    double precio = rs.getDouble("Precio");
                    productPrices.put(nombreProducto, precio);  // Almacenar el precio en el HashMap
                    stockQuantities.put(nombreProducto, cantidad);  // Almacenar la cantidad en stock en el HashMap
                    tempQuantities.put(nombreProducto, 0);  // Inicializar las cantidades temporales

                    JLabel nameLabel = new JLabel(nombreProducto);
                    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productPanel.add(nameLabel);

                    try {
                        BufferedImage img = ImageIO.read(new File(imagePath));
                        ImageIcon icon = new ImageIcon(img.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
                        JLabel imageLabel = new JLabel(icon);
                        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        productPanel.add(imageLabel);
                    } catch (IOException e) {
                        JLabel errorLabel = new JLabel("Imagen no disponible");
                        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        productPanel.add(errorLabel);
                    }

                    JLabel priceLabel = new JLabel(String.format("Precio: $%.2f", precio));
                    priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productPanel.add(priceLabel);

                    JLabel quantityLabel = new JLabel("Existencia: " + cantidad);
                    quantityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productPanel.add(quantityLabel);

                    JPanel buttonPanel = new JPanel();
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

                    JLabel addedQuantityLabel = new JLabel("Añadido: 0");
                    addedQuantityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productPanel.add(addedQuantityLabel);

                    // Botón para incrementar la cantidad
                    JButton increaseButton = new JButton("+");
                    increaseButton.addActionListener(event -> {
                        int currentAdded = Integer.parseInt(addedQuantityLabel.getText().split(": ")[1]);
                        if (currentAdded < stockQuantities.get(nombreProducto)) {
                            addedQuantityLabel.setText("Añadido: " + (currentAdded + 1));
                            tempQuantities.put(nombreProducto, currentAdded + 1); // Actualizar HashMap temporal
                        } else {
                            JOptionPane.showMessageDialog(this, "No se puede añadir más de la cantidad en existencia", "Cantidad excedida", JOptionPane.WARNING_MESSAGE);
                        }
                    });
                    buttonPanel.add(increaseButton);

                    // Botón para decrementar la cantidad
                    JButton decreaseButton = new JButton("-");
                    decreaseButton.addActionListener(event -> {
                        int currentAdded = Integer.parseInt(addedQuantityLabel.getText().split(": ")[1]);
                        if (currentAdded > 0) {
                            addedQuantityLabel.setText("Añadido: " + (currentAdded - 1));
                            tempQuantities.put(nombreProducto, currentAdded - 1); // Actualizar HashMap temporal
                        }
                    });
                    buttonPanel.add(decreaseButton);

                    // Botón "Añadir" para guardar la cantidad
                    JButton addButton = new JButton("Añadir");
                    addButton.addActionListener(event -> {
                        int addedAmount = tempQuantities.get(nombreProducto);
                        if (addedAmount == 0) {
                            JOptionPane.showMessageDialog(this, "Agrega una cantidad mayor a 0 para añadir al carrito", "Cantidad inválida", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        addedQuantities.put(nombreProducto, addedAmount);
                        JOptionPane.showMessageDialog(this, "Cantidad añadida: " + addedAmount + " para " + nombreProducto);
                    });
                    buttonPanel.add(addButton);

                    productPanel.add(buttonPanel);
                    mainPanel.add(productPanel);
                    isFirst = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setPreferredSize(new Dimension(750, 550));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Tienda de aves exóticos", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JButton recarButton = new JButton("Recargar");
        JButton cartButton = new JButton("Carrito");

        cartButton.addActionListener(event -> {
            Catalogo cartDialog = new Catalogo(this, addedQuantities, productPrices, stockQuantities, databaseFilePath);
            cartDialog.setVisible(true);
        });

        recarButton.addActionListener(event -> {
            enviarCambiosAlCliente();
            updateInterface();
        });

        titlePanel.add(recarButton, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(cartButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private static void enviarCambiosAlCliente() {
        try {
            // Conectar al servidor
            Socket cl = new Socket("127.0.0.1", 6040);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
            DataInputStream dis = new DataInputStream(cl.getInputStream());

            // Enviar el tipo de conexión al servidor
            dos.writeUTF("CONECCION1");

            // Recibir archivos del servidor
            receiveFilesFromServer(dis);

            // Cerrar conexiones
            dos.close();
            dis.close();
            cl.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveFilesFromServer(DataInputStream dis) {
        try {
            // Crear carpeta para las imágenes si no existe
            File imagesFolder = new File("imagenes_recibidas");
            if (!imagesFolder.exists()) {
                imagesFolder.mkdir();
            }

            String databaseFilePath = "received_identifier.sqlite"; // Ruta fija para la base de datos
            // Mapa para almacenar las actualizaciones de rutas de imágenes
            Map<String, String> imageUpdates = new HashMap<>();

            int numberOfFiles = dis.readInt(); // Leer el número de archivos

            for (int i = 0; i < numberOfFiles; i++) {
                // Recibir nombre del archivo
                String originalFileName = dis.readUTF();

                // Recibir tamaño del archivo
                long fileSize = dis.readLong();

                // Obtener nombre predefinido
                String newFileName;
                if (originalFileName.equals("identifier.sqlite")) {
                    newFileName = databaseFilePath; // Guardar la base de datos con el nombre fijo
                } else if (originalFileName.endsWith(".jpg") || originalFileName.endsWith(".png") || originalFileName.endsWith(".jpeg")) {
                    newFileName = "imagenes_recibidas/" + originalFileName;
                    imageUpdates.put(originalFileName, newFileName); // Guardar actualización de ruta de imagen
                } else {
                    newFileName = "received_" + originalFileName;
                }

                // Crear archivo con el nombre predefinido
                File fileToSave = new File(newFileName);
                System.out.println("Guardando como: " + fileToSave.getAbsolutePath());

                // Recibir contenido del archivo
                byte[] buffer = new byte[4096];
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    int bytesRead;
                    long totalRead = 0;
                    while (totalRead < fileSize && (bytesRead = dis.read(buffer, 0, Math.min(buffer.length, (int)(fileSize - totalRead)))) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                    }
                }
                System.out.println("Archivo " + originalFileName + " recibido y guardado como " + newFileName);
            }

            // Actualizar rutas de imágenes en la base de datos
            SQLiteBase.updateImagePaths(databaseFilePath, imageUpdates);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public void updateQuantities(HashMap<String, Integer> newQuantities) {
        this.addedQuantities = new HashMap<>(newQuantities); // Clonar el mapa para evitar problemas de referencia
        updateInterface(); // Actualiza la interfaz para reflejar las nuevas cantidades
    }

    public static void main(String[] args) {
        String databaseFilePath = (args.length > 0) ? args[0] : "identifier.sqlite";

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new InterfaceGrafica(databaseFilePath));
    }
}
