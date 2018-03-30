package populator;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapPresentToMapIdPopulator {
    public static Map<String, List<String>> populate(Map<TextChannel, List<String>> presents){
        Map<String, List<String>> mapId = new HashMap<>();
        for(Map.Entry entry : presents.entrySet()){
            mapId.put(((TextChannel)entry.getKey()).getId(), (List<String>)entry.getValue());
        }
        return mapId;
    }
}
