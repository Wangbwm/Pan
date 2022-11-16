package pan.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket mySocket=new ServerSocket(8080);
        Socket getSocket;
        System.out.println("Waiting……");
        while(true){
            getSocket=mySocket.accept();
            System.out.println("客户机:"+getSocket.getInetAddress()+" 上线");
            ServerThread function=new ServerThread(getSocket);
            new Thread(function,"服务线程 "+getSocket.getInetAddress()).start();
        }
    }
}
