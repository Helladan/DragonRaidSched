package bot.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class Datas implements Serializable{
    private Map<String, String> playerMap = new HashMap<>();
    private Map<String, Infos> infos = new HashMap<>();
}
