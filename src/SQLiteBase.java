import java.sql.*;
import java.util.Map;

public class SQLiteBase {

    //Mostrar datos de la tabla
    public static void printTableData(String tableName) {
        Connection connection = null;
        try {
            String url = "jdbc:sqlite:identifier.sqlite";
            connection = DriverManager.getConnection(url);
            System.out.println("Conexion establecida con exito");
            String query = "SELECT * FROM " + tableName;
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                // Obtener metadatos de los resultados para imprimir los nombres de las columnas
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                // Imprimir nombres de las columnas
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rsmd.getColumnName(i) + "\t");
                }
                System.out.println();

                // Imprimir los datos de cada fila
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rs.getString(i) + "\t");
                    }
                    System.out.println();
                }
            } catch (SQLException e) {
                System.out.println("Error al consultar datos: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    //Agregar datos
    public static void insertData(String tableName, String producto, int cantidad, String imagen, double precio) {
        Connection connection = null;
        try {
            String url = "jdbc:sqlite:identifier.sqlite";
            connection = DriverManager.getConnection(url);
            System.out.println("Conexion establecida con exito");
            String sql = "INSERT INTO " + tableName + " (Producto, Cantidad, Imagen, Precio) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, producto);
                pstmt.setInt(2, cantidad);
                pstmt.setString(3, imagen);
                pstmt.setDouble(4, precio);
                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows + " filas insertadas.");
            } catch (SQLException e) {
                System.out.println("Error al insertar datos: " + e.getMessage());
        }} catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    //Eliminar
    public static void deleteData(String tableName, int cursoID) {
        Connection connection = null;
        try {
            String url = "jdbc:sqlite:identifier.sqlite";
            connection = DriverManager.getConnection(url);
            System.out.println("Conexion establecida con exito");
            String sql = "DELETE FROM " + tableName + " WHERE CursoID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, cursoID);
                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows + " filas eliminadas.");
            } catch (SQLException e) {
                System.out.println("Error al eliminar datos: " + e.getMessage());
        }} catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    //Actualizar
    public static void updateData(String tableName, int cursoID, String producto, int cantidad, String imagen, double precio) {
        Connection connection = null;
        try {
            String url = "jdbc:sqlite:identifier.sqlite";
            connection = DriverManager.getConnection(url);
            System.out.println("Conexion establecida con exito");
            String sql = "UPDATE " + tableName + " SET Producto = ?, Cantidad = ?, Imagen = ?, Precio = ? WHERE CursoID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, producto);
                pstmt.setInt(2, cantidad);
                pstmt.setString(3, imagen);
                pstmt.setDouble(4, precio);
                pstmt.setInt(5, cursoID);
                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows + " filas actualizadas.");
            } catch (SQLException e) {
                System.out.println("Error al actualizar datos: " + e.getMessage());
        }} catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void updateProductQuantity(String dbPath, String tableName, String producto, int cantidadComprada) {
        Connection connection = null;
        try {
            String url = "jdbc:sqlite:" + dbPath;
            connection = DriverManager.getConnection(url);
            String sql = "UPDATE " + tableName + " SET Cantidad = Cantidad - ? WHERE Producto = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, cantidadComprada);
                pstmt.setString(2, producto);
                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows + " filas actualizadas.");
            } catch (SQLException e) {
                System.out.println("Error al actualizar datos: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void updateImagePaths(String databaseFilePath, Map<String, String> imageUpdates) {
        System.out.println(imageUpdates);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath)) {
            for (Map.Entry<String, String> entry : imageUpdates.entrySet()) {
                String originalFileName = entry.getKey();
                String newFilePath = entry.getValue();

                String sql = "UPDATE Tienda SET Imagen = ? WHERE Imagen = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newFilePath);
                    pstmt.setString(2, originalFileName);
                    pstmt.executeUpdate();
                }
            }
            System.out.println("Rutas de imágenes actualizadas en la base de datos.");
        } catch (SQLException e) {
            System.out.println("Error al actualizar rutas de imágenes: " + e.getMessage());
        }
    }

}

