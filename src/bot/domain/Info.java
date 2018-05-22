package bot.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

@lombok.Data
@Entity
public class Info implements Serializable {
    @Id
    private String id;
    private String annonceId;
    private String raidLead;
    private String raidEmote;
    @ElementCollection
    private List<String> isPresent = new ArrayList<>();
    @ElementCollection
    private List<String> reserve = new ArrayList<>();
    private String mode;
    private String target;
    private int dayOfWeek;
    private int hour;
    private int minute;
    private int time;
}