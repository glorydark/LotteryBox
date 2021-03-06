package glorydark.lotterybox.tools;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.LevelException;
import glorydark.lotterybox.MainClass;

public class BasicTool {

    public static Boolean checkTicketCounts(String player, String ticket, Integer counts){
        return new Config(MainClass.path+"/tickets/"+player+".yml", Config.YAML).getInt(ticket, 0) >= counts;
    }

    public static Integer getTicketCounts(String player, String ticket){
        return new Config(MainClass.path+"/tickets/"+player+".yml", Config.YAML).getInt(ticket, 0);
    }

    public static void setTicketCounts(String player, String ticket, Integer amount){
        Config config = new Config(MainClass.path+"/tickets/"+player+".yml", Config.YAML);
        config.set(ticket, amount);
        config.save();
    }

    public static void changeTicketCounts(String player, String ticket, Integer delta){
        setTicketCounts(player, ticket, getTicketCounts(player, ticket) + delta);
    }

    public static Integer getLotteryPlayTimes(String player, String lotteryName){
        return new Config(MainClass.path+"/lotteryrecords/"+player+".yml", Config.YAML).getInt(lotteryName, 0);
    }

    public static void setLotteryPlayTimes(String player, String lotteryName, Integer amount){
        Config config = new Config(MainClass.path+"/lotteryrecords/"+player+".yml", Config.YAML);
        config.set(lotteryName, amount);
        config.save();
    }

    public static void changeLotteryPlayTimes(String player, String lotteryName, Integer delta){
        setLotteryPlayTimes(player, lotteryName, getTicketCounts(player, lotteryName) + delta);
    }

    public static Boolean checkItemsExists(Player player, Item[] needItems){
        for(Item needItem: needItems){
            Integer counts = 0;
            for(Item hasItem: player.getInventory().getContents().values()){
                if(hasItem.equals(needItem, false)){
                    counts+=hasItem.getCount();
                }
            }
            if(needItem.getCount() > counts){
                return false;
            }
        }
        return true;
    }

    public static Boolean checkItemExists(Player player, Item needItem, Integer spins){
        Integer counts = 0;
        for(Item hasItem: player.getInventory().getContents().values()){
            if(hasItem.equals(needItem, false)){
                counts+=hasItem.getCount();
            }
        }
        if(needItem.getCount() > counts * spins){
            return false;
        }
        return true;
    }
}
