package bot.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@lombok.Data
public class Data implements Serializable {
    private static final long serialVersionUID = 4503589453989765124L;

    private Map<String, String> playerMap = new HashMap<>();
    private Map<String, Info> infos = new HashMap<>();
}
