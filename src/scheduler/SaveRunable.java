package scheduler;

import bot.domain.Data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class SaveRunable implements Runnable {
    public final static String DATA_FILE = "save/data.sav";

    Data data;

    public SaveRunable(Data data) {
        this.data = data;
    }

    @Override
    public void run() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(DATA_FILE));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
