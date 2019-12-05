package AuctionHouse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Steven Chase
 * This class reads in one to two text files from the resource directory
 * and converts it into a list for the auction items.
 */
public class ItemList {
    /**
     * nameList- ArrayList where the name of potential items are stored
     * adjList- List of adjectives (currently unused)
     */
    protected static ArrayList<String> nameList;
    private static ArrayList<String> adjList;

    /**
     * reads in text file and converts it into a list of strings. The strings
     * in the list are treated as nouns
     * @param file name of the file being read
     * @return returns the List of strings
     */
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


    /**
     * reads in two text files and creates a list of strings from it. The
     * Strings in the lists are treated as nouns/adjectives
     * @param nameFile
     * @param adjFile
     * @return
     */
    public static ItemList createList(String nameFile, String adjFile){
        ItemList list = new ItemList();

        return list;
    }

    /**
     * Gets a random String from nameList
     * @return returns a random string
     */
    public String getRandomName(){
        Random random = new Random();
        int size = nameList.size();
        return nameList.get(random.nextInt(size));
    }

    /**
     * gets a random String from adjList
     * @return returns a random string
     */
    public String getRandomAdj(){
        Random random = new Random();
        int size = adjList.size();
        return adjList.get(random.nextInt(size));
    }
}
