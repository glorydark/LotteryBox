package glorydark.lotterybox.api;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import glorydark.lotterybox.LotteryBoxMain;

import java.util.ArrayList;
import java.util.List;

public class LotteryBoxAPI {

    public static boolean checkTicketCounts(String player, String ticket, Integer counts) {
        return new Config(LotteryBoxMain.path + "/tickets/" + player + ".yml", Config.YAML).getInt(ticket, 0) >= counts;
    }

    public static int getTicketCounts(String player, String ticket) {
        return new Config(LotteryBoxMain.path + "/tickets/" + player + ".yml", Config.YAML).getInt(ticket, 0);
    }

    public static void setTicketCounts(String player, String ticket, Integer amount) {
        Config config = new Config(LotteryBoxMain.path + "/tickets/" + player + ".yml", Config.YAML);
        config.set(ticket, amount);
        config.save();
    }

    public static void changeTicketCounts(String player, String ticket, Integer delta) {
        setTicketCounts(player, ticket, getTicketCounts(player, ticket) + delta);
    }

    public static Integer getLotteryPlayTimes(String player, String lotteryName) {
        return new Config(LotteryBoxMain.path + "/lottery_records/" + player + ".yml", Config.YAML).getInt(lotteryName, 0);
    }

    public static void setLotteryPlayTimes(String player, String lotteryName, Integer amount) {
        Config config = new Config(LotteryBoxMain.path + "/lottery_records/" + player + ".yml", Config.YAML);
        config.set(lotteryName, amount);
        config.save();
    }

    public static int getLotteryPrizeTimes(String player, String lotteryName, String prize) {
        return new Config(LotteryBoxMain.path + "/prize_records/" + player + ".yml", Config.YAML).getSection(lotteryName).getInt(prize);
    }

    public static void changeLotteryPrizeTimes(String player, String lotteryName, String prize) {
        Config config = new Config(LotteryBoxMain.path + "/prize_records/" + player + ".yml", Config.YAML);
        if (!config.exists(lotteryName)) {
            config.set(lotteryName, new ConfigSection());
        }
        config.getSection(lotteryName).set(prize, config.getSection(lotteryName).getInt(prize) + 1);
        config.save();
    }

    public static void changeLotteryPlayTimes(String player, String lotteryName, Integer delta) {
        setLotteryPlayTimes(player, lotteryName, getLotteryPlayTimes(player, lotteryName) + delta);
    }

    public static boolean checkItemsExists(Player player, Item[] needItems) {
        for (Item needItem : needItems) {
            int counts = 0;
            for (Item hasItem : player.getInventory().getContents().values()) {
                if (hasItem.equals(needItem, false)) {
                    counts += hasItem.getCount();
                }
            }
            if (needItem.getCount() > counts) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkItemExists(Player player, Item needItem, Integer spins) {
        int counts = 0;
        for (Item hasItem : player.getInventory().getContents().values()) {
            if (hasItem.equals(needItem, false)) {
                counts += hasItem.getCount();
            }
        }
        return needItem.getCount() <= counts * spins;
    }

    public static boolean isPE(Player player) {
        List<Integer> pc = new ArrayList<>();
        pc.add(7);
        pc.add(11);
        pc.add(13);
        pc.add(20);
        return !pc.contains(player.getLoginChainData().getDeviceOS());
    }

}
