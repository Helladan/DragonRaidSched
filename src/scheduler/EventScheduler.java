package scheduler;

import bot.domain.Data;
import bot.domain.Info;
import net.dv8tion.jda.core.entities.TextChannel;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventScheduler {
    // 2 Heures
    private static final Integer END_EVENT = 2 * 60 * 60 * 1000;

    private ScheduledExecutorService executor;


    public EventScheduler(TextChannel textChannel, Data data) {
        Info info = data.getInfos().get(textChannel.getId());
        executor = Executors.newScheduledThreadPool(2);
        Calendar schedul = getNextSchedul();


        // 7 Jours
        int period = 7 * 24 * 60 * 60 * 1000;
        long initialDelay = schedul.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() + END_EVENT;

        executor.scheduleAtFixedRate(new SaveRunable(data), 10, 10, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(new EventRunable(data, textChannel),
                initialDelay,
                period,
                TimeUnit.MILLISECONDS);
    }

    public static Calendar getNextSchedul() {
        Calendar schedul = Calendar.getInstance();
        schedul.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        schedul.set(Calendar.HOUR_OF_DAY, 21);
        schedul.clear(Calendar.MINUTE);
        schedul.clear(Calendar.SECOND);
        schedul.clear(Calendar.MILLISECOND);
        if(Calendar.getInstance().getTimeInMillis() > (schedul.getTimeInMillis() + END_EVENT - 10000)){
            schedul.add(Calendar.DAY_OF_WEEK, 7);
        }
        return schedul;
    }
}
