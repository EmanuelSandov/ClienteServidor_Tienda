import java.net.*;
import java.io.*;

public class Servidor {
    private static final String DB_FILE_PATH = "C:\\Users\\emanu\\Documents\\Escuela\\PROGRAMACION\\Java\\Practica_4\\Envio_archivos\\identifier.sqlite";
    private static final String IMAGE_FOLDER_PATH = "C:\\Users\\emanu\\Documents\\Escuela\\PROGRAMACION\\Java\\Practica_4\\Envio_archivos\\image";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(6040)) {
            while (true) {
                handleConnections(serverSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleConnections(ServerSocket serverSocket) {
        try {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Conexión establecida desde: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            // Procesar la conexión en un nuevo hilo para manejar múltiples conexiones
            new Thread(() -> {
                try {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream())) {
            String connectionType = dis.readUTF();
            switch (connectionType) {
                case "CONECCION1":
                    sendFiles(clientSocket);
                    break;
                case "CONECCION3":
                    receiveDatabase(clientSocket);
                    break;
                default:
                    System.out.println("Tipo de conexión no reconocido: " + connectionType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendFiles(Socket socket) {
        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            File dbFile = new File(DB_FILE_PATH);
            File folder = new File(IMAGE_FOLDER_PATH);
            File[] listOfFiles = folder.listFiles();
            int numberOfFiles = 1 + (listOfFiles != null ? listOfFiles.length : 0);
            dos.writeInt(numberOfFiles);

            sendFile(dbFile, dos);

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        sendFile(file, dos);
                    }
                }
            }

            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(File file, DataOutputStream dos) throws IOException {
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        byte[] buffer = new byte[4096];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void receiveDatabase(Socket socket) throws IOException {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            receiveFile(dis, DB_FILE_PATH, fileSize);
        }
    }

    private static void receiveFile(DataInputStream dis, String newFileName, long fileSize) throws IOException {
        File fileToSave = new File(newFileName);
        System.out.println("Guardando como: " + fileToSave.getAbsolutePath());

        byte[] buffer = new byte[4096];
        try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
            int bytesRead;
            long totalRead = 0;
            while (totalRead < fileSize && (bytesRead = dis.read(buffer, 0, Math.min(buffer.length, (int) (fileSize - totalRead)))) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
        }
        System.out.println("Archivo recibido y guardado como " + newFileName);
    }
}
