package bot.service;

import static java.lang.String.format;

import java.awt.Color;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import bot.domain.Cible;
import bot.domain.Data;
import bot.domain.Info;
import bot.domain.Mode;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import scheduler.EventScheduler;

public class AnnonceGenerator {
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
    private final static DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.FRANCE);
    private final static String SAY_DATE = "Raid du %s à %s";
    private final static String SAY_MODE = "Mode : %s";
    private final static String SAY_TARGET = "**%s**\n";
    private final static String SAY_RESERVE = "⟾ __**Reserve**__ ⟽";
    private final static String SAY_COMBATTANT= "⟾ __**Combattant**__ ⟽";
    private final static String SAY_NB_INSCRIT = "   *%s inscrit%s*\n";
    private final static String SAY_NB_RESERVE = "   *%s en reserve*";
    
    private final static String SAY_ACHIEVEMENT = "★ **État de l'escouade :**";
    private final static String SUCCESS_OVERTEN = "Stack Overflow _(+ de 10 joueurs inscrits)_";
    private final static String SUCCESS_ONE = "Héros _(un seul inscrit)_";
    private final static String SUCCESS_FIVE = "Format Donjon _(5 inscrits)_";
    private final static String SUCCESS_TEN = "Roster parfait _(10 inscrits)_";
    private final static String SUCCESS_RESERVE = "Poules mouillées _(plus de joueurs en réserve qu'inscrits)_";
    private final static String SUCCESS_BALANCED = "\"Perfectly balanced, as all things should be.\" - Thanos";

    public static String getMessage(Info info) {
        Calendar calendar = EventScheduler.getNextSchedul(info);
        return String.format(SAY_DATE,
                DATE_FORMAT.format(calendar.getTime()),
                TIME_FORMAT.format(calendar.getTime()));
    }

    public static boolean isCurrentMessage(Message message, Info info){
        if(!message.getEmbeds().isEmpty()){
            MessageEmbed embed = message.getEmbeds().get(0);
            return AnnonceGenerator.getMessage(info).equals(embed.getTitle());
        }
        return true;
    }

    public static MessageEmbed getAnnonce(Data data, TextChannel textChannel){
        Info info = data.getInfos().get(textChannel.getId());
        Guild guild = textChannel.getGuild();
        EmbedBuilder annonceBuilder = new EmbedBuilder();
        String achievement = null;
        if(info.getTime() != 0) {
        	annonceBuilder.setColor(Color.BLUE);
        }else {
        	annonceBuilder.setColor(Color.MAGENTA);
        }
        String description = "";
        Cible cible = null;
        if(info.getTarget() != null && !"".equals(info.getTarget())){
            try{
                cible = Cible.valueOf(info.getTarget().toUpperCase());
                description = cible.getNom();
                annonceBuilder.setThumbnail(cible.getImageUrl());
            }catch (IllegalArgumentException e){
                description =info.getTarget();
            }
            description = format(SAY_TARGET, description);
        }
        annonceBuilder.setTitle(getMessage(info), cible!=null?cible.getTutoUrl():null);
        int presentSize = info.getIsPresent().size();
        int reserveSize = info.getReserve().size();
        description += format(SAY_NB_INSCRIT, presentSize + "/10", presentSize >1?"s":"");
        if(reserveSize > 0){
            description += format(SAY_NB_RESERVE, reserveSize);
        }
        annonceBuilder.setDescription(description);
        if(info.getMode()!= null && !"".equals(info.getMode())){
            try{
                Mode mode = Mode.valueOf(info.getMode().toUpperCase());
                annonceBuilder.setFooter(format(SAY_MODE, mode.getNom()), mode.getUrl());
            }catch (IllegalArgumentException e){
                annonceBuilder.setFooter(format(SAY_MODE, info.getMode()), null);
            }
        }
        for(String user : new ArrayList<>(info.getIsPresent())) {
        	if(guild.getMembersByName(user, false).isEmpty()) {
            	info.getIsPresent().remove(user);
            	return getAnnonce(data, textChannel);
        	}
        }
        for(String user : new ArrayList<>(info.getReserve())) {
        	if(guild.getMembersByName(user, false).isEmpty()) {
            	info.getReserve().remove(user);
            	return getAnnonce(data, textChannel);
        	}
        }
        List<String> presents = info.getIsPresent().subList(0, presentSize <= 10 ? presentSize : 10);
        if(presentSize>0) {
            annonceBuilder.addField("", SAY_COMBATTANT, false);
        }
        for(String present : presents){
            List<Member> members = guild.getMembersByName(present, false);
			Member member = members.get(0);
            String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
            String name = getName(userName);
            if(present.equals(info.getRaidLead())){
                name = info.getRaidEmote() + " " + name;
            }
            annonceBuilder.addField(name, " ⇨ " + data.getPlayerMap().get(present), false);
        }
        if(presentSize > 10 || reserveSize >0){
            annonceBuilder.addField("", SAY_RESERVE, false);
        }
        if(presentSize > 10){
            for(String present : info.getIsPresent().subList(10, presentSize)){
                Member member = guild.getMembersByName(present, false).get(0);
                String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
                annonceBuilder.addField(getName(userName), " ⇨ " + data.getPlayerMap().get(present), false);
            }
            achievement = SUCCESS_OVERTEN;
        }
        if (presentSize > 1 && presentSize == reserveSize){achievement = SUCCESS_BALANCED;}
        if (reserveSize > presentSize){achievement = SUCCESS_RESERVE;}
        if (presentSize == 1){achievement = SUCCESS_ONE;}
        if (presentSize == 5){achievement = SUCCESS_FIVE;}
        if (presentSize == 10){achievement = SUCCESS_TEN;}
        
        for(String present : info.getReserve()){
            Member member = guild.getMembersByName(present, false).get(0);
            String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
            annonceBuilder.addField(getName(userName), " ⇨ " + data.getPlayerMap().get(present), false);
        }
        
        if (achievement!=null){
        	annonceBuilder.addBlankField(false);
            annonceBuilder.addField(SAY_ACHIEVEMENT,"	" + achievement, false);
        }
        
        
        return annonceBuilder.build();
    }

    private static String getName(String userName) {
        String name = "";
        for(int i = 0; i < userName.length(); i++){
            char c = userName.charAt(i);
            if(c<=255){
                name += c;
            }
        }
        return name;
    }
}
