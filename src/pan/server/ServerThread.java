package pan.server;

import pan.function.fileFind;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

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
        String get=this.in.readUTF();
        if(get.equals("File")){
            String path = this.in.readUTF();
            path = "database/" + path;
            File myFile = new File(path);
            getFile(myFile);
        } else if (get.equals("Directory")) {
            int size= this.in.readInt();
            for(int i=0;i<size;i++) {
                String path = this.in.readUTF();
                path = "database/" + path;
                File myFile = new File(path);
                myFile.getParentFile().mkdirs();
                getFile(myFile);
            }
        }else {
            System.out.println("Error");
        }

    }

    private void getFile(File myFile) throws IOException {
        if(myFile.exists()){
            myFile.delete();
        }
        myFile.createNewFile();
        OutputStream output=new FileOutputStream(myFile);
        byte[] buff=new byte[1024];
        int len;
        int len_=0;
        Float fileLength=this.in.readFloat();
        if(fileLength!=0) {
            while ((len = this.in.read(buff)) != -1) {
                output.write(buff, 0, len);
                len_ = len_ + len;
                if (len_ >= fileLength) {
                    break;
                }
            }
            this.out.flush();
        }
        this.out.writeBoolean(true);
        System.out.println("接收到来自："+this.mySocket.getInetAddress()+" 的文件:"+myFile.getName());
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
                if(myFile.isDirectory()){
                    sendDirectory(path);
                }else{
                    this.out.writeUTF("File");
                    sendFile(path, myFile);
                    mySocket.shutdownOutput();
                }

                mySocket.close();

            }
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }
    }

    private void sendFile(String path, File myFile) throws IOException {

        //this.out.flush();
        InputStream input=new FileInputStream(myFile);
        byte[] buff=new byte[1024];
        int len;
        this.out.writeUTF(path);
        this.out.flush();
        this.out.writeFloat(myFile.length());
        if(myFile.length()!=0) {
            while ((len = input.read(buff)) != -1) {
                this.out.write(buff, 0, len);
                this.out.flush();
            }
            this.out.flush();
        }
        if(this.in.readBoolean()){
            System.out.println("成功向："+this.mySocket.getInetAddress()+" 发送文件"+path);
        }
        //mySocket.shutdownOutput();
    }
    private void sendDirectory(String path) throws IOException {
        path="database/"+path;
        fileFind find=new fileFind(path);
        LinkedList<String>list=find.run();
        this.out.writeUTF("Directory");
        this.out.writeInt(list.size());
        this.out.flush();
        for (String file:list){
            File myFile=new File("database/"+file);
            sendFile(file,myFile);
        }
        this.mySocket.shutdownOutput();
    }
}
