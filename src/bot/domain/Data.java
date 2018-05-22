package bot.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;


@lombok.Data
@Entity
public class Data implements Serializable {
	@Id
	private Long id;
    @ElementCollection
    private Map<String, String> playerMap = new HashMap<>();
    @ElementCollection
    private Map<String, Info> infos = new HashMap<>();
}
