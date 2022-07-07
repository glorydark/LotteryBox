package glorydark.lotterybox.tools;

import cn.nukkit.item.Item;

import java.util.Arrays;
import java.util.List;

public class Prize {
    private String name;
    private String description;
    private Item displayitem;
    private Boolean broadcast;
    private Item[] items;
    private List<String> consolecommands;
    private Integer possibility;

    private Boolean showOriginName;

    public Prize(String name, String description, Item displayItem, Boolean broadcast, Item[] items, List<String> consolecommands, Integer possibility, Boolean showOriginName){
        this.name = name;
        this.description = description;
        this.displayitem = displayItem;
        this.broadcast = broadcast;
        this.items = items;
        this.consolecommands = consolecommands;
        this.possibility = possibility;
        this.showOriginName = showOriginName;
    }

    public Boolean isShowOriginName() {
        return showOriginName;
    }

    public String getName() {
        return name;
    }

    public Boolean isBroadcast() {
        return broadcast;
    }

    public Integer getPossibility() {
        return possibility;
    }

    public List<String> getConsolecommands() {
        return consolecommands;
    }

    public String getDescription() {
        return description;
    }

    public Item[] getItems() {
        return items;
    }

    public Item getDisplayitem() {
        return displayitem;
    }

    @Override
    public String toString() {
        return "Prize{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", displayitem=" + displayitem +
                ", broadcast=" + broadcast +
                ", items=" + Arrays.toString(items) +
                ", consolecommands=" + consolecommands +
                ", possibility=" + possibility +
                '}';
    }
}
