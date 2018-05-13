package scheduler;

import bot.domain.Data;
import bot.domain.Info;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventScheduler {
	// 7 Jours
	private final static int PERIOD = 7 * 24 * 60 * 60 * 1000;

	private static ScheduledExecutorService executor;

	public static void update(Data data, List<Guild> guilds) {
		if (executor != null) {
			executor.shutdown();
		}
		executor = Executors.newScheduledThreadPool(data.getInfos().size() + 1);
		executor.scheduleAtFixedRate(new SaveRunable(data), 10, 10, TimeUnit.MINUTES);
		for (String textChannelId : new HashSet<>(data.getInfos().keySet())) {
			Info info = data.getInfos().get(textChannelId);
			if (info.getDayOfWeek() != 0) {
				Calendar schedul = getNextSchedul(info);

				long initialDelay = schedul.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()
						+ info.getTime();

				TextChannel textChannel = null;
				for (Guild guild : guilds) {
					textChannel = guild.getTextChannelById(textChannelId);
					if (textChannel != null) {
						break;
					}
				}

				if (textChannel != null) {
					executor.scheduleAtFixedRate(new EventRunable(data, textChannel), initialDelay, PERIOD,
							TimeUnit.MILLISECONDS);
				} else {
					data.getInfos().remove(textChannelId);
				}
			}
		}
	}

	public static Calendar getNextSchedul(Info info) {
		Calendar schedul = Calendar.getInstance();
		schedul.set(Calendar.DAY_OF_WEEK, info.getDayOfWeek());
		schedul.set(Calendar.HOUR_OF_DAY, info.getHour());
		schedul.set(Calendar.MINUTE, info.getMinute());
		schedul.clear(Calendar.SECOND);
		schedul.clear(Calendar.MILLISECOND);
		if (Calendar.getInstance().getTimeInMillis() > (schedul.getTimeInMillis() + info.getTime() - 10000)) {
			schedul.add(Calendar.DAY_OF_WEEK, 7);
		}
		return schedul;
	}
}
