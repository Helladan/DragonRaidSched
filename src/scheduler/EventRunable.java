package scheduler;

import bot.Bot;
import bot.domain.Info;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RequestFuture;

import java.awt.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class EventRunable implements Runnable{
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
    private final static DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.FRANCE);
    private final static String SAY = "Raid du %s à %s";

    private TextChannel textChannel;
    private Info info;

    public EventRunable(TextChannel textChannel, Info info) {
        this.textChannel = textChannel;
        this.info = info;
    }

    @Override
    public void run() {
        try {
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
            RequestFuture<Message> request = textChannel.sendMessage(Bot.getAnnonce(info, textChannel.getGuild())).submit();
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
            return getMessage().equals(embed.getTitle());
        }
        return true;
    }

    public static String getMessage() {
        Calendar calendar = EventScheduler.getNextSchedul();
        return String.format(SAY,
                DATE_FORMAT.format(calendar.getTime()),
                TIME_FORMAT.format(calendar.getTime()));
    }
}
