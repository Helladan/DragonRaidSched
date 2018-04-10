package scheduler;

import bot.domain.Info;
import bot.domain.Infos;

import java.util.ArrayList;

public class CleanerRunable implements Runnable {

    private Info info;

    public CleanerRunable(Info info) {
        this.info = info;
    }

    @Override
    public void run() {
        info.setTarget(null);
        info.setMode(null);
        info.setIsPresent(new ArrayList<>());
    }
}
