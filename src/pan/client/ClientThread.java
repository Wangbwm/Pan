package pan.client;

import pan.function.fileFind;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Locale;
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
        String get=this.in.readUTF();
        if(get.equals("can not find the file")){
            System.out.println("can not find the file");
        }else if(get.equals("File")){
            receiveFile(name);
        } else if (get.equals("Directory")) {
            int size = this.in.readInt();
            for(int i=0;i<size;i++){
                String path=this.in.readUTF();
                path="download/"+path;
                File myFile=new File(path);
                myFile.getParentFile().mkdirs();
                getFile(myFile);
            }
        }else {
            System.out.println("error");
        }
    }

    private void getFile(File myFile) throws IOException {
        if(myFile.exists()){
            myFile.delete();
        }
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
            output.flush();
        }
        System.out.println("收到来自："+this.mySocket.getInetAddress()+" 的文件 "+ myFile.getName());
        this.out.writeBoolean(true);
    }

    private void receiveFile(String name) throws IOException {
        String[] temp=name.split("\\\\");
        if(temp.length>1){//说明是目录下的文件
            String bathPath="download";
            for (int i=0;i<temp.length;i++){
                if(i!=temp.length){
                    bathPath=bathPath+"\\\\"+temp[i];
                    File myFile = new File(bathPath);
                    myFile.mkdirs();
                }
            }
            File myFile=new File("download/"+ name);
            getFile(myFile);
        }else{
            File myFile=new File("download/"+ name);
            getFile(myFile);
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
            if(myFile.isDirectory()){
                this.out.writeUTF("Directory");
                sendDirectory(path);
            }else{
                this.out.writeUTF("File");
                sendFile(myFile,myFile.getName());
                mySocket.shutdownOutput();
            }

        }

    }

    private void sendDirectory(String path) throws IOException {
        fileFind find=new fileFind(path);
        LinkedList<String> list=find.run();
        int size=list.size();
        this.out.writeInt(size);
        for (String file:list){
            String[] s=file.split("\\\\");
            StringBuffer m = new StringBuffer();
            for(int i=0;i<s.length;i++){
                if(i==0)continue;
                if(i==s.length-1){
                    m.append(s[i]);
                }else{
                    m.append(s[i]+"\\");
                }

            }
            File myFile=new File(path+"\\"+m);
            sendFile(myFile,file);
        }
        this.mySocket.shutdownOutput();
    }

    private void sendFile(File myFile,String file) throws IOException {
        this.out.writeUTF(file);
        this.out.writeFloat(myFile.length());
        if(myFile.length()!=0) {
            InputStream input = new FileInputStream(myFile);
            byte[] buff = new byte[1024];
            int len;

            while ((len = input.read(buff)) != -1) {
                this.out.write(buff, 0, len);
            }
            this.out.flush();
        }
        if(this.in.readBoolean()) {
            System.out.println("向：" + this.mySocket.getInetAddress() + " 发送文件 " + myFile.getName());
        }
    }
}
