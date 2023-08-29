package glorydark.lotterybox.event;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;

public class LotteryForceCloseEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public LotteryForceCloseEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
