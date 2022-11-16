package pan.server;

import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable{
    DataInputStream in;
    DataOutputStream out;
    Socket mySocket;
    ServerThread(Socket mySocket) throws IOException {
        this.mySocket=mySocket;
        this.in=new DataInputStream(mySocket.getInputStream());
        this.out=new DataOutputStream(mySocket.getOutputStream());
    }
    @Override
    public void run() {
        try {
            this.out.writeUTF("************欢迎使用Pan助手*************");
            String function=this.in.readUTF();
            if(function.equals("receive")){
                send();
            } else if (function.equals("send")) {
                receive();
            }else{
                this.out.writeUTF("wrong input");
            }
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }

    }

    private void receive() throws IOException {
        String path=this.in.readUTF();
        File myFile=new File("database/"+path);
        if(myFile.exists()){
            myFile.delete();
        }
        myFile.createNewFile();
        OutputStream output=new FileOutputStream(myFile);
        byte[] buff=new byte[1024];
        int len;
        while((len = this.in.read(buff))!=-1){
            output.write(buff,0,len);
        }
        this.out.writeBoolean(true);
        System.out.println("接收到来自："+this.mySocket.getInetAddress()+" 的文件:"+path);
    }

    private void send() {
        try {
            String basePath="database";
            String[] list=new File(basePath).list();
            this.out.writeInt(list.length);
            for(String s:list){
                this.out.writeUTF(s);
            }

            String path=this.in.readUTF();
            File myFile=new File("database/"+path);
            if(!myFile.exists()){
                this.out.writeUTF("can not find the file");
                System.out.println("can not find the file");
            }else {
                this.out.writeUTF(path);
                InputStream input=new FileInputStream(myFile);
                byte[] buff=new byte[1024];
                int len;
                while((len = input.read(buff))!=-1){
                    this.out.write(buff,0,len);
                }
                mySocket.shutdownOutput();
                if(this.in.readBoolean()){
                    System.out.println("成功向："+this.mySocket.getInetAddress()+" 发送文件"+path);
                }
                mySocket.close();

            }
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }
    }
}
