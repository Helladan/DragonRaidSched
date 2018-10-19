package bot.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Cible {
    VG("Vale Guardian", "https://wiki-fr.guildwars2.com/images/0/0f/Mini-Gardien_de_la_Vall%C3%A9e.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/vallee-des-esprits/le-gardien-de-la-vallee.html"),
    GORSEVAL("Gorseval le Disparate", "https://wiki.guildwars2.com/images/d/d1/Mini_Gorseval_the_Multifarious.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/vallee-des-esprits/gorseval.html"),
    SABETHA("Sabetha la saboteuse", "https://wiki.guildwars2.com/images/5/54/Mini_Sabetha.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/vallee-des-esprits/sabetha.html"),
    PARESSOR("Paressor", "https://wiki.guildwars2.com/images/1/12/Mini_Slothasor.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/passage-de-la-redemption/paressor.html"),
    BANDITS("Trio de Bandits", "https://wiki.guildwars2.com/images/5/5a/Mini_Narella.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/passage-de-la-redemption/trio-de-bandits-sauver-les-prisonniers.html"),
    MATTHIAS("Matthias Gabrel", "https://wiki.guildwars2.com/images/5/5d/Mini_Matthias_Abomination.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/passage-de-la-redemption/matthias-gabrel.html"),
    ESCORTE("Assaut sur la Forteresse", "https://wiki.guildwars2.com/images/b/b5/Mini_McLeod_the_Silent.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/forteresse-des-fideles/assaut-sur-la-forteresse.html"),
    TITAN("Titan du fort", "https://wiki.guildwars2.com/images/e/ea/Mini_Keep_Construct.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/forteresse-des-fideles/le-titan-du-fort.html"),
    CHATEAU("Le Château corrompu", "https://cdn.discordapp.com/attachments/363023600376348684/433233933799391242/Sans_titre.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/forteresse-des-fideles/le-chateau-corrompu.html"),
    XERA("Xera", "https://wiki.guildwars2.com/images/4/4b/Mini_Xera.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/forteresse-des-fideles/xera.html"),
    CAIRN("Cairn l'Indomptable", "https://wiki-fr.guildwars2.com/images/e/e7/Mini-Cairn_l%27Indomptable.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/bastion-du-penitent/cairn-l-indomptable.html"),
    MURSAAT("Surveillant mursaat", "https://wiki-fr.guildwars2.com/images/3/39/Mini-surveillant_mursaat.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/bastion-du-penitent/surveillant-mursaat.html"),
    SAMAROG("Valerie Damidot", "https://wiki.guildwars2.com/images/f/f0/Mini_Samarog.png",
            "http://www.lebusmagique.fr/pages/expeditions/raids/bastion-du-penitent/samarog.html"),
    DEIMOS("Deimos", "https://wiki.guildwars2.com/images/e/e0/Mini_Ragged_White_Mantle_Figurehead.png", null),
    HORREUR("Horreur sans âme", "https://wiki.guildwars2.com/images/d/d4/Mini_Desmina.png", null),
    GRENTH("Statues de Grenth", "https://wiki.guildwars2.com/images/3/37/Mini_Broken_King.png", null),
    DHUUM("Dhuum", "https://wiki.guildwars2.com/images/e/e4/Mini_Dhuum.png", null),
    KENUT("Kenut", "https://render.guildwars2.com/file/453C959040B6AF7F639FDD78367AF39FD7C73246/2038614.png", null),
    ZOMMOROS("Zommoros", "https://render.guildwars2.com/file/B316A9FAA3275D0EF6D84A9179E062BF10C4545A/2038619.png", null);

    private String nom;
    private String imageUrl;
    private String tutoUrl;
}
