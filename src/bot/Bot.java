package bot;

import bot.domain.*;
import lombok.AllArgsConstructor;
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
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

public class Bot {
    private JDA jda;
    private final static String SAY_MODE = "Mode : %s";
    private final static String SAY_RESERVE = "Reserve";
    private final static String SAY_INSCRIT = "   *%s/10 inscrit";
    Data data = new Data();

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
                        refrechAnnonce(getMessageById(textChannel, info.getAnnonceId()));
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
            Object o = ois.readObject();
            try {
                data = ((Data) o);
            }catch (ClassCastException e){
                Datas datas = ((Datas) o);
                data.setPlayerMap(datas.getPlayerMap());
                Map<String, Info> infos = new HashMap<>();
                for(Map.Entry<String, Infos> oldInfo : datas.getInfos().entrySet()){
                    Info info = new Info();
                    info.setAnnonceId(oldInfo.getValue().getAnnonceId());
                    info.setIsPresent(oldInfo.getValue().getIsPresent());
                    info.setMode(oldInfo.getValue().getMode());
                    info.setTarget(oldInfo.getValue().getTarget());
                    infos.put(oldInfo.getKey(), info);
                }
                data.setInfos(infos);
            }
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

    private MessageEmbed getAnnonce(Info info, Guild guild){
        EmbedBuilder annonceBuilder = new EmbedBuilder();
        annonceBuilder.setTitle(EventRunable.getMessage());
        annonceBuilder.setColor(Color.RED);
        String description = "";
        if(info.getTarget() != null && !"".equals(info.getTarget())){
            try{
                Cible cible = Cible.valueOf(info.getTarget().toUpperCase());
                description = cible.getNom();
                annonceBuilder.setThumbnail(cible.getUrl());
            }catch (IllegalArgumentException e){
                description =info.getTarget();
            }
            description = "**" + description + "**\n";
        }
        int size = info.getIsPresent().size();
        description += String.format(SAY_INSCRIT, size) + (size>1?"s*":"*");
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
        List<String> presents = info.getIsPresent().subList(0, size <= 10 ? size : 10);
        for(String present : presents){
            Member member = guild.getMembersByName(present, false).get(0);
            String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
            String name = "";
            for(int i = 0; i < userName.length(); i++){
                char c = userName.charAt(i);
                if(c<=255){
                    name += c;
                }
            }
            if(present.equals(info.getRaidLead())){
                name = info.getRaidEmote() + " " + name;
            }
            annonceBuilder.addField(name, " ⇨ " + data.getPlayerMap().get(present), false);
        }
        if(size > 10){
            annonceBuilder.addBlankField(false);
            for(String present : info.getIsPresent().subList(10, size)){
                Member member = guild.getMembersByName(present, false).get(0);
                String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
                annonceBuilder.addField(userName + "[" + SAY_RESERVE + "]", " ⇨ " + data.getPlayerMap().get(present), false);
            }
        }
        return annonceBuilder.build();
    }

    @AllArgsConstructor
    private class RaidLeadFirst implements Comparator<String> {

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

    private Message getMessageById(TextChannel textChannel, String id){
        return textChannel.getMessageById(id).complete();
    }
}
