package glorydark.lotterybox;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityMinecartChest;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import glorydark.lotterybox.commands.MainCommand;
import glorydark.lotterybox.forms.GuiListener;
import glorydark.lotterybox.languages.Lang;
import glorydark.lotterybox.listeners.EventListeners;
import glorydark.lotterybox.tools.*;

import java.io.File;
import java.util.*;

public class MainClass extends PluginBase {

    public static String path = "";

    public static Lang lang;

    public static List<LotteryBox> lotteryBoxList = new ArrayList<>();

    public static MainClass instance;

    public static HashMap<Player, EntityMinecartChest> chestList = new HashMap<>();

    public static HashMap<Player, LotteryBox> playerLotteryBoxes = new HashMap<>();

    public static List<Player> playingPlayers = new ArrayList<>();

    public static HashMap<String, Rarity> rarities = new HashMap<>();

    public static boolean inSky= false;

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
        this.saveResource("rarity.yml", false);
        path = this.getDataFolder().getPath();
        instance = this;
        Config config = new Config(path+"/config.yml", Config.YAML);
        inSky = config.getBoolean("allow_in_sky", false);
        String language = config.getString("language");
        lang = new Lang(new File(this.getDataFolder()+"/languages/"+language+".yml"));
        //this.getServer().getCommandMap().register("", new TestCommand("test"));
        this.getServer().getCommandMap().register("", new MainCommand("lotterybox"));
        this.getLogger().info("LotteryBox onEnabled!");
        if(config.getInt("version", 0) != 2022080301) {
            //updateConfig();
            config.set("allow_in_sky", true);
            config.set("version", 2022080301);
            config.save();
        }
        Config rarityCfg = new Config(path+"/rarity.yml", Config.YAML);
        for(String key: rarityCfg.getKeys(false)){
            rarities.put(key, new Rarity(rarityCfg.getString(key+".blockParticle", "default")));
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
                if(config.exists("blockParticle")){
                    config.remove("blockParticle");
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
                Map<String, Object> prizesMap = (Map<String, Object>)config.get("prizes");
                for(String key: prizesMap.keySet()){
                    Map<String, Object> keyMap = (Map<String, Object>) prizesMap.get(key);
                    keyMap.put("rarity", "default");
                    prizesMap.put(key, keyMap);
                }
                config.set("prizes", prizesMap);
                config.save();
            }
        }

        Config config = new Config(path+"/languages/zh-cn.yml", Config.YAML);
        config.remove("StartLottery");
        config.set("StartLotteryWithOneSpinsItemName", "单抽");
        config.set("StartLotteryWithTenSpinsItemName", "十连抽");
        config.save();
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
                        prize = new Prize(key, (String) subMap.getOrDefault("description", ""), Inventory.getItem((String) subMap.getOrDefault("displayitem", "0:0:1:null")), (Boolean) subMap.getOrDefault("broadcast", true), items.toArray(new Item[items.size()-1]),(List<String>) subMap.getOrDefault("consolecommands", new ArrayList<>()), (Integer) subMap.getOrDefault("possibility", 5), (Boolean) subMap.getOrDefault("showoriginname", false), (String) subMap.getOrDefault("varity", "default"));
                    }else{
                        prize = new Prize(key, (String) subMap.getOrDefault("description", ""), Inventory.getItem((String) subMap.getOrDefault("displayitem", "0:0:1:null")), (Boolean) subMap.getOrDefault("broadcast", true), items.toArray(new Item[0]),(List<String>) subMap.getOrDefault("consolecommands", new ArrayList<>()), (Integer) subMap.getOrDefault("possibility", 5), (Boolean) subMap.getOrDefault("showoriginname", false), (String) subMap.getOrDefault("varity", "default"));
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
                LotteryBox lotteryBox = new LotteryBox(file.getName().split("\\.")[0],config.getString("displayName"), config.getStringList("needs"), config.getStringList("descriptions"), prizes, bonuses, config.getInt("permanentLimit"), config.getBoolean("spawnFirework"), config.getString("endParticle"));
                lotteryBoxList.add(lotteryBox);
                Server.getInstance().getLogger().info(MainClass.lang.getTranslation("Tips","LotteryBoxLoaded").replaceFirst("%s",lotteryBox.getName()));
            }
            Server.getInstance().getLogger().info(MainClass.lang.getTranslation("Tips","LotteryBoxFinish").replaceFirst("%d", lotteryBoxList.size()+""));
        }
    }

    @Override
    public void onDisable() {
        this.getLogger().info("LotteryBox onDisabled!");
    }
}
