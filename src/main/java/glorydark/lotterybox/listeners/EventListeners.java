package glorydark.lotterybox.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityMinecartChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.InventoryMoveItemEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.inventory.CraftingGrid;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.inventory.PlayerUIInventory;
import cn.nukkit.network.protocol.EntityFallPacket;
import cn.nukkit.network.protocol.SetEntityMotionPacket;
import cn.nukkit.utils.Config;
import glorydark.lotterybox.MainClass;
import glorydark.lotterybox.event.LotteryForceCloseEvent;
import glorydark.lotterybox.tasks.LotteryBoxChangeTask;
import glorydark.lotterybox.tools.Inventory;
import glorydark.lotterybox.tools.LotteryBox;

import java.util.ArrayList;


public class EventListeners implements Listener {
    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event){
        if(event.getEntity() instanceof EntityMinecartChest && event.getEntity().namedTag.contains("IsLotteryBox")){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerInteractEntityEvent(PlayerInteractEntityEvent event){
        if(event.getEntity() instanceof EntityMinecartChest && event.getEntity().namedTag.contains("IsLotteryBox")){
            event.setCancelled(true);
        }
    }


    public static void resetBasisWindow(Player player){
        PlayerUIInventory inventory = player.getUIInventory();
        player.addWindow(inventory);
        CraftingGrid grid = player.getCraftingGrid();
        player.addWindow(grid);
        PlayerOffhandInventory inventory1 = player.getOffhandInventory();
        player.addWindow(inventory1);
    }

    @EventHandler
    public void slotChange(InventoryMoveItemEvent event){
        Player[] players = event.getViewers();
        for(Player player: players) {
            if (MainClass.chestList.containsKey(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event){
        if(MainClass.chestList.containsKey(event.getPlayer())){
            EntityMinecartChest chest = MainClass.chestList.get(event.getPlayer());
            chest.getInventory().clearAll();
            chest.close();
            MainClass.chestList.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void LotteryForceCloseEvent(LotteryForceCloseEvent event){
        if(MainClass.chestList.containsKey(event.getPlayer())){
            EntityMinecartChest chest = MainClass.chestList.get(event.getPlayer());
            chest.getInventory().clearAll();
            chest.close();
            MainClass.chestList.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void moveItem(InventoryClickEvent event){
        Player player = event.getPlayer();
        if (MainClass.chestList.containsKey(player)) {
            if(event.getSourceItem().getCustomName().equals(MainClass.lang.getTranslation("PlayLotteryWindow","StartLotteryWithOneSpinsItemName")) && !MainClass.playingPlayers.contains(player)){
                startLottery(player, false);
            }
            if(event.getSourceItem().getCustomName().equals(MainClass.lang.getTranslation("PlayLotteryWindow","StartLotteryWithTenSpinsItemName")) && !MainClass.playingPlayers.contains(player)){
                startLottery(player, true);
            }
            event.setCancelled(true);
        }
    }

    public void startLottery(Player player, Boolean isTenSpins){
        LotteryBox lotteryBox = MainClass.playerLotteryBoxes.get(player);
        int spin = 1;
        if(isTenSpins){
            spin = 10;
        }
        if(lotteryBox.deductNeeds(player, spin)) {
            if(lotteryBox.checkLimit(player.getName(), spin)) {
                Server.getInstance().getScheduler().scheduleRepeatingTask(new LotteryBoxChangeTask(MainClass.chestList.get(player), player, lotteryBox, spin), 4);
            }else{
                player.sendMessage(MainClass.lang.getTranslation("Tips","TimesLimit"));
                Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
            }
        }else{
            player.sendMessage(MainClass.lang.getTranslation("Tips","LackOfItemsOrTickets"));
            Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
        }
    }

    @EventHandler
    public void Join(PlayerLocallyInitializedEvent event){
        Player player = event.getPlayer();
        Config config = new Config(MainClass.path+"/cache.yml", Config.YAML);
        if(config.exists(player.getName())){
            for(String string: new ArrayList<>(config.getStringList(player.getName()))){
                player.getInventory().addItem(Inventory.getItem(string));
            }
            config.remove(event.getPlayer().getName());
            event.getPlayer().sendMessage("检测到您上次退出有异常，将未发放的物品发放给您！");
            MainClass.instance.getLogger().alert("Detect [" + player.getName() + "] quit the server unexpectedly, trying to deal with it.");
            config.save();
        }
    }

    @EventHandler
    public void DataPacketSendEvent(DataPacketSendEvent event){
        if(!MainClass.chestList.containsKey(event.getPlayer())){return;}
        if(event.getPacket() instanceof SetEntityMotionPacket){
            SetEntityMotionPacket pk = (SetEntityMotionPacket) event.getPacket();
            if(MainClass.chestList.get(event.getPlayer()).getId() == pk.eid){
                MainClass.chestList.get(event.getPlayer()).teleport(event.getPlayer().getPosition(), null);
                event.setCancelled(true);
            }
        }
    }
}
