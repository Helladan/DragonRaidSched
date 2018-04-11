package bot.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@lombok.Data
public class Info implements Serializable {
    private static final long serialVersionUID = 6016683147058983728L;

    private String annonceId;
    private String raidLead;
    private String raidEmote;
    private List<String> isPresent = new ArrayList<>();
    private List<String> reserve = new ArrayList<>();
    private String mode;
    private String target;
}