package bot.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Infos implements Serializable{
    private String annonceId;
    private List<String> isPresent = new ArrayList<>();
    private String mode;
    private String target;
}
