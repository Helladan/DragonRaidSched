package bot.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Mode {
    INFANTILE("Infantile", "https://cdn.discordapp.com/attachments/426784911731458048/432967871623200770/Icone_evenement_crane.png"),
    TRIBULATION("Tribulation", "https://cdn.discordapp.com/attachments/426784911731458048/432967869861593098/Icone_crane_rouge.png");

    private String nom;
    private String url;
}
