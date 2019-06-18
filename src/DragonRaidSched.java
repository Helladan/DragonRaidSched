import bot.Bot;
import bot.presistance.DataPersist;

public class DragonRaidSched {
    public static void main(String[] args) throws Exception{
        if(args.length > 0) {
        	new DataPersist();
            new Bot(args[0]);
        } else {
            System.out.println("Veuillez indiquer le token du bot");
        }
    }
}
