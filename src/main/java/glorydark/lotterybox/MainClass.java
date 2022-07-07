package glorydark.lotterybox;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import glorydark.lotterybox.commands.MainCommand;
import glorydark.lotterybox.forms.GuiListener;
import glorydark.lotterybox.languages.Lang;
import glorydark.lotterybox.listeners.EventListeners;
import glorydark.lotterybox.tools.Bonus;
import glorydark.lotterybox.tools.Inventory;
import glorydark.lotterybox.tools.LotteryBox;
import glorydark.lotterybox.tools.Prize;

import java.io.File;
import java.util.*;

public class MainClass extends PluginBase {

    public static String path = "";

    public static Lang lang;

    public static List<LotteryBox> lotteryBoxList = new ArrayList<>();

    public static MainClass instance;

    public static HashMap<Player, Block> chestList = new HashMap<>();

    public static HashMap<Player, LotteryBox> playerLotteryBoxes = new HashMap<>();

    public static List<Player> playingPlayers = new ArrayList<>();

    @Override
    public void onLoad() {
        this.getLogger().info("LotteryBox onLoad!");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new File(path+"/languages/").mkdir();
        new File(path+"/players/").mkdir();
        new File(path+"/boxes/").mkdir();
        new File(path+"/tickets/").mkdir();
        this.saveResource("languages/zh-cn.yml", false);
        path = this.getDataFolder().getPath();
        instance = this;
        Config config = new Config(path+"/config.yml", Config.YAML);
        String language = config.getString("language");
        lang = new Lang(new File(this.getDataFolder()+"/languages/"+language+".yml"));
        //this.getServer().getCommandMap().register("", new TestCommand("test"));
        this.getServer().getCommandMap().register("", new MainCommand("lotterybox"));
        this.getLogger().info("LotteryBox onEnabled!");
        if(!config.exists("version")) {
            updateConfig();
            config.set("version", 2022070401);
            config.save();
        }
        loadBoxesConfig();
        this.getServer().getPluginManager().registerEvents(new EventListeners(), this);
        this.getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

    public void updateConfig(){
        File folder = new File(path+"/boxes/");
        if(folder.exists()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                Config config = new Config(file, Config.YAML);
                if(!config.exists("displayName")){
                    config.set("displayName", "test");
                }
                if(!config.exists("blockParticle")){
                    config.set("blockParticle", "133:0");
                }
                if(!config.exists("displayName")) {
                    config.set("displayName", "test");
                }
                if(!config.exists("endParticle")) {
                    config.set("endParticle", "hugeexplosion");
                }
                if(!config.exists("spawnFirework")) {
                    config.set("spawnFirework", true);
                }
                if(!config.exists("permanentLimit")) {
                    config.set("permanentLimit", 1);
                }
                config.save();
            }
        }
    }

    public static void loadBoxesConfig(){
        File folder = new File(path+"/boxes/");
        lotteryBoxList.clear();
        if(folder.exists()){
            for(File file: Objects.requireNonNull(folder.listFiles())){
                Config config = new Config(file, Config.YAML);
                Map<String, Object> prizesMap = (Map<String, Object>)config.get("prizes");
                List<Prize> prizes = new ArrayList<>();
                for(String key: prizesMap.keySet()){
                    Map<String, Object> subMap = (Map<String, Object>) prizesMap.get(key);
                    List<Item> items = new ArrayList<>();
                    for(String itemString: (List<String>)subMap.get("items")){
                        items.add(Inventory.getItem(itemString));
                    }
                    Prize prize;
                    if(items.size()>0){
                        prize = new Prize(key, (String) subMap.getOrDefault("description", ""), Inventory.getItem((String) subMap.getOrDefault("displayitem", "0:0:1:null")), (Boolean) subMap.getOrDefault("broadcast", true), items.toArray(new Item[items.size()-1]),(List<String>) subMap.getOrDefault("consolecommands", new ArrayList<>()), (Integer) subMap.getOrDefault("possibility", 5), (Boolean) subMap.getOrDefault("showoriginname", false));
                    }else{
                        prize = new Prize(key, (String) subMap.getOrDefault("description", ""), Inventory.getItem((String) subMap.getOrDefault("displayitem", "0:0:1:null")), (Boolean) subMap.getOrDefault("broadcast", true), items.toArray(new Item[0]),(List<String>) subMap.getOrDefault("consolecommands", new ArrayList<>()), (Integer) subMap.getOrDefault("possibility", 5), (Boolean) subMap.getOrDefault("showoriginname", false));
                    }
                    prizes.add(prize);
                }

                Map<String, Object> bonusesMap = (Map<String, Object>)config.get("bonuses");
                List<Bonus> bonuses = new ArrayList<>();
                for(String key: bonusesMap.keySet()){
                    Map<String, Object> subMap = (Map<String, Object>) bonusesMap.get(key);
                    List<Item> items = new ArrayList<>();
                    for(String itemString: (List<String>)subMap.get("items")){
                        items.add(Inventory.getItem(itemString));
                    }
                    bonuses.add(new Bonus(key, items.toArray(new Item[items.size()-1]), (List<String>) subMap.get("consolecommands"), (Integer) subMap.get("possibility")));
                }
                String particle = config.getString("blockParticle");
                Block block = null;
                if(particle.equals("null")) {
                    String[] particleSplit = particle.split(":");
                    block = Block.get(Integer.parseInt(particleSplit[0]), Integer.parseInt(particleSplit[1]));
                }
                LotteryBox lotteryBox = new LotteryBox(file.getName().split("\\.")[0],config.getString("displayName"), config.getStringList("needs"), config.getStringList("descriptions"), prizes, bonuses, block, config.getInt("permanentLimit"), config.getBoolean("spawnFirework"), config.getString("endParticle"));
                lotteryBoxList.add(lotteryBox);
                Server.getInstance().getLogger().info(MainClass.lang.getTranslation("PlayLotteryWindow","LotteryBoxLoaded").replaceFirst("%s",lotteryBox.getName()));
            }
            Server.getInstance().getLogger().info(MainClass.lang.getTranslation("PlayLotteryWindow","LotteryBoxFinish").replaceFirst("%d", lotteryBoxList.size()+""));
        }
    }

    @Override
    public void onDisable() {
        for(Player p: MainClass.chestList.keySet()){
            Block chest = MainClass.chestList.get(p);
            p.level.setBlock(chest.getLocation(), Block.get(0));
            p.level.removeBlockEntity(chest.getLevelBlockEntity());
        }
        this.getLogger().info("LotteryBox onDisabled!");
    }
}
