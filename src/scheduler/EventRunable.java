package scheduler;

import bot.domain.Infos;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RequestFuture;

import java.awt.*;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class EventRunable implements Runnable{
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
    private final static DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.FRANCE);
    private final static String SAY = "Raid du %s à %s";

    private TextChannel textChannel;
    private Infos infos;

    public EventRunable(TextChannel textChannel, Infos infos) {
        this.textChannel = textChannel;
        this.infos = infos;
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
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle(getMessage());
            RequestFuture<Message> request = textChannel.sendMessage(builder.build()).submit();
            do {
                Thread.sleep(1000);
            } while (!request.isDone());
            infos.setAnnonceId(request.get().getId());
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
