package pan.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread implements Runnable{
    Socket mySocket;
    DataInputStream in;
    DataOutputStream out;
    ClientThread(Socket mySocket) throws IOException {
        this.mySocket=mySocket;
        this.in=new DataInputStream(mySocket.getInputStream());
        this.out=new DataOutputStream(mySocket.getOutputStream());
    }
    @Override
    public void run() {
        try {
            System.out.println(in.readUTF());
            choose();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void choose() throws IOException {
        System.out.println("1.发送   2.下载");
        Scanner reader=new Scanner(System.in);
        String num=reader.next();
        if(num.equals("1")){
            send();
        } else if (num.equals("2")) {
            download();
        }else{
            choose();
        }
    }

    private void download() throws IOException {

        Scanner reader=new Scanner(System.in);
        this.out.writeUTF("receive");
        int num=this.in.readInt();
        for(int i=0;i<num;i++){
            System.out.println(this.in.readUTF());
        }

        System.out.print("输入文件名字:");
        String name=reader.next();
        this.out.writeUTF(name);
        if(this.in.readUTF().equals("can not find the file")){
            System.out.println("can not find the file");
        }else {
            File myFile=new File("download/"+name);
            if(myFile.exists()){
                myFile.delete();
            }
            OutputStream output=new FileOutputStream(myFile);
            byte[] buff=new byte[1024];
            int len;
            while((len = this.in.read(buff))!=-1){
                output.write(buff,0,len);
            }
            System.out.println("收到来自："+this.mySocket.getInetAddress()+" 的文件 "+name);
            this.out.writeBoolean(true);
        }
    }

    private void send() throws IOException {
        Scanner reader=new Scanner(System.in);
        this.out.writeUTF("send");
        System.out.print("输入文件路径:");
        String path=reader.next();
        File myFile=new File(path);
        if(!myFile.exists()){
            System.out.println("文件不存在");
            send();
        }else{
            this.out.writeUTF(myFile.getName());
            InputStream input=new FileInputStream(myFile);
            byte[] buff=new byte[1024];
            int len;
            while((len = input.read(buff))!=-1){
                this.out.write(buff,0,len);
            }
            mySocket.shutdownOutput();
            if(this.in.readBoolean()) {
                System.out.println("向：" + this.mySocket.getInetAddress() + " 发送文件 " + myFile.getName());
            }
        }

    }
}
