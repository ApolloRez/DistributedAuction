package AuctionHouse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class ItemList {

    protected static ArrayList<String> nameList;
    private static ArrayList<String> adjList;
    public static ItemList createNameList(String file){
        ItemList list = new ItemList();
        nameList = new ArrayList<>();
        try{
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream is = loader.getResourceAsStream(file);
            assert is != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String name;
            while((name = br.readLine()) != null){
                nameList.add(name);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }
    public static ItemList createList(String nameFile, String adjFile){
        ItemList list = new ItemList();

        return list;
    }

    public String getRandomName(){
        Random random = new Random();
        int size = nameList.size();
        return nameList.get(random.nextInt(size));
    }

    public String getRandomAdj(){
        Random random = new Random();
        int size = adjList.size();
        return adjList.get(random.nextInt(size));
    }
    public void print(){
        for(String name:nameList){
            System.out.println(name);
        }
    }
}
