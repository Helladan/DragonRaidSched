package bot.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Messages {
	START("/start j hh:mm d\n"
			+ " - j : jour en toutes lettres (lundi, mardi, mercredi, etc...)\n"
			+ " - hh:mm : heure de début (format 24h)\n"
			+ " - d : duré du raid en heure (c'est le temps avant la prochaine annonce)"),
	DROIT("Tu n'as pas les droits pour cette commande (Gérer les messages)"),
	NOT_START("Aucune annonce de faite."),
	MAUVAISE_COMMANDE("Commande non-comprise"),
	TAPE_HELP("tape /help pour de l'aide"),
	INSCRITPION("merci de t'inscrire (entre les emotes des divers classes que tu peux jouer en Raid)"),
	OVERFLOW("10 personnes sont déjà  inscrites, tu seras en reserve"),
	NO_INSCRIPTION("Aucune inscription avait été faite");
	private String message; 
}
