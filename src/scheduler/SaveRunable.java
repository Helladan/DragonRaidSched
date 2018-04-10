package scheduler;

import bot.domain.Data;
import bot.domain.Datas;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class SaveRunable implements Runnable {
    public final static String PRESENCE_FOLDER = "save/presence/";
    public final static String PRESENCE_FILE = "%s.sav";
    public final static String PLAYER_FILE = "save/player.sav";
    public final static String DATA_FILE = "save/data.sav";

    Data data;

    public SaveRunable(Data data) {
        this.data = data;
    }

    @Override
    public void run() {
        try {
            File file = new File(PRESENCE_FOLDER);
            if(file.exists()){
                file.delete();
            }
            file = new File(PLAYER_FILE);
            if(file.exists()){
                file.delete();
            }
            file = new File(DATA_FILE);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
