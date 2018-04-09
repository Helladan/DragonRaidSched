package bot;

import bot.domain.Datas;
import bot.domain.Infos;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import scheduler.EventRunable;
import scheduler.EventScheduler;
import scheduler.SaveRunable;
import static java.lang.String.format;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Bot {
    public static final String SAY_PRESENT = "Présent";
    private JDA jda;
    private final static String SAY_TARGET = "Cible";
    private final static String SAY_MODE = "Mode";
    private final static String SAY_RRESERVE = "Reserve";
    Datas data = new Datas();

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
                    Infos infos = data.getInfos().get(textChannel.getId());
                    if(infos == null){
                        infos = new Infos();
                        data.getInfos().put(textChannel.getId(), infos);
                    }
                    new EventScheduler(textChannel, data);
                        refrechAnnonce(getMessageById(textChannel, infos.getAnnonceId()));
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
                    Infos infos = data.getInfos().get(textChannel.getId());
                    refrechAnnonce(getMessageById(textChannel, infos.getAnnonceId()));
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
            data = ((Datas) ois.readObject());
            ois.close();
        }else{
            File folder = new File(SaveRunable.PRESENCE_FOLDER);
            TextChannel textChannel = null;
            Infos infos = new Infos();
            if (folder.exists()) {
                infos.setIsPresent(new ArrayList<>());
                for (File presencesFile : folder.listFiles()) {
                    FileInputStream fis = fis = new FileInputStream(presencesFile);
                    ObjectInputStream ois = ois = new ObjectInputStream(fis);
                    String chanelId = presencesFile.getName().replace(format(SaveRunable.PRESENCE_FILE, ""), "");
                    textChannel = jda.getTextChannelById(chanelId);
                    infos.getIsPresent().addAll((List<String>) ois.readObject());
                    ois.close();
                    if(textChannel != null) {
                        data.getInfos().put(textChannel.getId(), infos);
                    }
                }
            }
            file = new File(SaveRunable.PLAYER_FILE);
            if (file.exists()) {
                FileInputStream fis = fis = new FileInputStream(file);
                ObjectInputStream ois = ois = new ObjectInputStream(fis);
                data.setPlayerMap((Map<String, String>) ois.readObject());
                ois.close();
            }
        }
    }

    private void refrechAnnonce(Message message) {
        Infos infos = data.getInfos().get(message.getTextChannel().getId());
        Guild guild = message.getGuild();
        Message newMessage = message;
        message.editMessage(getAnnonce(infos, guild))
                .submit();
    }

    private MessageEmbed getAnnonce(Infos infos, Guild guild){
        EmbedBuilder annonceBuilder = new EmbedBuilder();
        annonceBuilder.setTitle(EventRunable.getMessage());
        annonceBuilder.setColor(Color.RED);
        if(infos.getTarget() != null && !"".equals(infos.getTarget())){
            annonceBuilder.addField(SAY_TARGET, infos.getTarget(), true);
        }
        if(infos.getMode()!= null && !"".equals(infos.getMode())){
            annonceBuilder.addField(SAY_MODE, infos.getMode(), true);
        }
        annonceBuilder.addBlankField(false);
        List<String> presents = infos.getIsPresent().subList(0, infos.getIsPresent().size() <= 10 ? infos.getIsPresent().size() : 10);
        for(String present : presents){
            Member member = guild.getMembersByName(present, false).get(0);
            String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
            annonceBuilder.addField(userName, data.getPlayerMap().get(present), false);
        }
        if(infos.getIsPresent().size()> 10){
            annonceBuilder.addBlankField(false);
            for(String present : infos.getIsPresent().subList(10, infos.getIsPresent().size())){
                Member member = guild.getMembersByName(present, false).get(0);
                String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
                annonceBuilder.addField(userName + "[" + SAY_RRESERVE + "]", data.getPlayerMap().get(present), false);
            }
        }
        return annonceBuilder.build();
    }

    private Message getMessageById(TextChannel textChannel, String id){
        return textChannel.getMessageById(id).complete();
    }
}
