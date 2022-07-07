package glorydark.lotterybox.tasks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.item.enchantment.protection.EnchantmentProtectionAll;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DyeColor;
import glorydark.lotterybox.MainClass;
import glorydark.lotterybox.api.CreateFireworkApi;
import glorydark.lotterybox.tools.*;

import java.util.*;

import static glorydark.lotterybox.listeners.EventListeners.removeChest;

public class LotteryBoxChangeTask extends Task implements Runnable {
    private final Map<Integer, Item> inventory;
    private final BlockEntityChest chest;

    private final Integer maxIndex;

    private Integer index = 0;

    private final Player player;

    private final LotteryBox lotteryBox;

    private final List<Integer> allowIndex;

    private int ticks;

    public LotteryBoxChangeTask(Block chest, Player player, LotteryBox box) {
        this.chest = (BlockEntityChest) chest.getLevelBlockEntity();
        this.inventory = ((BlockEntityChest) Objects.requireNonNull(chest.getLevelBlockEntity())).getInventory().getContents();
        this.player = player;
        this.lotteryBox = box;
        Integer[] arr = new Integer[]{0,1,2,3,5,6,7,8,9,10,11,15,16,17,18,19,20,21,23,24,25,26};
        allowIndex = Arrays.asList(arr);
        this.maxIndex = 44+getMaxIndex();
    }

    public Integer getMaxIndex(){
        Random random = new Random();
        for(Prize prize: lotteryBox.getPrizes()){
            List<Integer> integers = new ArrayList<>();
            for(int i=0; i<prize.getPossibility(); i++){
                integers.add(Math.abs(random.nextInt())%10000);
            }
            if(integers.contains(Math.abs(random.nextInt())%10000)){
                return lotteryBox.getPrizes().indexOf(prize);
            }
        }
        List<Integer> absent = new ArrayList<>();
        for(int i = lotteryBox.getPrizes().size(); i<22; i++){
            absent.add(allowIndex.get(i));
        }
        if(absent.size() > 0) {
            return absent.get(random.nextInt(absent.size()));
        }else{
            return random.nextInt(22);
        }
    }

    @Override
    public void onRun(int i) {
        ticks+=1;
        if(chest.isValid() && !chest.closed && player.isOnline()){
            if(index < maxIndex) {
                if(index < 2 || index + 2 >= maxIndex){
                    if(ticks%4 != 0){
                        return;
                    }
                }else{
                    if(index < 5 || index + 5 >= maxIndex){
                        if(ticks%2 != 0){
                            return;
                        }
                    }
                }
                Integer realIndex = allowIndex.get(index % 22);
                chest.getInventory().setContents(inventory);
                Item item = chest.getInventory().getItem(realIndex);
                item.addEnchantment(new EnchantmentProtectionAll());
                chest.getInventory().setItem(realIndex, item);
                player.getLevel().addSound(player.getPosition(), Sound.NOTE_BASS);
                lotteryBox.addBlockParticle(player);
                index++;
            }else{
                Item item = inventory.get(allowIndex.get(maxIndex % 22));
                Item[] give;
                List<Prize> prizes = lotteryBox.getPrizes();
                Integer realIndex = allowIndex.get(maxIndex % 22);
                Prize prize = null;
                if(realIndex < prizes.size()){
                    give = prizes.get(realIndex).getItems();
                    prize = prizes.get(realIndex);
                }else{
                    give = new Item[]{new BlockAir().toItem()};
                }
                item.addEnchantment(new EnchantmentProtectionAll());
                chest.getInventory().setItem(maxIndex%27, item);
                //setEnchanted
                if(lotteryBox.isSpawnFirework()) {
                    CreateFireworkApi.spawnFirework(player.getPosition(), DyeColor.YELLOW, ItemFirework.FireworkExplosion.ExplosionType.BURST);
                }
                lotteryBox.showEndParticle(player);
                player.removeWindow(chest.getInventory());
                removeChest(player, true);
                //
                if(!item.getCustomName().equals(MainClass.lang.getTranslation("PlayLotteryWindow","BlockAir"))) {
                    player.getInventory().addItem(give);
                    player.sendMessage(MainClass.lang.getTranslation("Tips","DrawEndWithPrize").replace("%s", Objects.requireNonNull(prize).getName()));
                    for(String s: prize.getConsolecommands()){
                        Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), s.replace("%player%", player.getName()));
                    }
                    if(prize.isBroadcast()){
                        Server.getInstance().broadcastMessage(MainClass.lang.getTranslation("Tips", "PrizeBroadcast").replaceFirst("%s", player.getName()).replaceFirst("%s1", prize.getName()));
                    }
                    BasicTool.changeLotteryPlayTimes(player.getName(), lotteryBox.getName(), 1);
                    if(lotteryBox.getBonus(BasicTool.getLotteryPlayTimes(player.getName(), lotteryBox.getName())) != null){
                        Bonus bonus = lotteryBox.getBonus(BasicTool.getLotteryPlayTimes(player.getName(), lotteryBox.getName()));
                        for(String s: bonus.getConsolecommands()){
                            Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), s.replace("%player%", player.getName()));
                        }
                        player.getInventory().addItem(bonus.getItems());
                        Server.getInstance().broadcastMessage(MainClass.lang.getTranslation("Tips", "BonusBroadcast").replaceFirst("%s", player.getName()).replaceFirst("%s1", lotteryBox.getName()).replaceFirst("%s2", bonus.getName()).replaceFirst("%d", bonus.getName()+""));
                    }
                }else{
                    player.sendMessage(MainClass.lang.getTranslation("Tips","DrawEndWithoutPrize"));
                }
                this.cancel();
            }
        }else{
            List<Prize> prizes = lotteryBox.getPrizes();
            Integer realIndex = allowIndex.get(maxIndex % 22);
            if(realIndex < prizes.size()){
                Prize prize = prizes.get(realIndex);
                if(player.isOnline()) {
                    player.sendMessage(MainClass.lang.getTranslation("Tips", "DrawEndWithPrize").replace("%s", prize.getName()));
                    player.getInventory().addItem(prize.getItems());
                }else{
                    save(prize.getItems());
                }
                for(String s: prize.getConsolecommands()){
                    Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), s.replace("%player%", player.getName()));
                }
                if(prize.isBroadcast()){
                    Server.getInstance().broadcastMessage(MainClass.lang.getTranslation("Tips", "PrizeBroadcast").replaceFirst("%s", player.getName()).replaceFirst("%s1", prize.getName()));
                }
                BasicTool.changeLotteryPlayTimes(player.getName(), lotteryBox.getName(), 1);
                if(lotteryBox.getBonus(BasicTool.getLotteryPlayTimes(player.getName(), lotteryBox.getName())) != null){
                    Bonus bonus = lotteryBox.getBonus(BasicTool.getLotteryPlayTimes(player.getName(), lotteryBox.getName()));
                    for(String s: bonus.getConsolecommands()){
                        Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), s.replace("%player%", player.getName()));
                    }
                    if(player.isOnline()) {
                        player.getInventory().addItem(prize.getItems());
                    }else{
                        save(prize.getItems());
                    }
                    Server.getInstance().broadcastMessage(MainClass.lang.getTranslation("Tips", "BonusBroadcast").replaceFirst("%s", player.getName()).replaceFirst("%s1", lotteryBox.getName()).replaceFirst("%s2", bonus.getName()).replaceFirst("%d", bonus.getName()+""));
                }
            }else{
                Item[] give = new Item[]{new BlockAir().toItem()};
                if(player.isOnline()) {
                    player.sendMessage(MainClass.lang.getTranslation("Tips", "DrawEndWithoutPrize"));
                    player.getInventory().addItem(give);
                }
            }
            removeChest(player, true);
            this.cancel();
        }
    }

    private void save(Item[] items){
        Config config = new Config(MainClass.path+"/cache.yml", Config.YAML);
        List<String> stringList = new ArrayList<>(config.getStringList(player.getName()));
        for(Item item: items) {
            stringList.add(Inventory.saveItemToString(item));
        }
        config.set(player.getName(), stringList);
        config.save();
        MainClass.instance.getLogger().warning("Detect ["+player.getName()+"] exit the server, server will retry to give it in his or her next join");
    }
}
