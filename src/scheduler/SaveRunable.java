package scheduler;

import bot.domain.Data;
import bot.presistance.DataPersist;

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
