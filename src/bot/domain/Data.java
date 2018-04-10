package bot.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@lombok.Data
public class Data implements Serializable {
    private Map<String, String> playerMap = new HashMap<>();
    private Map<String, Info> infos = new HashMap<>();
}
