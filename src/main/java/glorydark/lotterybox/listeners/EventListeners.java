package glorydark.lotterybox.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.InventoryMoveItemEvent;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.inventory.CraftingGrid;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.inventory.PlayerUIInventory;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import glorydark.lotterybox.MainClass;
import glorydark.lotterybox.tasks.LotteryBoxChangeTask;
import glorydark.lotterybox.tools.Inventory;
import glorydark.lotterybox.tools.LotteryBox;

import java.util.ArrayList;


public class EventListeners implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void close(InventoryCloseEvent event){
        Player p = event.getPlayer();
        if(MainClass.chestList.containsKey(p)) {
            removeChest(p, false);
        }
    }

    public static void removeChest(Player p, Boolean isReset){
        Block origin = MainClass.chestList.get(p);
        if(origin == null){ return; }
        Location chestLocation = origin.getLocation();
        p.getLevel().setBlock(chestLocation, origin.getBlock());
        if(p.isOnline()) {
            p.getLevel().sendBlocks(new Player[]{p}, new Vector3[]{origin});
        }
        MainClass.chestList.remove(p);
        MainClass.playingPlayers.remove(p);
        if(isReset){
            if(p.isOnline()) {
                resetBasisWindow(p);
            }
        }
        if(origin.getLevel().getBlockEntity(chestLocation) == null){ return; }
        origin.getLevel().getBlockEntity(chestLocation).close();
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
    public void moveItem(InventoryClickEvent event){
        Player player = event.getPlayer();
        if (MainClass.chestList.containsKey(player)) {
            if(event.getSourceItem().getCustomName().equals(MainClass.lang.getTranslation("PlayLotteryWindow","StartLotteryItemName")) && !MainClass.playingPlayers.contains(player)){
                LotteryBox lotteryBox = MainClass.playerLotteryBoxes.get(player);
                if(lotteryBox.deductNeeds(player)) {
                    if(lotteryBox.checkLimit(player.getName())) {
                        Server.getInstance().getScheduler().scheduleRepeatingTask(new LotteryBoxChangeTask(MainClass.chestList.get(player), player, lotteryBox), 5);
                        MainClass.playingPlayers.add(player);
                    }else{
                        player.sendMessage(MainClass.lang.getTranslation("Tips","TimesLimit"));
                    }
                }else{
                    Block block = MainClass.chestList.get(player);
                    BlockEntityChest chest = (BlockEntityChest) block.getLevel().getBlockEntity(block.getLocation());
                    if(chest != null) {
                        player.removeWindow(chest.getInventory());
                    }
                    removeChest(player, true);
                    player.sendMessage(MainClass.lang.getTranslation("Tips","LackOfItemsOrTickets"));
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        if(MainClass.chestList.containsKey(p)) {
            removeChest(p, true);
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
}
