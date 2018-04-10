package bot.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@lombok.Data
public class Info implements Serializable {
    private String annonceId;
    private String raidLead;
    private String raidEmote;
    private List<String> isPresent = new ArrayList<>();
    private String mode;
    private String target;
}