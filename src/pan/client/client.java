package pan.client;

import java.io.IOException;
import java.net.Socket;

public class client {
    public static void main(String[] args) throws IOException {
        Socket mySocket=new Socket("127.0.0.1",8080);
        ClientThread function=new ClientThread(mySocket);
        new Thread(function,"客户端线程： "+mySocket.getInetAddress()).start();
    }
}
