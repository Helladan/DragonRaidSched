package bot;

import bot.domain.*;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import scheduler.EventRunable;
import scheduler.EventScheduler;
import scheduler.SaveRunable;
import static java.lang.String.format;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Bot {
    private JDA jda;
    private final static String SAY_MODE = "Mode : %s";
    private final static String SAY_TARGET = "**%s**\n";
    private final static String SAY_RESERVE = "⟾ __**Reserve**__ ⟽";
    private final static String SAY_COMBATTANT= "⟾ __**Combattant**__ ⟽";
    private final static String SAY_NB_INSCRIT = "   *%s inscrit%s*\n";
    private final static String SAY_NB_RESERVE = "   *%s en reserve*";
    private static Data data = new Data();

    public Bot(String token) throws LoginException, InterruptedException, ExecutionException, IOException, ClassNotFoundException {
        jda = new JDABuilder(AccountType.BOT).setToken(token).setBulkDeleteSplittingEnabled(false).buildBlocking();
        jda.getPresence().setGame(Game.of(Game.GameType.DEFAULT, "planifier"));
        System.out.println("Connecte avec: " + jda.getSelfUser().getName());
        int size = jda.getGuilds().size();
        System.out.println("Autorisé sur " + size + " serveur" + (size > 1 ? "s" : ""));
        for (Guild guild : jda.getGuilds()) {
            System.out.println("	 - " + guild.getName());
        }
        retriveSave();
        for (Guild guild : jda.getGuilds()) {
            for (TextChannel textChannel : guild.getTextChannels()) {
                if (textChannel.canTalk()) {
                    Info info = data.getInfos().get(textChannel.getId());
                    if(info == null){
                        info = new Info();
                        data.getInfos().put(textChannel.getId(), info);
                    }
                    new EventScheduler(textChannel, data);

                    Message message = getMessageById(textChannel, info.getAnnonceId());
                    if(message == null) {
                        info.setAnnonceId(textChannel.sendMessage(getAnnonce(data.getInfos().get(textChannel.getId()), textChannel.getGuild())).submit().get().getId());
                    }
                }
            }
        }
        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(MessageReceivedEvent event) {
                TextChannel textChannel = event.getTextChannel();
                System.out.println(((event.getGuild() != null) ? "[" + event.getGuild().getName() + "]" : "") + "{" + ((textChannel != null) ? textChannel.getName() : event.getPrivateChannel().getName()) + "}");
            System.out.println("    " + event.getAuthor().getName() + " : " + event.getMessage().getContentDisplay());
            if (!event.getAuthor().equals(jda.getSelfUser())) {
                if(ProcessMessage.process(event, data)){
                    Info info = data.getInfos().get(textChannel.getId());
                    refrechAnnonce(getMessageById(textChannel, info.getAnnonceId()));
                }
                textChannel.deleteMessageById(event.getMessageId()).submit();
            }
            }
        });
    }

    private void retriveSave() throws IOException, ClassNotFoundException, ExecutionException, InterruptedException {
        File file = new File(SaveRunable.DATA_FILE);
        if(file.exists()) {
            FileInputStream fis = fis = new FileInputStream(file);
            ObjectInputStream ois = ois = new ObjectInputStream(fis);
            data = ((Data) ois.readObject());
            ois.close();
        }
    }

    private void refrechAnnonce(Message message) {
        Info info = data.getInfos().get(message.getTextChannel().getId());
        Guild guild = message.getGuild();
        Message newMessage = message;
        message.editMessage(getAnnonce(info, guild))
                .submit();
    }

    public static MessageEmbed getAnnonce(Info info, Guild guild){
        EmbedBuilder annonceBuilder = new EmbedBuilder();
        annonceBuilder.setColor(Color.BLUE);
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
        annonceBuilder.setTitle(EventRunable.getMessage(), cible!=null?cible.getTutoUrl():null);
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
        Collections.sort(info.getIsPresent(), new RaidLeadFirst(info.getRaidLead()));
        List<String> presents = info.getIsPresent().subList(0, presentSize <= 10 ? presentSize : 10);
        if(presentSize>0) {
            annonceBuilder.addField("", SAY_COMBATTANT, false);
        }
        for(String present : presents){
            Member member = guild.getMembersByName(present, false).get(0);
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
        }
        for(String present : info.getReserve()){
            Member member = guild.getMembersByName(present, false).get(0);
            String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
            annonceBuilder.addField(getName(userName), " ⇨ " + data.getPlayerMap().get(present), false);
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

    private Message getMessageById(TextChannel textChannel, String id){
        if(id != null){
            try {
                return textChannel.getMessageById(id).complete();
            }catch (ErrorResponseException e){
                System.err.println("Annonce non trouvée");
                return null;
            }
        }else{
            return null;
        }
    }

    @AllArgsConstructor
    private static class RaidLeadFirst implements Comparator<String> {

        String readLead;

        @Override
        public int compare(String o1, String o2) {
            if(o1.equals(readLead)){
                return -1;
            }
            if (o2.equals(readLead)){
                return 1;
            }
            return 0;
        }
    }
}
