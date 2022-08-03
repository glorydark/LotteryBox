package glorydark.lotterybox.forms;

import cn.nukkit.Player;
import cn.nukkit.entity.item.EntityMinecartChest;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;

public class LotteryMenu extends ContainerInventory {
    private final long id;

    public LotteryMenu(EntityMinecartChest chest, long id) {
        super(chest.getInventory().getHolder(), InventoryType.CHEST);
        this.id = id;
    }

    @Override
    public void onOpen(Player player) {
        ContainerOpenPacket pk = new ContainerOpenPacket();
        pk.windowId = player.getWindowId(this);
        pk.entityId = this.id;
        player.dataPacket(pk);
    }

    @Override
    public void onClose(Player player) {
        super.onClose(player);

        RemoveEntityPacket pk = new RemoveEntityPacket();
        pk.eid = this.id;
        player.dataPacket(pk);
    }
}