package scheduler;

import bot.service.AnnonceGenerator;
import bot.domain.Data;
import bot.domain.Info;
import net.dv8tion.jda.core.entities.TextChannel;

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
        	Info oldInfo =  data.getInfos().get(textChannel.getId());
            Info info = new Info();
            info.setDayOfWeek(oldInfo.getDayOfWeek());
            info.setHour(oldInfo.getHour());
            info.setMinute(oldInfo.getMinute());
            info.setTime(oldInfo.getTime());
            data.getInfos().put(textChannel.getId(), info);
            info.setAnnonceId(textChannel.sendMessage(AnnonceGenerator.getAnnonce(data, textChannel)).submit().get().getId());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.err);
        }
    }
}
