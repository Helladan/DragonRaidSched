package scheduler;

import bot.domain.Data;
import bot.domain.Info;
import bot.presistance.DataPersist;

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
        DataPersist.save(data);
    }
}
