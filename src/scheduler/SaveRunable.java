package scheduler;

import net.dv8tion.jda.core.entities.TextChannel;
import populator.MapPresentToMapIdPopulator;
import static java.lang.String.format;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class SaveRunable implements Runnable {
    public final static String PRESENCE_FOLDER = "save/presence/";
    public final static String PRESENCE_FILE = "%s.sav";
    public final static String PLAYER_FILE = "save/player.sav";

    Map<TextChannel, List<String>> isPresent;
    Map<String, String> playerMap;
    public SaveRunable(Map<TextChannel, List<String>> isPresent, Map<String, String> playerMap) {
        this.isPresent = isPresent;
        this.playerMap = playerMap;
    }

    @Override
    public void run() {
        try {
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            Map<String, List<String>> isPresentId = MapPresentToMapIdPopulator.populate(isPresent);
            for(String chanelId : isPresentId.keySet()) {
                fos = new FileOutputStream(PRESENCE_FOLDER + format(PRESENCE_FILE, chanelId));
                oos = new ObjectOutputStream(fos);
                oos.writeObject(isPresentId.get(chanelId));
                oos.close();
            }
            fos = new FileOutputStream(PLAYER_FILE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(playerMap);
            oos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
