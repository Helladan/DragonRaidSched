package scheduler;

import bot.service.AnnonceGenerator;
import bot.domain.Data;
import bot.domain.Info;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

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
            Info info = new Info();
            data.getInfos().put(textChannel.getId(), info);
            info.setAnnonceId(textChannel.sendMessage(AnnonceGenerator.getAnnonce(data, textChannel)).submit().get().getId());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.err);
        }
    }
}
