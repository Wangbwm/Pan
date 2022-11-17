package pan.function;

import java.io.File;
import java.util.LinkedList;

public class fileFind {
    String path;
    LinkedList<String>fList=new LinkedList<>();
    public fileFind(String path){
        this.path=path;
    }
    public LinkedList<String> run() {
        File myFile=new File(path);
        if(myFile.isFile()){
            fList.add(myFile.getName());
        }else {
            Directory(myFile,"",fList);
        }
        return fList;
    }
    private void Directory(File myFile, String path, LinkedList<String> list) {
        if(myFile.isDirectory()){
            File[] children=myFile.listFiles();
            path=path+myFile.getName()+"\\";
            for(File f:children){
                Directory(f,path,list);
            }
        }else{
                list.add(path+myFile.getName());
            }

        }
    }


