import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class TCPClient {
  private static final int TIMEOUT = 10000;
  private static final String LOG_FILE = "res/client-log.txt";

  public static void main(String[] args) {
    if (args.length != 2) {
      log("Incorrect Arguments");
      throw new IllegalArgumentException("Please enter the following arguments - IP/hostname and Port number");
    }

    String hostname = args[0];
    int port = Integer.parseInt(args[1]);
    String operationsScript = "res/operations-script.txt";

    log("Starting TCP Client");
    log("Attempting connection to " + hostname + " on port " + port);

    try (Socket clientSocket = new Socket(hostname, port)) {
      clientSocket.setSoTimeout(TIMEOUT);
      PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      Scanner scanner = new Scanner(System.in);

      log("Connection established.");

      while (true) {
        log("Enter 'run' to execute command from script, 'console' to enter commands manually, or 'close' to exit: ");
        String userInput = scanner.nextLine().trim().toLowerCase();

        if ("run".equals(userInput)) {
          processScript(operationsScript, out, in, "All Operations Performed");
        } else if ("console".equals(userInput)) {
          log("Entering manual command mode. Type 'exit' to return to the main menu.");
          while (true) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine();
            if ("exit".equalsIgnoreCase(command)) break;
            processCommand(command, out, in);
          }
        } else if ("close".equals(userInput)) {
          log("Exiting client.");
          break;
        } else {
          log("Invalid input. Please enter 'run', 'console', or 'close'.");
        }
      }
    } catch (SocketTimeoutException e) {
      log("Server connection timed out.");
    } catch (IOException e) {
      log("Error connecting to server: " + e.getMessage());
    }
  }

  private static void processScript(String scriptFilePath, PrintWriter out, BufferedReader in, String completionMsg) throws IOException {
    try (BufferedReader scriptReader = new BufferedReader(new FileReader(scriptFilePath))) {
      String line;
      while ((line = scriptReader.readLine()) != null) {
        processCommand(line, out, in);
      }
      log(completionMsg);
    } catch (FileNotFoundException e) {
      log("Script file not found: " + scriptFilePath);
    }
  }

  private static void processCommand(String command, PrintWriter out, BufferedReader in) throws IOException {
    if (command.trim().isEmpty()) {
      return;
    }
    out.println(command);
    log("Command sent: " + command);

    try {
      String response = in.readLine();
      if (response != null) {
        log("Received: " + response);
      } else {
        log("No response from server.");
      }
    } catch (SocketTimeoutException e) {
      log("No response from server within timeout for command: " + command);
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
