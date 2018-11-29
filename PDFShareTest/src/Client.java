import java.io.*;
import java.net.*;

public class Client {
    public static final int PORT = 5005;
    //public static final String serverHostname = new String ("127.0.0.1");
    public static final String serverHostname = new String ("46.101.214.80");
    public static void main(String[] args) throws IOException {
        System.out.println ("Attempting to connect to host " +
                serverHostname + " on port "+PORT);

        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            echoSocket = new Socket(serverHostname, PORT);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to: " + serverHostname);
            System.exit(1);
        }
        BufferedReader finalIn = in;
        new Thread(){
            @Override
            public void run() {
                while(true){
                    try {
                        String inputLine;
                        while ((inputLine = finalIn.readLine()) != null)
                        {
                            System.out.println ("Server: " + inputLine);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in));
        String userInput;

        System.out.print ("input: ");
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            System.out.print ("input: ");
        }

        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
    }
}