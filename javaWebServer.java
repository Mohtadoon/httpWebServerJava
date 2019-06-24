package httpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
///sajkfdfkaj
// this is commited

//commited from IDEA
public class javaWebServer implements Runnable {

    static final File ROOT_DIRECTORY = new File("/home/shaikmohtadoon/Desktop");
    static final String DEFAULT = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED= "not_supported.html";
    private Socket clientSocket;

    public javaWebServer(Socket socket) {

        clientSocket = socket;
    }

    public static void main(String[] args) throws IOException {

        //    from test branch
            ServerSocket serverSocket = new ServerSocket(8000);
            System.out.println("Server started.    Listening for connections on port : " +8000 + " ...");

            while (true) {
                javaWebServer myServer = new javaWebServer(serverSocket.accept());

                Thread thread = new Thread(myServer);
                thread.start();
            }
    }
/////////////////////////////////////////////////////////////////////////////////////////////

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream inputStreamFromFile = null;
        byte[] filedata = new byte[fileLength];

        try {
            inputStreamFromFile = new FileInputStream(file);
            inputStreamFromFile.read(filedata);
        } finally {
            if (inputStreamFromFile != null)
                inputStreamFromFile.close();
        }
        return filedata;
    }

///////////////////////////////////////////////////////////////////////////////////////////

   private void responseHeadersForFileNotFound(PrintWriter out, OutputStream dataOut) throws IOException {
        File file = new File(ROOT_DIRECTORY, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    private void responseHeadersForMethodNotSupported(PrintWriter out, BufferedOutputStream dataOut) throws IOException {

        File file = new File(ROOT_DIRECTORY, METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();
        String contenttype = "text/html";

        byte[] fileData = readFileData(file, fileLength);
        out.println("HTTP/1.1 501 Not Implemented");
        out.println("Server: Java HTTP Server from Mohtadoon : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + contenttype);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private  void responseForGetMethod(PrintWriter outputWriter,BufferedOutputStream dataOut,String fileRequested) throws IOException {

        File file = new File(ROOT_DIRECTORY, fileRequested);
        int fileLength = (int) file.length();
        String content = getResourceType(fileRequested);


        byte[] fileData = readFileData(file, fileLength);
        outputWriter.println("HTTP/1.1 200 OK");
        outputWriter.println("Server: Java HTTP Server from Mohtadoon : 1.0");
        outputWriter.println("Date: " + new Date());
        outputWriter.println("Content-type: " + content);
        outputWriter.println("Content-length: " + fileLength);
        outputWriter.println();
        outputWriter.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String getResourceType(String fileRequested) {
        if (fileRequested.endsWith(".jpeg"))
            return "image";
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return "text/html";
        if (fileRequested.endsWith(".json"))
            return "JSON";
        else
            return "text/plain";
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void run() {
        BufferedReader inputReader = null;
        PrintWriter outputWriter = null;
        BufferedOutputStream dataOut = null;
        String fileRequested;

        try {
            inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputWriter = new PrintWriter(clientSocket.getOutputStream());
            dataOut = new BufferedOutputStream(clientSocket.getOutputStream());

            String input = inputReader.readLine();
            String[] divider = input.split(" ");
            String method = divider[0];
            fileRequested = divider[1];

            if (!method.equals("GET"))
                responseHeadersForMethodNotSupported(outputWriter, dataOut);

            else {
                if (method.equals("GET"))
                    if (fileRequested.endsWith("/"))
                        fileRequested += DEFAULT;
                    responseForGetMethod(outputWriter, dataOut, fileRequested);
            }

        } catch (FileNotFoundException fnfe) {
            try {
                responseHeadersForFileNotFound(outputWriter, dataOut);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                inputReader.close();
                outputWriter.close();
                dataOut.close();
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }
        }

    }
    //////////////////////////////////////////////////////////////////

}
