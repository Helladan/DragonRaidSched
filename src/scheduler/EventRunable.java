package scheduler;

import bot.service.AnnonceGenerator;
import bot.domain.Data;
import bot.domain.Info;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RequestFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EventRunable implements Runnable{

    private TextChannel textChannel;
    private Data data;

    public EventRunable(Data data, TextChannel textChannel) {
        this.data = data;
        this.textChannel = textChannel;
    }

    @Override
    public void run() {
        try {
            Info info = data.getInfos().get(textChannel.getId());
            List<Message> messages = textChannel.getHistory().retrievePast(100).submit().get();
            User me = textChannel.getJDA().getSelfUser();
            for (Message message : messages) {
                if(!message.getAuthor().equals(me)) {
                    textChannel.deleteMessageById(message.getId()).submit();
                }
            }
            info.setTarget(null);
            info.setMode(null);
            info.setIsPresent(new ArrayList<>());
            info.setAnnonceId(textChannel.sendMessage(AnnonceGenerator.getAnnonce(data, textChannel)).submit().get().getId());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.err);
        }
    }
}
