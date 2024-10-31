import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPServer {
  private static final Map<String, String> store = new HashMap<>();
  private static final String SCRIPT_FILE_PATH = "res/data-population-script.txt";
  private static final String LOG_FILE = "res/server-log.txt";

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      log("Incorrect Arguments");
      throw new IllegalArgumentException("Please enter the following arguments - Port number");
    }

    int port = Integer.parseInt(args[0]);
    log("TCP Server is starting on port " + port);

    populateData(SCRIPT_FILE_PATH);

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        log("Waiting for client connection...");
        try {
          Socket clientSocket = serverSocket.accept();
          log("Connection established with " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
          handleClient(clientSocket);
        } catch (IOException e) {
          log("Error accepting client connection: " + e.getMessage());
        }
      }
    }
  }

  private static void populateData(String scriptPath) {
    try (BufferedReader scriptReader = new BufferedReader(new FileReader(scriptPath))) {
      String line;
      while ((line = scriptReader.readLine()) != null) {
        handleRequest(line);
      }
      log("Data Population Completed");
    } catch (FileNotFoundException e) {
      log("Script file not found: " + scriptPath);
    } catch (IOException e) {
      log("Error reading script file: " + e.getMessage());
    }
  }

  private static void handleClient(Socket clientSocket) {
    try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

      String clientInput;
      while ((clientInput = input.readLine()) != null) {
        log("Received: " + clientInput);
        String response = handleRequest(clientInput);
        output.println(response);
        log("Sent: " + response);
      }
    } catch (IOException e) {
      log("Error in client communication: " + e.getMessage());
    } finally {
      try {
        clientSocket.close();
      } catch (IOException e) {
        log("Error closing client socket: " + e.getMessage());
      }
    }
  }

  private static String handleRequest(String request) {
    String[] parts = request.split(" ");
    if (parts.length < 1) {
      return "Error: Malformed request";
    }

    String command = parts[0].toUpperCase();
    switch (command) {
      case "PUT":
        if (parts.length != 3) {
          return "Error: PUT command requires 2 arguments (key, value)";
        }
        store.put(parts[1], parts[2]);
        return "Success: Key=" + parts[1] + ", Value=" + parts[2] + " stored.";
      case "GET":
        if (parts.length != 2) {
          return "Error: GET command requires 1 argument (key)";
        }
        String value = store.get(parts[1]);
        return value != null ? "Success: Key=" + parts[1] + ", Value=" + value : "Error: Key not found";
      case "DELETE":
        if (parts.length != 2) {
          return "Error: DELETE command requires 1 argument (key)";
        }
        return store.remove(parts[1]) != null ? "Success: Key=" + parts[1] + " deleted." : "Error: Key not found";
      default:
        return "Error: Unknown command";
    }
  }

  private static void log(String message) {
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    String logMessage = "[" + timestamp + "] " + message;
    System.out.println(logMessage);

    try (FileWriter logFile = new FileWriter(LOG_FILE, true)) {
      logFile.write(logMessage + "\n");
    } catch (IOException e) {
      System.err.println("Failed to log to file: " + e.getMessage());
    }
  }
}
