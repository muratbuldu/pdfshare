import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Server {
    public static final int PORT = 5005;
    public static ArrayList<ArrayList<Socket>> socketListList;
    public static ArrayList<Socket> listList;
    public static ArrayList<Integer> pageIndexList;
    public static ArrayList<String> nameList;
    public static ArrayList<String> fileNameList;
    private static Semaphore lock = new Semaphore(1);
    public static void main(String[] args) throws IOException
    {
        // init lists
        socketListList = new ArrayList<>();
        listList = new ArrayList<>();
        pageIndexList = new ArrayList<>();
        nameList = new ArrayList<>();
        fileNameList = new ArrayList<>();

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        }
        catch (IOException e)
        {
            System.err.println("Could not listen on port: "+PORT);
            System.exit(1);
        }
        Socket clientSocket;
        while(true){
            clientSocket = null;
            System.out.println ("Waiting for connection.....");
            try {
                clientSocket = serverSocket.accept();
                System.out.println ("Connection successful");
                Socket finalClientSocket = clientSocket;
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            handleClient(finalClientSocket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
            catch (IOException e)
            {
                System.err.println("Accept failed.");
                System.exit(1);
            }
        }
        // clientSocket.close();
        // serverSocket.close();
    }
    // broadcast page info for that channel
    public static void broadcastInfo(int serveId, int page){
        ArrayList<Socket> socketList = socketListList.get(serveId);
        for (Socket s: socketList) {
            try {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                out.println(page);
                out.println("page;"+page+";"+fileNameList.get(serveId));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // notify all sockets in list state
    public static void notifyList(String name) throws IOException {
        for (Socket s: listList) {
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(name);
        }
    }

    public static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader( clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        String inputLine;
        while ((inputLine = in.readLine()) != null)
        {
            try {
                String[] parts = inputLine.split(";");
                if(parts[0].equals("new")){
                    System.out.println("in new");
                    int serveId = socketListList.size();
                    ArrayList<Socket> sList = new ArrayList<>();
                    lock.acquire();
                    socketListList.add(sList);
                    pageIndexList.add(0);
                    nameList.add(parts[1]);
                    fileNameList.add(parts[2]);
                    lock.release();
                    // send id back
                    out.println("id;"+serveId);
                    System.out.println("id;"+serveId);
                    // notify all sockets in list
                    notifyList(parts[1]);
                }else if(parts[0].equals("setPage")){
                    System.out.println("in set");
                    int serveId = Integer.parseInt(parts[1]);
                    int page = Integer.parseInt(parts[2]);
                    lock.acquire();
                    pageIndexList.set(serveId,page);
                    lock.release();
                    // broadcast page to that id
                    broadcastInfo(serveId,page);
                }else if(parts[0].equals("beServed")){
                    lock.acquire();
                    listList.remove(clientSocket);
                    for (int i = 0; i < socketListList.size(); i++) {
                        socketListList.get(i).remove(clientSocket);
                    }
                    lock.release();
                    System.out.println("in served");
                    String name = parts[1];
                    //int serveId = nameList.indexOf(name);
                    int serveId = Integer.parseInt(parts[1]);
                    socketListList.get(serveId).add(clientSocket);
                    out.println("init;"+pageIndexList.get(serveId)+";"+fileNameList.get(serveId));
                }else if(parts[0].equals("list")){
                    listList.add(clientSocket);
                    out.println(String.join(";", nameList));
                }
                else if(parts[0].equals("status")){
                    System.out.println(pageIndexList.toString());
                    System.out.println(nameList.toString());
                    System.out.println(fileNameList.toString());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
