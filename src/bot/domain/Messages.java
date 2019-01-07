package bot.domain;

import bot.service.ProcessMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Messages {
	START(ProcessMessage.START + " j hh:mm d ou " + ProcessMessage.START + " -s j hh:mm\n"
			+ " - [-s] pour une seul soirée (mode singleton)"
			+ " - j : jour en toutes lettres (lundi, mardi, mercredi, etc...)\n"
			+ " - hh:mm : heure de début (format 24h)\n"
			+ " - d : durÃ©e du raid en heure (c'est le temps avant la prochaine annonce)"),
	DROIT("Tu n'as pas les droits pour cette commande (Géré les messages)"),
	NOT_START("Aucune annonce de faite."),
	MAUVAISE_COMMANDE("Commande non-comprise"),
	TAPE_HELP("tape " + ProcessMessage.HELP + " pour de l'aide"),
	INSCRITPION("merci de t'inscrire (entre les emotes des divers classes que tu peux jouer en Raid)"),
	OVERFLOW("10 personnes sont dÃ©jÃ  inscrites, tu seras en reserve"),
	NO_INSCRIPTION("Aucune inscription n'avait Ã©tÃ© faite");
	private String message; 
}
