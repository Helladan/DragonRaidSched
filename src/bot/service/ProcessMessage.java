package bot.service;

import bot.domain.Commande;
import bot.domain.Data;
import bot.domain.Info;
import bot.domain.Messages;
import bot.domain.Mode;
import bot.domain.Cible;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;
import scheduler.EventScheduler;

import java.util.*;
import java.util.regex.Pattern;

public class ProcessMessage {
	private static final String START_SINGLETON_MODE = "-s ";
	private final static String START_CMD = "$";
	private final static List<String> PRESENT = new ArrayList<>(Arrays.asList(START_CMD + "p", START_CMD + "present", START_CMD + "présent"));
	private final static List<String> RESERVE = new ArrayList<>(Arrays.asList(START_CMD + "r", START_CMD + "reserve", START_CMD + "réserve"));
	private final static List<String> NON_PRESENT = new ArrayList<>(
			Arrays.asList(START_CMD + "np", START_CMD + "non-present", START_CMD + "non-présent"));
	private final static String MODE = START_CMD + "m";
	private final static String TARGET = START_CMD + "t";
	private final static String RAID_LEAD = START_CMD + "rl";
	public final static String HELP = START_CMD + "help";
	public final static String START = START_CMD + "start";
	
	private final static String LUNDI    = "lundi";
	private final static String MARDI    = "mardi";
	private final static String MERCREDI = "mercredi";
	private final static String JEUDI    = "jeudi";
	private final static String VENDREDI = "vendredi";
	private final static String SAMEDI   = "samedi";
	private final static String DIMANCHE = "dimanche";
	
	public final static String START_REGEX_DAYS = "(" + LUNDI + "|" + MARDI + "|" + MERCREDI + "|" + JEUDI + "|" + VENDREDI + "|" + SAMEDI + "|" + DIMANCHE + ")";
	public final static String START_REGEX_HOURS = "[0-9]{1,2}:[0-9]{2}";
	public final static String START_REGEX_BASE = START_REGEX_DAYS + " " + START_REGEX_HOURS;
	public final static String START_REGEX = "("+ START_SINGLETON_MODE + START_REGEX_BASE + ")|(" + START_REGEX_BASE + " [1-9][0-9]*)";


	public static boolean process(MessageReceivedEvent event, Data data) {
		RestAction<PrivateChannel> privateChannel = event.getAuthor().openPrivateChannel();
		Info info = data.getInfos().get(event.getTextChannel().getId());
		if (info == null) {
			info = new Info();
			data.getInfos().put(event.getTextChannel().getId(), info);
		}
		String pseudo = event.getAuthor().getName();
		Message message = event.getMessage();
		String contentDisplay = message.getContentDisplay();
		if (isCommande(contentDisplay.toLowerCase())) {
			Commande commande = getCommande(contentDisplay.toLowerCase());
			if (info.getAnnonceId() != null || Commande.START.equals(commande)) {
				if (info.getIsPresent() == null) {
					info.setIsPresent(new ArrayList<>());
				}
				switch (commande) {
				case PRESENT:
					if (data.getPlayerMap().containsKey(message.getAuthor().getName())) {
						addPlayer(pseudo, info, privateChannel);
						return sortAndRefrech(info);
					} else {
						privateChannel.complete().sendMessage(Messages.INSCRITPION.getMessage()).submit();
					}
					break;
				case RESERVE:
					if (data.getPlayerMap().containsKey(message.getAuthor().getName())) {
						addReserve(pseudo, info);
						return sortAndRefrech(info);
					} else {
						privateChannel.complete().sendMessage(Messages.INSCRITPION.getMessage()).submit();
					}
					break;
				case NON_PRESENT:
					if (info.getIsPresent().contains(pseudo) || info.getReserve().contains(pseudo)) {
						removePlayer(pseudo, info);
						return sortAndRefrech(info);
					} else {
						privateChannel.complete().sendMessage(Messages.NO_INSCRIPTION.getMessage()).submit();
					}
					break;
				case MODE:
				case TARGET:
				case RAID_LEAD:
				case START:
					return applyCommande(event, privateChannel, data, commande);
				case HELP:
					String msg = "";
					String param = getParams(contentDisplay);
					if (param != null) {
						param = param.toLowerCase();
						Commande helpCommande = getCommande(param);
						if(helpCommande != null) {
							switch (helpCommande) {
							case MODE:
								msg += "mode de jeu.\nPeut prendre n'importe quel texte, mais ceux-ci sont reconus :\n";
								for(Mode mode : Mode.values()) {
									msg += " - " + mode.name() + " (" + mode.getNom() + ")\n";
								}
								break;
							case TARGET:
								msg += "cible du Raid.\nPeut prendre n'importe quel texte, mais ceux-ci sont reconus :\n";
								for(Cible cible : Cible.values()) {
									msg += " - " + cible.name() + " (" + cible.getNom() + ")\n";
								}
								break;
							case START:
								msg += "set le jour des raid.\nFormat : \n" + Messages.START.getMessage();
								break;
							default:
								msg += "pas d'explication particulière sur cette commande.";
							}
						} else {
							privateChannel.complete().sendMessage(Messages.MAUVAISE_COMMANDE.getMessage()).submit();
						}
					} else {
						msg += String.join(" ou ", PRESENT) + " : s'inscrire pour le Raid\n"
								// + String.join(" ou ", RESERVE) + " : s'inscrire en reserve\n"
								+ String.join(" ou ", NON_PRESENT) + " : se désinscrire\n" 
								+ HELP + " : afficher l'aide\n";
						if (hasPermition(event)) {
							msg += START + " : régler le moment de l'evenement sur le canal\n"
								+ MODE + " : mode de jeu\n"
								+ TARGET + " : cible du raid\n" 
								+ RAID_LEAD + " : se marquer comme raid lead";
						}
					}
					privateChannel.complete().sendMessage(msg).submit();
					return false;
				}
			} else {
				String msg = Messages.NOT_START.getMessage();
				if (hasPermition(event)) {
					msg += "\n" + Messages.START.getMessage();
				}
				privateChannel.complete().sendMessage(msg).submit();
			}
		} else if (isInscription(contentDisplay.toLowerCase())) {
			List<Emote> emotes = message.getEmotes();
			String classes = "";
			for (Emote emote : emotes) {
				classes += emote.getAsMention();
			}
			data.getPlayerMap().put(pseudo, classes);
			addPlayer(pseudo, info, privateChannel);
			return true;
		} else {
			String msg = Messages.MAUVAISE_COMMANDE.getMessage();
			msg += "\n" + Messages.TAPE_HELP.getMessage();
			privateChannel.complete().sendMessage(msg).submit();
		}
		return false;
	}

	private static boolean sortAndRefrech(Info info) {
		info.getIsPresent().sort(new RaidLeadFirst(info.getRaidLead()));
		return true;
	}

	private static boolean isInscription(String message) {
		Pattern pattern = Pattern.compile("(:.*: *)*");
		return pattern.matcher(message).matches();
	}

	private static Commande getCommande(String message) {
		message = message.split(" ")[0];
		if (PRESENT.contains(message)) {
			return Commande.PRESENT;
		}
		if (NON_PRESENT.contains(message)) {
			return Commande.NON_PRESENT;
		}/*
		if (RESERVE.contains(message)) {
			return Commande.RESERVE;
		}*/
		if (message.equals(START)) {
			return Commande.START;
		}
		if (message.equals(MODE)) {
			return Commande.MODE;
		}
		if (message.equals(TARGET)) {
			return Commande.TARGET;
		}
		if (message.equals(RAID_LEAD)) {
			return Commande.RAID_LEAD;
		}
		if (message.equals(HELP)) {
			return Commande.HELP;
		}
		return null;
	}

	private static void addPlayer(String pseudo, Info info, RestAction<PrivateChannel> privateChannel) {
		List<String> isPresent = info.getIsPresent();
		List<String> reserve = info.getReserve();
		if (!isPresent.contains(pseudo)) {
			isPresent.add(pseudo);
			reserve.remove(pseudo);
			if (isPresent.size() > 10) {
				privateChannel.complete().sendMessage(Messages.OVERFLOW.getMessage()).submit();
			}
		}
	}

	private static void addReserve(String pseudo, Info info) {
		List<String> isPresent = info.getIsPresent();
		List<String> reserve = info.getReserve();
		if (!reserve.contains(pseudo)) {
			isPresent.remove(pseudo);
			reserve.add(pseudo);
		}
	}

	private static void removePlayer(String pseudo, Info info) {
		List<String> isPresent = info.getIsPresent();
		List<String> reserve = info.getReserve();
		isPresent.remove(pseudo);
		reserve.remove(pseudo);
	}

	private static boolean isCommande(String message) {
		return getCommande(message) != null;
	}

	private static String getParams(String message) {
		return (message.split(" ").length > 1 ? message.substring(message.split(" ")[0].length() + 1) : null);
	}

	private static boolean applyCommande(MessageReceivedEvent event, RestAction<PrivateChannel> privateChannel,
			Data data, Commande commande) {
		Info info = data.getInfos().get(event.getTextChannel().getId());

		if (hasPermition(event)) {
			String message = event.getMessage().getContentDisplay();
			String params = getParams(message);
			switch (commande) {
			case TARGET:
				info.setTarget(params);
				break;
			case MODE:
				info.setMode(params);
				break;
			case RAID_LEAD:
				if (params != null && !"".equals(params)) {
					info.setRaidEmote(event.getMessage().getEmotes().get(0).getAsMention());
					info.setRaidLead(event.getAuthor().getName());
				} else {
					info.setRaidEmote(null);
					info.setRaidLead(null);
				}
				info.getIsPresent().sort(new RaidLeadFirst(info.getRaidLead()));
				break;
			case START:
				if (Pattern.compile(START_REGEX).matcher(params).find()) {
					data.getInfos().remove(event.getTextChannel().getId());
					info = new Info();
					if(params.contains(START_SINGLETON_MODE)) {
						params = params.replaceAll(START_SINGLETON_MODE, "");
					}else {
						info.setTime(Integer.parseInt(params.split(" ")[2]));
					}
					String paramTabs[] = params.split(" ");
					switch (paramTabs[0]) {
					case LUNDI:
						info.setDayOfWeek(Calendar.MONDAY);
						break;

					case MARDI:
						info.setDayOfWeek(Calendar.TUESDAY);
						break;

					case MERCREDI:
						info.setDayOfWeek(Calendar.WEDNESDAY);
						break;

					case JEUDI:
						info.setDayOfWeek(Calendar.THURSDAY);
						break;

					case VENDREDI:
						info.setDayOfWeek(Calendar.FRIDAY);
						break;

					case SAMEDI:
						info.setDayOfWeek(Calendar.SATURDAY);
						break;
					case DIMANCHE:
						info.setDayOfWeek(Calendar.SUNDAY);
						break;
					default:
						break;
					}
					String time[] = paramTabs[1].split(":");
					info.setHour(Integer.parseInt(time[0]));
					info.setMinute(Integer.parseInt(time[1]));
					data.getInfos().put(event.getTextChannel().getId(), info);
					EventScheduler.update(data, event.getJDA().getGuilds());
				} else {
					privateChannel.complete()
							.sendMessage(Messages.MAUVAISE_COMMANDE.getMessage() + "\n" + Messages.START.getMessage())
							.submit();
				}
				break;
			}
			return true;
		} else {
			privateChannel.complete().sendMessage(Messages.DROIT.getMessage()).submit();
		}
		return false;
	}

	private static boolean hasPermition(MessageReceivedEvent event) {
		return event.getMember().getPermissions(event.getTextChannel()).contains(Permission.MESSAGE_MANAGE);
	}

	@AllArgsConstructor
	private static class RaidLeadFirst implements Comparator<String> {

		String readLead;

		@Override
		public int compare(String o1, String o2) {
			if (o1.equals(readLead)) {
				return -1;
			}
			if (o2.equals(readLead)) {
				return 1;
			}
			return 0;
		}
	}
}
