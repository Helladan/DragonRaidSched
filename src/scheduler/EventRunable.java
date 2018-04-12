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
            RequestFuture<List<Message>> historyRequest = textChannel.getHistory().retrievePast(100).submit();
            User me = textChannel.getJDA().getSelfUser();
            do {
                Thread.sleep(1000);
            } while (!historyRequest.isDone());
            for (Message message : historyRequest.get()) {
                if(!message.getAuthor().equals(me) || isCurrentMessage(message)) {
                    textChannel.deleteMessageById(message.getId()).submit();
                }
            }
            info.setTarget(null);
            info.setMode(null);
            info.setIsPresent(new ArrayList<>());
            RequestFuture<Message> request = textChannel.sendMessage(AnnonceGenerator.getAnnonce(data, textChannel)).submit();
            do {
                Thread.sleep(1000);
            } while (!request.isDone());
            info.setAnnonceId(request.get().getId());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.err);
        }
    }
    private boolean isCurrentMessage(Message message){
        if(!message.getEmbeds().isEmpty()){
            MessageEmbed embed = message.getEmbeds().get(0);
            return AnnonceGenerator.getMessage().equals(embed.getTitle());
        }
        return true;
    }
}
