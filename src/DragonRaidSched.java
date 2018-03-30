import bot.Bot;

public class DragonRaidSched {
    public static void main(String[] args) throws Exception{
        if(args.length > 0) {
            Bot bot = new Bot(args[0]);
        } else {
            System.out.println("Vieulliez indiquer le token du bot");
        }
    }
}
