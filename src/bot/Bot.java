package bot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.RestAction;
import scheduler.EventScheduler;
import scheduler.SaveRunable;
import static java.lang.String.format;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Bot {
    private final static List<String> PRESENT = new ArrayList<String>(Arrays.asList(new String[]{"p", "present", "présent"}));
    private final static List<String> NON_PRESENT = new ArrayList<String>(Arrays.asList(new String[]{"np", "non-present", "non-présent"}));

    private JDA jda;
    private Map<String, String> playerMap = new HashMap<>();
    private Map<TextChannel, List<String>> isPresent = new HashMap<>();
    private Map<TextChannel, Message> annonces = new HashMap<>();

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
                    new EventScheduler(textChannel, annonces, isPresent, playerMap);
                    if(annonces.get(textChannel) != null){
                        refrechAnnonce(annonces.get(textChannel));
                    }
                }
            }
        }
        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(MessageReceivedEvent event) {
            System.out.println(((event.getGuild() != null) ? "[" + event.getGuild().getName() + "]" : "") + "{" + ((event.getTextChannel() != null) ? event.getTextChannel().getName() : event.getPrivateChannel().getName()) + "}");
            System.out.println("    " + event.getAuthor().getName() + " : " + event.getMessage().getContentDisplay());
            if (!event.getAuthor().equals(jda.getSelfUser())) {
                RestAction<PrivateChannel> privateChannel = event.getAuthor().openPrivateChannel();
                if (annonces.containsKey(event.getTextChannel())) {
                    Message message = event.getMessage();
                    String pseudo = event.getAuthor().getName();
                    if(!isPresent.containsKey(event.getTextChannel())){
                        isPresent.put(event.getTextChannel(), new ArrayList<>());
                    }
                    List<String> presents = isPresent.get(event.getTextChannel());
                    if (PRESENT.contains(message.getContentDisplay().toLowerCase())) {
                        if (playerMap.containsKey(message.getAuthor().getName())) {
                            addPlayer(pseudo, presents, privateChannel);
                            refrechAnnonce(annonces.get(event.getTextChannel()));
                        } else {
                            privateChannel.complete().sendMessage("T'es qui ?!").submit();
                        }
                    } else if(NON_PRESENT.contains(message.getContentDisplay().toLowerCase())){
                        if(presents.contains(pseudo)){
                            presents.remove(pseudo);
                            refrechAnnonce(annonces.get(event.getTextChannel()));
                        }else{
                            privateChannel.complete().sendMessage("Tu été pas compté de toutes maniére").submit();
                        }
                    } else  {
                        Pattern pattern = Pattern.compile("[:[a-z|_]*:[ ]*]*");
                        if (pattern.matcher(message.getContentDisplay()).matches()) {
                            List<Emote> emotes = message.getEmotes();
                            String classes = "";
                            for (Emote emote : emotes) {
                                classes += emote.getAsMention();
                            }
                            playerMap.put(pseudo, classes);
                            addPlayer(pseudo, presents, privateChannel);
                            refrechAnnonce(annonces.get(event.getTextChannel()));
                        } else {
                            privateChannel.complete().sendMessage("Arrete de raconter de la merde !").submit();
                        }
                    }
                } else {
                    privateChannel.complete().sendMessage("Aucune annonce n'a été faites ici connard !").submit();
                }
                event.getTextChannel().deleteMessageById(event.getMessageId()).submit();
            }
            }
        });
    }

    private void retriveSave() throws IOException, ClassNotFoundException, ExecutionException, InterruptedException {
        File folder = new File(SaveRunable.PRESENCE_FOLDER);
        if(folder.exists()) {
            this.isPresent = new HashMap<>();
            for (File presencesFile : folder.listFiles()){
                FileInputStream fis = fis = new FileInputStream(presencesFile);
                ObjectInputStream ois = ois = new ObjectInputStream(fis);
                String chanelId = presencesFile.getName().replace(format(SaveRunable.PRESENCE_FILE, ""), "");
                TextChannel textChannel = jda.getTextChannelById(chanelId);
                this.isPresent.put(textChannel, (List<String>) ois.readObject());
                ois.close();
            }
        }
        File file = new File(SaveRunable.PLAYER_FILE);
        if(file.exists()){
            FileInputStream fis = fis = new FileInputStream(file);
            ObjectInputStream ois = ois = new ObjectInputStream(fis);
            this.playerMap = (Map<String, String>) ois.readObject();
            ois.close();
        }
    }

    private void addPlayer(String pseudo, List<String> presents, RestAction<PrivateChannel> privateChannel) {
        if(!presents.contains(pseudo)) {
            presents.add(pseudo);
            if(presents.size() > 10){
                privateChannel.complete().sendMessage("10 personnes sont déjà inscrites, tu seras sur le banc de touche").submit();
            }
        }
    }

    private void refrechAnnonce(Message message) {
        String annonce = message.getContentDisplay();
        int i = 0;
        if(isPresent.get(message.getTextChannel()) != null ) {
            for (String present : isPresent.get(message.getTextChannel())) {
                annonce += "\n";
                if (i == 10) {
                    annonce += " ==== Reserve ====\n";
                }
                Member member = message.getGuild().getMembersByName(present, false).get(0);
                String userName = (member.getNickname() != null) ? member.getNickname() : member.getUser().getName();
                annonce += "    " + userName + " => " + playerMap.get(present);
                i++;
            }
        }
        message.editMessage(annonce).submit();
    }
}
