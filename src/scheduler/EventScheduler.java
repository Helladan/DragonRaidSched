package scheduler;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventScheduler {
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);

    private ScheduledExecutorService executor;


    public EventScheduler(TextChannel textChannel, Map<TextChannel, Message> annonces, Map<TextChannel, List<String>> isPresent, Map<String, String> playerMap) {
        executor = Executors.newScheduledThreadPool(3);
        Calendar schedul = getNextSchedul();


        // 7 Jours
        int period = 7 * 24 * 60 * 60 * 1000;
        // 2 Heures
        int endEvent = 2 * 60 * 60 * 1000;
        long initialDelay = schedul.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() + endEvent;

        new EventRunable(textChannel, annonces).run();

        executor.scheduleAtFixedRate(new SaveRunable(isPresent, playerMap), 10, 10, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(new CleanerRunable(isPresent),
                initialDelay - 10000,
                period - 10000,
                TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(new EventRunable(textChannel, annonces),
                initialDelay,
                period,
                TimeUnit.MILLISECONDS);
    }

    static  public Calendar getNextSchedul() {
        Calendar schedul = Calendar.getInstance();
        schedul.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        schedul.set(Calendar.HOUR_OF_DAY, 21);
        schedul.clear(Calendar.MINUTE);
        schedul.clear(Calendar.SECOND);
        schedul.clear(Calendar.MILLISECOND);
        if(schedul.before(Calendar.getInstance())){
            schedul.add(Calendar.DAY_OF_WEEK, 7);
        }
        return schedul;
    }
}
