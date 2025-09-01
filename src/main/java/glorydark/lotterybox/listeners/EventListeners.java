package glorydark.lotterybox.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityMinecartChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.inventory.*;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.network.protocol.SetEntityMotionPacket;
import cn.nukkit.utils.Config;
import glorydark.lotterybox.LotteryBoxMain;
import glorydark.lotterybox.event.LotteryForceCloseEvent;
import glorydark.lotterybox.forms.FormFactory;
import glorydark.lotterybox.tasks.nonWeight.LotteryBoxChangeTask;
import glorydark.lotterybox.tools.Inventory;
import glorydark.lotterybox.tools.LotteryBox;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventListeners implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (LotteryBoxMain.playingPlayers.contains(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof EntityMinecartChest && event.getEntity().namedTag.contains("IsLotteryBox")) {
            event.setCancelled(true);
            return;
        }
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (LotteryBoxMain.playingPlayers.contains(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void PlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (event.getEntity() instanceof EntityMinecartChest && event.getEntity().namedTag.contains("IsLotteryBox")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSlotChange(InventoryMoveItemEvent event) {
        if (event.getInventory() == null) {
            return;
        }
        Player[] players = event.getViewers();
        for (Player player : players) {
            if (LotteryBoxMain.chestList.containsKey(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (event.getInventory() == null) {
            return;
        }
        Player p = event.getPlayer();
        if (LotteryBoxMain.chestList.containsKey(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerDropItemEvent(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        if (LotteryBoxMain.playingPlayers.contains(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (LotteryBoxMain.playingPlayers.contains(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryTransact(InventoryTransactionEvent event) {
        Player p = event.getTransaction().getSource();
        if (LotteryBoxMain.playingPlayers.contains(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void InventoryMoveItemEvent(InventoryMoveItemEvent event) {
        if (event.getInventory() == null) {
            return;
        }
        Player[] players = event.getViewers();
        for (Player player : players) {
            if (LotteryBoxMain.chestList.containsKey(player)) {
                event.setCancelled(true);
                break;
            } else if (LotteryBoxMain.playingPlayers.contains(player)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getInventory() == null) {
            return;
        }
        if (LotteryBoxMain.chestList.containsKey(event.getPlayer())) {
            EntityMinecartChest chest = LotteryBoxMain.chestList.get(event.getPlayer());
            chest.getInventory().clearAll();
            chest.close();
            LotteryBoxMain.chestList.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void EntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (LotteryBoxMain.playingPlayers.contains((Player) event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void LotteryForceCloseEvent(LotteryForceCloseEvent event) {
        if (LotteryBoxMain.chestList.containsKey(event.getPlayer())) {
            EntityMinecartChest chest = LotteryBoxMain.chestList.get(event.getPlayer());
            chest.getInventory().clearAll();
            chest.close();
            LotteryBoxMain.chestList.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void InventoryPickupItemEvent(InventoryPickupItemEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        for (Player player : holder.getInventory().getViewers()) {
            if (LotteryBoxMain.chestList.containsKey(player)) {
                EntityMinecartChest chest = LotteryBoxMain.chestList.get(player);
                chest.getInventory().clearAll();
                chest.close();
                LotteryBoxMain.chestList.remove(player);
            }
        }
    }

    @EventHandler
    public void moveItem(InventoryClickEvent event) {
        if (event.getInventory() == null) {
            return;
        }
        Player player = event.getPlayer();
        if (LotteryBoxMain.chestList.containsKey(player)) {
            if (event.getSourceItem().getCustomName().equals(LotteryBoxMain.lang.getTranslation("PlayLotteryWindow", "StartLotteryWithOneSpinsItemName")) && !LotteryBoxMain.playingPlayers.contains(player)) {
                startLottery(player, false);
            }
            if (event.getSourceItem().getCustomName().equals(LotteryBoxMain.lang.getTranslation("PlayLotteryWindow", "StartLotteryWithTenSpinsItemName")) && !LotteryBoxMain.playingPlayers.contains(player)) {
                startLottery(player, true);
            }
            event.setCancelled(true);
        }
    }

    public void startLottery(Player player, Boolean isTenSpins) {
        LotteryBox lotteryBox = LotteryBoxMain.playerLotteryBoxes.get(player);
        int spin = 1;
        if (isTenSpins) {
            spin = 10;
        }
        if (lotteryBox.checkLimit(player.getName(), spin)) {
            if (lotteryBox.deductNeeds(player, spin)) {
                Server.getInstance().getScheduler().scheduleRepeatingTask(LotteryBoxMain.instance, new LotteryBoxChangeTask(LotteryBoxMain.chestList.get(player), player, lotteryBox, spin), LotteryBoxMain.chest_speed_ticks);
            } else {
                Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
            }
        } else {
            player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "TimesLimit"));
            Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
        }
    }

    @EventHandler
    public void onLevelChangeEvent(EntityLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (LotteryBoxMain.playingPlayers.contains(p)) {
                for (String path : LotteryBoxMain.inventory_cache_paths) {
                    File file = new File((Server.getInstance().getFilePath() + "/" + path + "/").replace("%player%", p.getName()));
                    if (file.exists()) {
                        Config config = new Config(file);
                        config.set("inventoryContents", new HashMap<>());
                        config.save();
                    }
                }
                LotteryBoxMain.playingPlayers.remove(p);
                LotteryBoxMain.playerLotteryBoxes.remove(p);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerLocallyInitializedEvent event) {
        Player player = event.getPlayer();
        Config config = new Config(LotteryBoxMain.path + "/cache.yml", Config.YAML);
        if (config.exists(player.getName())) {
            if (LotteryBoxMain.save_bag_enabled) {
                player.getInventory().clearAll();
                for (Map<String, Object> string : new ArrayList<Map<String, Object>>(config.get(player.getName() + ".inventory_cache", new ArrayList<>()))) {
                    player.getInventory().setItem((Integer) string.getOrDefault("slot", 0), Inventory.getItem((String) string.getOrDefault("item", "")));
                }
                for (Map<String, Object> string : new ArrayList<Map<String, Object>>(config.get(player.getName() + ".offhand_inventory_cache", new ArrayList<>()))) {
                    player.getOffhandInventory().setItem((Integer) string.getOrDefault("slot", 0), Inventory.getItem((String) string.getOrDefault("item", "")));
                }
            }
            for (String string : new ArrayList<>(config.getStringList(player.getName() + ".items"))) {
                player.getInventory().addItem(Inventory.getItem(string));
            }
            for (String string : new ArrayList<>(config.getStringList(player.getName() + ".console_commands"))) {
                Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), string.replace("%player%", player.getName()));
            }
            List<String> opCommands = new ArrayList<>(config.getStringList(player.getName() + ".op_commands"));
            if (!opCommands.isEmpty()) {
                boolean isRemoveOp = !player.isOp();
                player.setOp(true);
                for (String string : opCommands) {
                    Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), string.replace("%player%", player.getName()));
                }
                if (isRemoveOp) {
                    player.setOp(false);
                }
            }
            for (String string : new ArrayList<>(config.getStringList(player.getName() + ".messages"))) {
                player.sendMessage(string.replace("%player%", player.getName()));
            }
            config.remove(event.getPlayer().getName());
            event.getPlayer().sendMessage("检测到您上次退出有异常，将未发放的物品发放给您！");
            LotteryBoxMain.instance.getLogger().alert("Detect [" + player.getName() + "] quit the server unexpectedly, trying to deal with it.");
            config.save();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (LotteryBoxMain.playingPlayers.contains(player)) {
            event.getPlayer().getInventory().clearAll();
            LotteryBoxMain.playingPlayers.remove(player);
        }
        FormFactory.exchangeCaches.remove(player);
    }

    @EventHandler
    public void DataPacketSendEvent(DataPacketSendEvent event) {
        if (!LotteryBoxMain.chestList.containsKey(event.getPlayer())) {
            return;
        }
        if (event.getPacket() instanceof SetEntityMotionPacket) {
            SetEntityMotionPacket pk = (SetEntityMotionPacket) event.getPacket();
            if (LotteryBoxMain.chestList.get(event.getPlayer()).getId() == pk.eid) {
                LotteryBoxMain.chestList.get(event.getPlayer()).teleport(event.getPlayer().getPosition(), null);
                event.setCancelled(true);
            }
        }
    }
}
