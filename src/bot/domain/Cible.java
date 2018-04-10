package bot.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Cible {
    CAIRN("Cairn l'Indomptable", "https://wiki-fr.guildwars2.com/images/e/e7/Mini-Cairn_l%27Indomptable.png"),
    ESCORTE("Escorte", "https://wiki.guildwars2.com/images/b/b5/Mini_McLeod_the_Silent.png"),
    MURSAAT("Surveillant mursaat", "https://wiki-fr.guildwars2.com/images/3/39/Mini-surveillant_mursaat.png"),
    GORSEVAL("Gorseval le Disparate", "https://wiki.guildwars2.com/images/d/d1/Mini_Gorseval_the_Multifarious.png"),
    SABETHA("Sabetha la saboteuse", "https://wiki.guildwars2.com/images/5/54/Mini_Sabetha.png"),
    PARESSOR("Paressor", "https://wiki.guildwars2.com/images/1/12/Mini_Slothasor.png"),
    MATTHIAS("Matthias Gabrel", "https://wiki.guildwars2.com/images/5/5d/Mini_Matthias_Abomination.png"),
    TITAN("Titan du fort", "https://wiki.guildwars2.com/images/e/ea/Mini_Keep_Construct.png"),
    XERA("Xera", "https://wiki.guildwars2.com/images/4/4b/Mini_Xera.png"),
    SAMAROG("Samarog", "https://wiki.guildwars2.com/images/f/f0/Mini_Samarog.png"),
    DEIMOS("Deimos", "https://wiki.guildwars2.com/images/e/e0/Mini_Ragged_White_Mantle_Figurehead.png"),
    HORREUR("Horreur sans âme", "https://wiki.guildwars2.com/images/d/d4/Mini_Desmina.png"),
    GRENTH("Statues of Grenth", "https://wiki.guildwars2.com/images/3/37/Mini_Broken_King.png"),
    DHUUM("Dhuum", "https://wiki.guildwars2.com/images/e/e4/Mini_Dhuum.png"),
    VG("Vale Guardian", "https://wiki-fr.guildwars2.com/images/0/0f/Mini-Gardien_de_la_Vall%C3%A9e.png");

    private String nom;
    private String url;
}
