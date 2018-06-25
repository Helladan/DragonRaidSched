package scheduler;

import bot.domain.Data;
import bot.domain.Info;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EventScheduler {
	// 7 Jours
	private final static int PERIOD = 7 * 24 * 60 * 60 * 1000;

	private static ScheduledExecutorService executor;

	public static void update(Data data, List<Guild> guilds) {
		if (executor != null) {
			executor.shutdown();
		}
		Supplier<Stream<Entry<String, Info>>> infoSupplier = () -> data.getInfos().entrySet().stream().filter(entry -> entry.getValue().getTime()>0);
		executor = Executors.newScheduledThreadPool((int) (infoSupplier.get().count() + 1));
		executor.scheduleAtFixedRate(new SaveRunable(data), 10, 10, TimeUnit.MINUTES);
		infoSupplier.get().forEach(entry -> {
			String textChannelId = entry.getKey();
			Info info = entry.getValue();
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
		});
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
