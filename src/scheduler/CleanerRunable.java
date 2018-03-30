package scheduler;

import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CleanerRunable implements Runnable {
    public CleanerRunable(Map<TextChannel, List<String>> isPresent) {
        this.isPresent = isPresent;
    }

    Map<TextChannel, List<String>> isPresent;
    @Override
    public void run() {
        for(TextChannel textChannel : isPresent.keySet()){
            isPresent.put(textChannel, new ArrayList<>());
        }
    }
}
