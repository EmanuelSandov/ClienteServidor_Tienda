import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Cliente {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Escribir una dirección del servidor: ");
            String host = br.readLine();
            System.out.printf("\nEscriba un puerto: ");
            int pto = Integer.parseInt(br.readLine());

            // Conectar al servidor
            Socket cl = new Socket(host, pto);
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

            // Lanzar la interfaz gráfica utilizando la base de datos recibida
            InterfaceGrafica.main(new String[]{databaseFilePath});

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
