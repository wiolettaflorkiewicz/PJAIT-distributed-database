import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseNode {

    private final int port;
    private int key;
    private int value;
    private Set<String> connections;
    private final String adres;

    public DatabaseNode(int port, int key, int value, Set<String> connections) {
        this.port = port;
        this.key = key;
        this.value = value;
        this.connections = connections;

        try {
            this.adres = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 0;
        int key = 0;
        int value = 0;
        HashSet<String> connections = new HashSet<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-tcpport" -> port = Integer.parseInt(args[++i]);
                case "-record" -> {
                    String[] record = args[++i].split(":");
                    key = Integer.parseInt(record[0]);
                    value = Integer.parseInt(record[1]);
                }
                case "-connect" -> connections.add(args[++i]);
            }
        }

        DatabaseNode databaseNode = new DatabaseNode(port, key, value, connections);
        databaseNode.start();

        HashSet<String> connectionAddresses = new HashSet<>();

        for (String connection : connections) {
            String connectionAddress = databaseNode.connectionHandler(connection, "get-host");
            connectionAddresses.add(connectionAddress);
        }
        databaseNode.setConnections(connectionAddresses);
        for (String connectionFixed : connectionAddresses) {
            databaseNode.connectionHandler(connectionFixed, "add-connection " + databaseNode.getAdres());
        }
    }

    public String getAdres() {
        return adres;
    }

    public void setConnections(Set<String> connections) {
        this.connections = connections;
    }


    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started at:\n" +
                "LocalAdress: " + adres + "\n" +
                "Port: " + port + "\n" +
                "Data values: KEY {" + key + "}, VALUE{" + value + "}");
        new Thread(() -> {
        while (true) {
            try {
                System.out.println("Waiting for clients");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                String conntectionDetails;
                String clientAddress = "";

                while ((conntectionDetails = bufferedReader.readLine()) != null) {
                    String[] pairsOfRecord = conntectionDetails.split(" ");
                    String operations = pairsOfRecord[0];
                    clientAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                    System.out.println("Received request:\n" +
                            "Adress: " + adres + "\n" + "get from:" + "\n"+
                            "ClientAdress: " + clientAddress + "\n" +
                            "Operation: " + conntectionDetails);
                    switch (operations) {
                        case "set-value" -> setValue(pairsOfRecord, printWriter);
                        case "get-value" -> getValue(pairsOfRecord, printWriter);
                        case "find-key" -> findKey(pairsOfRecord,printWriter, clientAddress);
                        case "get-max" -> getMax(printWriter, clientAddress);
                        case "get-min" -> getMin(printWriter, clientAddress);
                        case "new-record" -> newRecord(pairsOfRecord, printWriter);
                        case "get-record" -> getRecord(printWriter, clientAddress);
                        case "terminate" -> terminate(printWriter);
                        case "add-connection" -> addConnection(pairsOfRecord, printWriter);
                        case "remove-connection" -> removeConnection(pairsOfRecord, printWriter);
                        case "get-host" ->getHost(printWriter, clientAddress);
                        default -> error(printWriter, clientAddress, conntectionDetails);
                    }
                    break;
                }
                System.out.println("The connection has been completed" + "\n" +
                        adres + " with " + clientAddress);
                bufferedReader.close();
                printWriter.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }).start();
    }

    private String connectionHandler(String connection, String conntectionDetails) {
        String[] pairsOfRecords = connection.split(":");
        String address = pairsOfRecords[0];
        int port = Integer.parseInt(pairsOfRecords[1]);
        System.out.println(adres + " redirects " + connection + " reguest " + conntectionDetails);

        try {
            Socket connectionSocket = new Socket(address, port);
            PrintWriter connectionWriter = new PrintWriter(connectionSocket.getOutputStream(), true);
            BufferedReader connectionReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            connectionWriter.println(conntectionDetails);
            String response = connectionReader.readLine();

            connectionWriter.close();
            connectionReader.close();
            connectionSocket.close();

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void setValue(String[] pairsOfRecords,PrintWriter printWriter){
        String[] keyAndValue = pairsOfRecords[1].split(":");
        int setKey = Integer.parseInt(keyAndValue[0]);
        boolean isDone = false;

        if (setKey == key) {
            value = Integer.parseInt(keyAndValue[1]);
            isDone = true;
        } else {
            for (String connection : connections) {
                String response = connectionHandler(connection, "get-record");
                String[] responseParts = response.split(":");
                if (Integer.parseInt(responseParts[0]) == setKey) {
                    connectionHandler(connection, "set-value " + pairsOfRecords[1]);
                    isDone = true;
                    break;
                }
            }
        }

        if (isDone) {
            System.out.print("Change the value to " + " { " + value + " }");
            printWriter.println("OK");

        } else {
            System.out.println("Changing the value failed.");
            printWriter.println("ERROR");
            }
        }


    private void getValue(String[] pairOfRecords, PrintWriter printWriter){
        int searchValue = Integer.parseInt(pairOfRecords[1]);
        int result = -1;
        if (value == searchValue) {
            result = value;
        } else {
            for (String connection : connections) {
                String response = connectionHandler(connection, "get-record");
                String[] responseParts = response.split(":");
                if (Integer.parseInt(responseParts[1]) == searchValue) {
                    result = Integer.parseInt(responseParts[1]);
                    break;
                }
            }
        }

        if (result == -1) {
            printWriter.println("ERROR");
        } else {
            printWriter.println(result);
        }
    }
    private void findKey(String[] pairsOfRecords, PrintWriter printWriter, String clientAddress) {
        int searchKey = Integer.parseInt(pairsOfRecords[1]);
        String foundNodeAddress = "";

        if (searchKey == key) {
            foundNodeAddress = adres;
        } else {
            for (String connection : connections) {
                String response = connectionHandler(connection, "get-record");
                String[] responseParts = response.split(":");
                if (Integer.parseInt(responseParts[0]) == searchKey) {
                    foundNodeAddress = connection;
                    break;
                }
            }
        }

        if (foundNodeAddress.equals("")) {
            printWriter.println("ERROR");
            System.out.println(adres + " <- " + clientAddress + "Finding the address of the node with key " + " { " + key + " } " + ": no record.");

        } else {
            printWriter.println("{ " + adres + " }");
            System.out.println(adres + " <- " + clientAddress + "Finding the address of the node with key: " + "{ " + key + " }");
        }
    }

    public void getMax(PrintWriter printWriter, String clientAddress) {
        ConcurrentHashMap<Integer, Integer> values = new ConcurrentHashMap<>();
        values.put(key, value);

        for (String connection : connections) {
            String response = connectionHandler(connection, "get-record");
            String[] responseParts = response.split(":");
            values.put(Integer.parseInt(responseParts[0]), Integer.parseInt(responseParts[1]));
        }

        int maxValue = Collections.max(values.values());

        int maxKey = 0;
        for(Integer key : values.keySet()){
            if(values.get(key) == maxValue){
                maxKey = key;
            }
        }

        System.out.println(adres + " -> " + clientAddress + " read max value:." + maxValue);
        printWriter.println("{ " + maxKey + " } " + ":" + " { " + maxValue + " }");
    }

    private void getMin(PrintWriter printWriter, String clientAddress) {
        ConcurrentHashMap<Integer, Integer> values = new ConcurrentHashMap<>();
        values.put(key, value);

        for (String connection : connections) {
            String response = connectionHandler(connection, "get-record");
            String[] responseParts = response.split(":");
            values.put(Integer.parseInt(responseParts[0]), Integer.parseInt(responseParts[1]));
        }

        int minValue = Collections.min(values.values());

        //find the max key
        int minKey = 0;
        for(Integer key : values.keySet()){
            if(values.get(key) == minValue){
                minKey = key;
            }
        }
        System.out.println(adres + " -> " + clientAddress + " read min value:." + minValue);
        printWriter.println("{ " + minKey + " } " + ":" + " { " + minValue + " }");
    }


    private void newRecord(String[] pairsOfRecords, PrintWriter out){
        String[] keyAndVal = pairsOfRecords[1].split(":");
        key = Integer.parseInt(keyAndVal[0]);
        value = Integer.parseInt(keyAndVal[1]);
        System.out.println(adres + " Adding a new key record " + key + " and value " + value);
        out.println("OK");
    }

    private void getRecord(PrintWriter printWriter, String clientAddress){
        printWriter.println("{ " + key + " } " + ":" + " { " + value + " }");
        System.out.println(adres + " <- " + clientAddress + " Reading the key record" + key + " and value " + value);
    }

    private void terminate(PrintWriter printWriter){
        printWriter.println("OK");
        System.out.println(adres + " terminate node");
        for (String connection : connections) {
            System.out.println("Node shutdown.");
            connectionHandler(connection, "remove-connection"  + adres);
        }
        System.exit(0);
    }

    private void addConnection(String[] pairsOfRecords, PrintWriter printWriter){
        String newConnection = pairsOfRecords[1];
        connections.add(newConnection);
        printWriter.println("OK");
        System.out.println(adres + " Adding a connection: " + newConnection);
    }

    private void removeConnection(String[] pairsOfRecords, PrintWriter printWriter){
        String connection = pairsOfRecords[1];
        connections.remove(connection);
        printWriter.println("OK");
        System.out.println(adres + " remove a connection: " + connection);
    }

    private void getHost(PrintWriter output, String clientAddress){
        output.println(adres);
        System.out.println(adres + " <- " + clientAddress + " Reading the host address");
    }

    private void error(PrintWriter output, String clientAddress, String conntectionDetails){
        String response = connectionHandler(conntectionDetails, "get-host");
        if(response.equals("ERROR")){
            output.println("ERROR");
            System.out.println(adres + " < - " + clientAddress + " Error in query: " + conntectionDetails);
        } else {
            output.println("OK");
        }
    }
}