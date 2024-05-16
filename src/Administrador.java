import java.util.Scanner;

public class Administrador {
    public static void main(String[] args) {
        adminInterface();
    }

    public static void adminInterface() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Opciones de administración:");
            System.out.println("1. Agregar producto");
            System.out.println("2. Eliminar producto");
            System.out.println("3. Actualizar producto");
            System.out.println("4. Mostrar productos");
            System.out.println("5. Salir");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            switch (choice) {
                case 1:
                    System.out.print("Nombre del producto: ");
                    String name = scanner.nextLine();
                    System.out.print("Cantidad del producto: ");
                    int cantidad = scanner.nextInt();
                    scanner.nextLine(); // Consumir la nueva línea
                    System.out.print("Ruta de la imagen del producto: ");
                    String imagen = scanner.nextLine();
                    System.out.print("Precio del producto: ");
                    double price = scanner.nextDouble();
                    scanner.nextLine(); // Consumir la nueva línea
                    SQLiteBase.insertData("Tienda", name, cantidad, imagen, price);
                    break;
                case 2:
                    System.out.print("ID del producto a eliminar: ");
                    int idToDelete = scanner.nextInt();
                    scanner.nextLine(); // Consumir la nueva línea
                    SQLiteBase.deleteData("Tienda", idToDelete);
                    break;
                case 3:
                    System.out.print("ID del producto a actualizar: ");
                    int idToUpdate = scanner.nextInt();
                    scanner.nextLine(); // Consumir la nueva línea
                    System.out.print("Nuevo nombre del producto: ");
                    String newName = scanner.nextLine();
                    System.out.print("Nueva cantidad del producto: ");
                    int newCantidad = scanner.nextInt();
                    scanner.nextLine(); // Consumir la nueva línea
                    System.out.print("Nueva ruta de la imagen del producto: ");
                    String newImagen = scanner.nextLine();
                    System.out.print("Nuevo precio del producto: ");
                    double newPrice = scanner.nextDouble();
                    scanner.nextLine(); // Consumir la nueva línea
                    SQLiteBase.updateData("Tienda", idToUpdate, newName, newCantidad, newImagen, newPrice);
                    break;
                case 4:
                    SQLiteBase.printTableData("Tienda");
                    break;
                case 5:
                    scanner.close();
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }
}
