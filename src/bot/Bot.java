package bot;

import bot.domain.*;
import bot.service.AnnonceGenerator;
import bot.service.ProcessMessage;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import scheduler.EventScheduler;
import scheduler.SaveRunable;
import static java.lang.String.format;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.concurrent.ExecutionException;

public class Bot {
    private JDA jda;
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
                        info.setAnnonceId(textChannel.sendMessage(AnnonceGenerator.getAnnonce(data, textChannel)).submit().get().getId());
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
        message.editMessage(AnnonceGenerator.getAnnonce(data, message.getTextChannel()))
                .submit();
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
}
