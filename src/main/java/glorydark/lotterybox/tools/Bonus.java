package glorydark.lotterybox.tools;

import cn.nukkit.item.Item;

import java.util.List;

public class Bonus {
    private String name;
    private Item[] items;
    private List<String> consolecommands;
    private Integer needTimes;

    public Bonus(String prizeName, Item[] items, List<String> consolecommands, Integer possibility){
        this.name = prizeName;
        this.items = items;
        this.consolecommands = consolecommands;
        this.needTimes = possibility;
    }

    public String getName() {
        return name;
    }

    public Integer getNeedTimes() {
        return needTimes;
    }

    public List<String> getConsolecommands() {
        return consolecommands;
    }

    public Item[] getItems() {
        return items;
    }
}
