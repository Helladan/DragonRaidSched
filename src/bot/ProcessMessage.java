package bot;

import bot.domain.Data;
import bot.domain.Datas;
import bot.domain.Info;
import bot.domain.Infos;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.*;
import java.util.regex.Pattern;

public class ProcessMessage {
    private final static List<String> PRESENT = new ArrayList<String>(Arrays.asList(new String[]{"p", "present", "présent"}));
    private final static List<String> NON_PRESENT = new ArrayList<String>(Arrays.asList(new String[]{"np", "non-present", "non-présent"}));
    private final static String MODE = "/m";
    private final static String TARGET = "/t";
    private final static String RAID_LEAD = "/rl";


    public static boolean process(MessageReceivedEvent event, Data data) {
        RestAction<PrivateChannel> privateChannel = event.getAuthor().openPrivateChannel();
        boolean needRefrech = false;
        Info info = data.getInfos().get(event.getTextChannel().getId());
        if(info == null){
            data.getInfos().put(event.getTextChannel().getId(), new Info());
        }
        if (info.getAnnonceId() != null) {
            Message message = event.getMessage();
            String pseudo = event.getAuthor().getName();
            if(info.getIsPresent() == null){
                info.setIsPresent(new ArrayList<>());
            }
            List<String> presents = info.getIsPresent();
            String contentDisplay = message.getContentDisplay();
            if(isCommande(contentDisplay.toLowerCase())){
                Commande commande = getCommande(contentDisplay.toLowerCase());
                switch (commande){
                    case PRESENT:
                        if (data.getPlayerMap().containsKey(message.getAuthor().getName())) {
                            addPlayer(pseudo, presents, privateChannel);
                            needRefrech = true;
                        } else {
                            privateChannel.complete().sendMessage("T'es qui ?!").submit();
                        }
                        break;
                    case NON_PRESENT:
                        if(presents.contains(pseudo)){
                            presents.remove(pseudo);
                            needRefrech = true;
                        }else{
                            privateChannel.complete().sendMessage("Tu été pas compté de toutes maniére").submit();
                        }
                        break;
                    case MODE:
                    case TARGET:
                    case RAID_LEAD:
                        needRefrech = applyCommande(event, privateChannel, info, commande);
                        break;
                }
            }else if (isInscription(contentDisplay.toLowerCase())){
                List<Emote> emotes = message.getEmotes();
                String classes = "";
                for (Emote emote : emotes) {
                    classes += emote.getAsMention();
                }
                data.getPlayerMap().put(pseudo, classes);
                addPlayer(pseudo, presents, privateChannel);
                needRefrech = true;
            }else{
                privateChannel.complete().sendMessage("Arrete de raconter de la merde !").submit();
            }
        } else {
            privateChannel.complete().sendMessage("Aucune annonce n'a été faites ici connard !").submit();
        }
        return needRefrech;
    }

    private static boolean isInscription(String message) {
        Pattern pattern = Pattern.compile("[:[a-z|_]*:[ ]*]*");
        return pattern.matcher(message).matches();
    }

    private static Commande getCommande(String message){
        if(PRESENT.contains(message)){
            return Commande.PRESENT;
        }
        if(NON_PRESENT.contains(message)){
            return Commande.NON_PRESENT;
        }
        if (message.startsWith(MODE)){
            return Commande.MODE;
        }
        if (message.startsWith(TARGET)){
            return Commande.TARGET;
        }
        if (message.startsWith(RAID_LEAD)){
            return Commande.RAID_LEAD;
        }
        return null;
    }

    private static void addPlayer(String pseudo, List<String> presents, RestAction<PrivateChannel> privateChannel) {
        if(!presents.contains(pseudo)) {
            presents.add(pseudo);
            if(presents.size() > 10){
                privateChannel.complete().sendMessage("10 personnes sont déjà inscrites, tu seras sur le banc de touche").submit();
            }
        }
    }

    private static boolean isCommande(String message){
        return getCommande(message) != null;
    }

    private static String getParams(String message){
        return (message.split(" ").length > 1?message.substring(message.split(" ")[0].length() + 1):null);
    }

    private static boolean applyCommande(MessageReceivedEvent event, RestAction<PrivateChannel> privateChannel, Info info, Commande commande){

        if(hasPermition(event)){
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
                    } else{
                        info.setRaidEmote(null);
                        info.setRaidLead(null);
                    }
                    break;
            }
            return true;
        }else{
            privateChannel.complete().sendMessage("Touche pas à ça petit con !").submit();
        }
        return false;
    }
    private static boolean hasPermition(MessageReceivedEvent event){
        return event.getMember().getPermissions(event.getTextChannel()).contains(Permission.MESSAGE_MANAGE);
    }
}
