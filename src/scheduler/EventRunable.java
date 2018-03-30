package scheduler;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RequestFuture;
import net.dv8tion.jda.core.requests.RestAction;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EventRunable implements Runnable{
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
    private final static DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.FRANCE);
    private final static String SAY = "Raid du %s à %s";

    private TextChannel textChannel;
    private Calendar calendar;
    private Map<TextChannel, Message> annonces;

    public EventRunable(TextChannel textChannel, Map<TextChannel, Message> annonces) {
        this.textChannel = textChannel;
        this.calendar = EventScheduler.getNextSchedul();
        this.annonces = annonces;
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
                if(!message.getAuthor().equals(me) || message.getContentDisplay().startsWith(String.format(SAY,
                        DATE_FORMAT.format(calendar.getTime()),
                        TIME_FORMAT.format(calendar.getTime())))) {
                    textChannel.deleteMessageById(message.getId()).submit();
                }
            }
            RequestFuture<Message> request = textChannel.sendMessage(
                    String.format(SAY,
                            DATE_FORMAT.format(calendar.getTime()),
                            TIME_FORMAT.format(calendar.getTime()))
            ).submit();
            do {
                Thread.sleep(1000);
            } while (!request.isDone());
            annonces.put(textChannel, request.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.err);
        }
    }
}
