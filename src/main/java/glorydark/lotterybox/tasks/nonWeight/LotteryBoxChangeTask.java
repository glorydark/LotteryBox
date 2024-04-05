package glorydark.lotterybox.tasks.nonWeight;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockAir;
import cn.nukkit.entity.item.EntityMinecartChest;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.item.enchantment.protection.EnchantmentProtectionAll;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DyeColor;
import glorydark.lotterybox.LotteryBoxMain;
import glorydark.lotterybox.api.CreateFireworkApi;
import glorydark.lotterybox.api.LotteryBoxAPI;
import glorydark.lotterybox.event.LotteryForceCloseEvent;
import glorydark.lotterybox.tools.Bonus;
import glorydark.lotterybox.tools.Inventory;
import glorydark.lotterybox.tools.LotteryBox;
import glorydark.lotterybox.tools.Prize;

import java.util.*;

public class LotteryBoxChangeTask extends Task implements Runnable {
    private final EntityMinecartChest chest;
    private final List<Integer> prizeIndexList = new ArrayList<>();
    private final Player player;
    private final LotteryBox lotteryBox;
    private final List<Integer> allowIndex;
    private final int maxSpin;
    private Map<Integer, Item> inventory;
    private Integer index = 0;
    private int ticks;
    private int spin = 1;

    public LotteryBoxChangeTask(EntityMinecartChest chest, Player player, LotteryBox box, Integer spins) {
        this.chest = chest;
        this.inventory = this.chest.getInventory().getContents();
        this.player = player;
        this.lotteryBox = box;
        Integer[] arr = new Integer[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26};
        allowIndex = Arrays.asList(arr);
        for (int i = 0; i < spins; i++) {
            int prizeIndex = getPrizeIndexList();
            if (prizeIndex != -1) {
                if (spins == 1) {
                    Prize prize = lotteryBox.getPrizes().get(allowIndex.get(prizeIndex % 22));
                    if (prize != null) {
                        LotteryBoxAPI.changeLotteryPrizeTimes(player.getName(), lotteryBox.getName(), prize.getName());
                    }
                    this.prizeIndexList.add(prizeIndex);
                } else {
                    this.prizeIndexList.add(22 + prizeIndex);
                }
            } else {
                this.prizeIndexList.add(-1);
            }
            LotteryBoxAPI.changeLotteryPlayTimes(player.getName(), lotteryBox.getName(), 1);
            if (lotteryBox.getBonus(LotteryBoxAPI.getLotteryPlayTimes(player.getName(), lotteryBox.getName())) != null) {
                Bonus bonus = lotteryBox.getBonus(LotteryBoxAPI.getLotteryPlayTimes(player.getName(), lotteryBox.getName()));
                for (String s : bonus.getConsolecommands()) {
                    Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), s.replace("%player%", player.getName()));
                }
                player.getInventory().addItem(bonus.getItems());
                Server.getInstance().broadcastMessage(LotteryBoxMain.lang.getTranslation("Tips", "BonusBroadcast", player.getName(), lotteryBox.getName(), bonus.getNeedTimes(), bonus.getName()));
                LotteryBoxMain.log.info("玩家 {" + player.getName() + "} 在抽奖箱 {" + lotteryBox.getName() + "} 中抽奖达到 {" + bonus.getNeedTimes() + "} 次，获得物品 {" + bonus.getName() + "}!");
            }
        }
        this.maxSpin = spins;
    }

    public Integer getPrizeIndexList() {
        Random random = new Random();
        List<Prize> remnantPrizes = new ArrayList<>(lotteryBox.getPrizes());
        remnantPrizes.removeIf(prize -> prize.getMaxGainedTime() != -1 &&
                LotteryBoxAPI.getLotteryPrizeTimes(player.getName(), lotteryBox.getName(), prize.getName()) >= prize.getMaxGainedTime());
        for (Prize prize : remnantPrizes) {
            List<Integer> integers = new ArrayList<>();
            for (int i = 0; i < prize.getPossibility(); i++) {
                integers.add(Math.abs(random.nextInt()) % 10000);
            }
            if (integers.contains(Math.abs(random.nextInt()) % 10000)) {
                return lotteryBox.getPrizes().indexOf(prize);
            }
        }
        List<Integer> absent = new ArrayList<>();
        for (int i = lotteryBox.getPrizes().size(); i < 22; i++) {
            absent.add(allowIndex.get(i));
        }
        if (absent.size() > 0) {
            return absent.get(random.nextInt(absent.size()));
        } else {
            return -1;
        }
    }

    @Override
    public void onRun(int i) {
        ticks += 1;
        if (player.isOnline() && !chest.closed && chest.getInventory().getViewers().contains(player) && !LotteryBoxMain.banWorlds.contains(player.getLevel().getName()) && LotteryBoxMain.isWorldAvailable(player.getLevel().getName())) {
            Integer thisMaxIndex = prizeIndexList.get(spin - 1);
            if (index <= thisMaxIndex) {
                if (thisMaxIndex > 10) {
                    if (index < 2 || index + 2 >= thisMaxIndex) {
                        if (ticks % 4 != 0) {
                            return;
                        }
                    } else {
                        if (index < 5 || index + 5 >= thisMaxIndex) {
                            if (ticks % 2 != 0) {
                                return;
                            }
                        }
                    }
                }
                Integer realIndex = allowIndex.get(index % 22);
                chest.getInventory().setContents(inventory);
                Item item = chest.getInventory().getItem(realIndex);
                item.addEnchantment(new EnchantmentProtectionAll());
                chest.getInventory().setItem(realIndex, item);
                player.getLevel().addSound(player.getPosition(), lotteryBox.getSound());
                if (index % 22 < lotteryBox.getPrizes().size()) {
                    lotteryBox.addBlockParticle(player, lotteryBox.getPrizes().get(index % 22));
                }
                index++;
            } else {
                if (thisMaxIndex == -1) {
                    player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "DrawEndWithoutPrize"));
                    if (spin < maxSpin - 1) {
                        chest.getInventory().setContents(inventory);
                        inventory = chest.getInventory().getContents();
                        index = 0;
                        spin++;
                    } else {
                        LotteryBoxMain.playerLotteryBoxes.remove(player);
                        LotteryBoxMain.playingPlayers.remove(player);
                        player.removeWindow(chest.getInventory());
                        Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
                        this.cancel();
                    }
                } else {
                    Item item = inventory.get(allowIndex.get(thisMaxIndex % 22));
                    Item[] give;
                    List<Prize> prizes = lotteryBox.getPrizes();
                    Integer realIndex = allowIndex.get(thisMaxIndex % 22);
                    Prize prize = null;
                    if (realIndex < prizes.size()) {
                        give = prizes.get(realIndex).getItems();
                        prize = prizes.get(realIndex);
                    } else {
                        give = new Item[]{new BlockAir().toItem()};
                    }
                    item.addEnchantment(new EnchantmentProtectionAll());
                    chest.getInventory().setItem(realIndex, item);
                    lotteryBox.showEndParticle(player);
                    if (lotteryBox.getSpawnFirework()) {
                        CreateFireworkApi.spawnFirework(player.getPosition(), DyeColor.YELLOW, ItemFirework.FireworkExplosion.ExplosionType.BURST);
                    }
                    if (!item.getCustomName().equals(LotteryBoxMain.lang.getTranslation("PlayLotteryWindow", "BlockAir")) && prize != null) {
                        player.getInventory().addItem(give);
                        player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "DrawEndWithPrize").replace("%s", Objects.requireNonNull(prize).getName()));
                        prize.executeOpCommands(player);
                        prize.executeConsoleCommands(player);
                        prize.checkBroadcast(player);
                        LotteryBoxMain.log.info("玩家 {" + player.getName() + "} 在抽奖箱 {" + lotteryBox.getName() + "} 中抽到物品 {" + prize.getName() + "}!");
                    } else {
                        player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "DrawEndWithoutPrize"));
                    }
                    if (spin < maxSpin - 1) {
                        chest.getInventory().setContents(inventory);
                        Item enchant = chest.getInventory().getItem(realIndex);
                        enchant.addEnchantment(new EnchantmentProtectionAll());
                        chest.getInventory().setItem(realIndex, enchant);
                        inventory = chest.getInventory().getContents();
                        index = 0;
                        spin++;
                    } else {
                        LotteryBoxMain.playerLotteryBoxes.remove(player);
                        LotteryBoxMain.playingPlayers.remove(player);
                        player.removeWindow(chest.getInventory());
                        Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
                        this.cancel();
                    }
                }
            }
        } else {
            for (int t = 1; t < spin; t++) {
                prizeIndexList.remove(0);
            }
            for (Integer thisMaxIndex : prizeIndexList) {
                if (thisMaxIndex == -1) {
                    continue;
                }
                List<Prize> prizes = lotteryBox.getPrizes();
                Integer realIndex = allowIndex.get(thisMaxIndex % 22);
                if (realIndex < prizes.size()) {
                    Prize prize = prizes.get(realIndex);
                    if (player.isOnline()) {
                        player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "DrawEndWithPrize").replace("%s", prize.getName()));
                        player.getInventory().addItem(prize.getItems());
                    } else {
                        saveItem(prize.getItems());
                        saveMessage(LotteryBoxMain.lang.getTranslation("Tips", "DrawEndWithPrize").replace("%s", prize.getName()));
                    }
                    prize.executeOpCommands(player);
                    prize.executeConsoleCommands(player);
                    prize.checkBroadcast(player);
                    LotteryBoxMain.log.info("玩家 {" + player.getName() + "} 在抽奖箱 {" + lotteryBox.getName() + "} 中抽到物品 {" + prize.getName() + "}!");
                } else {
                    Item[] give = new Item[]{new BlockAir().toItem()};
                    if (player.isOnline()) {
                        player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "DrawEndWithoutPrize"));
                        player.getInventory().addItem(give);
                    }
                }
            }
            LotteryBoxMain.instance.getLogger().warning("Detect [" + player.getName() + "] exit the server, server will retry to give it in his or her next join");
            player.removeWindow(chest.getInventory());
            LotteryBoxMain.playerLotteryBoxes.remove(player);
            LotteryBoxMain.playingPlayers.remove(player);
            Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
            this.cancel();
        }
    }

    private void saveItem(Item[] items) {
        if (!LotteryBoxMain.save_bag_enabled) {
            return;
        }
        Config config = new Config(LotteryBoxMain.path + "/cache.yml", Config.YAML);

        List<String> stringList = new ArrayList<>(config.getStringList(player.getName() + ".items"));
        for (Item item : items) {
            stringList.add(Inventory.saveItemToString(item));
        }
        config.set(player.getName() + ".items", stringList);
        config.save();
    }

    private void saveConsoleCommand(String command) {
        Config config = new Config(LotteryBoxMain.path + "/cache.yml", Config.YAML);

        List<String> stringList = new ArrayList<>(config.getStringList(player.getName() + ".console_commands"));
        stringList.add(command);
        config.set(player.getName() + ".console_commands", stringList);
        config.save();
    }

    private void saveOpCommand(String command) {
        Config config = new Config(LotteryBoxMain.path + "/cache.yml", Config.YAML);
        List<String> stringList = new ArrayList<>(config.getStringList(player.getName() + ".op_commands"));
        stringList.add(command);
        config.set(player.getName() + ".op_commands", stringList);
        config.save();
    }

    private void saveMessage(String message) {
        Config config = new Config(LotteryBoxMain.path + "/cache.yml", Config.YAML);

        List<String> stringList = new ArrayList<>(config.getStringList(player.getName() + ".messages"));
        stringList.add(message);
        config.set(player.getName() + ".messages", stringList);
        config.save();
    }
}
