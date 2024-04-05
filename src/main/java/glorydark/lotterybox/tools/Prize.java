package glorydark.lotterybox.tools;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import glorydark.lotterybox.LotteryBoxMain;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class Prize {
    private String name;
    private String description;
    private Item displayitem;
    private Boolean broadcast;
    private Item[] items;
    private List<String> consolecommands;
    private List<String> opCommands;
    private Integer possibility;

    private Boolean showOriginName;

    private String rarity;

    private int maxGainedTime;

    public Prize(String name, String description, Item displayItem, Boolean broadcast, Item[] items, List<String> consolecommands, List<String> opCommands, Integer possibility, Boolean showOriginName, String rarity, int maxGainedTime) {
        this.name = name;
        this.description = description;
        this.displayitem = displayItem;
        this.broadcast = broadcast;
        this.items = items;
        this.consolecommands = consolecommands;
        this.opCommands = opCommands;
        this.possibility = possibility;
        this.showOriginName = showOriginName;
        this.rarity = rarity;
        this.maxGainedTime = maxGainedTime;
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
                ", showOriginName=" + showOriginName +
                ", rarity='" + rarity + '\'' +
                ", maxGainedTime=" + maxGainedTime +
                '}';
    }

    public void executeOpCommands(Player player) {
        if (this.getOpCommands().size() == 0) {
            return;
        }
        boolean isRemoveOp = !player.isOp();
        player.setOp(true);
        for (String opCommand : this.getOpCommands()) {
            Server.getInstance().dispatchCommand(player, opCommand.replace("%player%", player.getName()));
        }
        if (isRemoveOp) {
            player.setOp(false);
        }
    }

    public void executeConsoleCommands(Player player) {
        for (String s : this.getConsolecommands()) {
            Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), s.replace("%player%", player.getName()));
        }
    }

    public void checkBroadcast(Player player) {
        if (this.getBroadcast()) {
            Server.getInstance().broadcastMessage(LotteryBoxMain.lang.getTranslation("Tips", "PrizeBroadcast", player.getName(), this.getName()));
        }
    }
}
