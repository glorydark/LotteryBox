package glorydark.lotterybox;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityMinecartChest;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.variable.BaseVariableV2;
import com.smallaswater.npc.variable.VariableManage;
import glorydark.lotterybox.api.LotteryBoxAPI;
import glorydark.lotterybox.commands.MainCommand;
import glorydark.lotterybox.forms.FormListener;
import glorydark.lotterybox.languages.Lang;
import glorydark.lotterybox.listeners.EventListeners;
import glorydark.lotterybox.logger.LoggerFormatter;
import glorydark.lotterybox.tools.*;
import tip.utils.Api;
import tip.utils.variables.BaseVariable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class LotteryBoxMain extends PluginBase {

    public static String path = "";

    public static Lang lang;

    public static List<LotteryBox> lotteryBoxList = new ArrayList<>();

    public static LotteryBoxMain instance;

    public static HashMap<Player, EntityMinecartChest> chestList = new HashMap<>();

    public static HashMap<Player, LotteryBox> playerLotteryBoxes = new HashMap<>();

    public static List<Player> playingPlayers = new ArrayList<>();

    public static HashMap<String, Rarity> rarities = new HashMap<>();

    public static boolean forceDefaultMode = false;

    public static int default_speed_ticks;

    public static int chest_speed_ticks;

    public static List<String> banWorlds;

    public static List<String> banWorldPrefix;

    public static String showType;

    public static boolean show_reward_window;

    public static List<String> inventory_cache_paths;

    public static boolean save_bag_enabled;

    public static List<String> registered_tickets;

    public static LinkedHashMap<String, Double> ticketPrice = new LinkedHashMap<>();

    public static boolean economyAPIEnabled;

    public static Logger log;

    public static boolean isWorldAvailable(String level) {
        for (String prefix : banWorldPrefix) {
            if (level.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    public static void loadBoxesConfig() {
        File folder = new File(path + "/boxes/");
        lotteryBoxList.clear();
        if (folder.exists()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                Config config = new Config(file, Config.YAML);
                Map<String, Object> prizesMap = config.get("prizes", new HashMap<>());
                List<Prize> prizes = new ArrayList<>();
                for (String key : prizesMap.keySet()) {
                    Map<String, Object> subMap = (Map<String, Object>) prizesMap.get(key);
                    List<Item> items = new ArrayList<>();
                    for (String itemString : (List<String>) subMap.get("items")) {
                        items.add(Inventory.getItem(itemString));
                    }
                    Prize prize;
                    prize = new Prize(key, (String) subMap.getOrDefault("description", ""), Inventory.getItem((String) subMap.getOrDefault("displayitem", "1:0:1:null")), (Boolean) subMap.getOrDefault("broadcast", true), items.toArray(new Item[0]), (List<String>) subMap.getOrDefault("consolecommands", new ArrayList<>()), (List<String>) subMap.getOrDefault("op_commands", new ArrayList<>()), (Integer) subMap.getOrDefault("possibility", 5), (Boolean) subMap.getOrDefault("showoriginname", false), (String) subMap.getOrDefault("rarity", "default"), (Integer) subMap.getOrDefault("max_gained_time", -1));
                    prizes.add(prize);
                }

                Map<String, Object> bonusesMap = (Map<String, Object>) config.get("bonuses");
                List<Bonus> bonuses = new ArrayList<>();
                for (String key : bonusesMap.keySet()) {
                    Map<String, Object> subMap = (Map<String, Object>) bonusesMap.get(key);
                    List<Item> items = new ArrayList<>();
                    for (String itemString : (List<String>) subMap.get("items")) {
                        items.add(Inventory.getItem(itemString));
                    }
                    bonuses.add(new Bonus(key, items.toArray(new Item[0]), (List<String>) subMap.get("consolecommands"), (Integer) subMap.get("times")));
                }
                ElementButtonImageData elementButtonImageData = null;
                if (config.exists("button_image_data")) {
                    String imageData = config.getString("button_image_data");
                    if (imageData.startsWith("url#")) {
                        elementButtonImageData = new ElementButtonImageData("url", imageData.replaceFirst("url#", ""));
                    } else if (imageData.startsWith("path#")) {
                        elementButtonImageData = new ElementButtonImageData("path", imageData.replaceFirst("path#", ""));
                    }
                }
                LotteryBox lotteryBox = new LotteryBox(file.getName().split("\\.")[0], config.getInt("priority"), config.getString("displayName"), config.getStringList("needs"), config.getStringList("descriptions"), prizes, bonuses, config.getInt("permanentLimit"), config.getBoolean("spawnFirework"), config.getString("endParticle"), config.getString("sound", Sound.RANDOM_ORB.getSound()), config.getBoolean("weightEnabled", false), config.getInt("max_draw_per_time"), elementButtonImageData);
                lotteryBoxList.add(lotteryBox);
                Server.getInstance().getLogger().info(LotteryBoxMain.lang.getTranslation("Tips", "LotteryBoxLoaded", lotteryBox.getName()));
            }
            LotteryBoxMain.lotteryBoxList.sort(Comparator.comparing(LotteryBox::getPriority).reversed());
            Server.getInstance().getLogger().info(LotteryBoxMain.lang.getTranslation("Tips", "LotteryBoxFinish", lotteryBoxList.size()));
        }
    }

    public static String getDate(long millis) {
        Date date = new Date(millis);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        return format.format(date);
    }

    @Override
    public void onLoad() {
        this.getLogger().info("LotteryBox onLoad!");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new File(path + "/languages/").mkdirs();
        new File(path + "/lottery_records/").mkdirs();
        new File(path + "/prize_records/").mkdirs();
        new File(path + "/boxes/").mkdirs();
        new File(path + "/tickets/").mkdirs();
        this.saveResource("languages/zh-cn.yml", false);
        this.saveResource("rarity.yml", false);

        path = this.getDataFolder().getPath();
        instance = this;
        log = Logger.getLogger("LotteryBox_" + UUID.randomUUID());
        new File(path + "/logs/").mkdirs();

        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler(path + "/logs/" + getDate(System.currentTimeMillis()) + ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.setFormatter(new LoggerFormatter());
        log.addHandler(fileHandler);

        Config config = new Config(path + "/config.yml", Config.YAML);
        if (config.getInt("version", 0) != 2022082002) {
            updateConfig();
            if (config.exists("registered_tickets")) {
                config.set("registered_tickets", new ArrayList<>());
            }
            config.set("version", 2022082002);
            config.save();
        }
        forceDefaultMode = config.getBoolean("force_default_mode", false);
        default_speed_ticks = config.getInt("default_speed_ticks", 4);
        chest_speed_ticks = config.getInt("chest_speed_ticks", 4);
        banWorlds = new ArrayList<>(config.getStringList("ban_worlds"));
        banWorldPrefix = new ArrayList<>(config.getStringList("ban_worlds_prefixs"));
        show_reward_window = config.getBoolean("show_reward_window", true);
        showType = config.getString("show_type", "actionbar");
        inventory_cache_paths = new ArrayList<>(config.getStringList("inventory_cache_paths"));
        save_bag_enabled = config.getBoolean("save_bag_enabled", true);
        registered_tickets = new ArrayList<>(config.getStringList("registered_tickets"));
        ticketPrice = config.get("ticket_price_economyapi", new LinkedHashMap<>());
        String language = config.getString("language");
        lang = new Lang(new File(this.getDataFolder() + "/languages/" + language + ".yml"));
        economyAPIEnabled = Server.getInstance().getPluginManager().getPlugin("EconomyAPI") != null;
        if (economyAPIEnabled) {
            this.getLogger().info(LotteryBoxMain.lang.getTranslation("Tips", "DependencyFound", "EconomyAPI"));
        } else {
            this.getLogger().error(LotteryBoxMain.lang.getTranslation("Tips", "DependencyMissing", "EconomyAPI"));
        }
        //this.getServer().getCommandMap().register("", new TestCommand("test"));
        this.getServer().getCommandMap().register("", new MainCommand("lotterybox"));
        Config rarityCfg = new Config(path + "/rarity.yml", Config.YAML);
        for (String key : rarityCfg.getKeys(false)) {
            rarities.put(key, new Rarity(rarityCfg.getString(key + ".blockParticle", "default")));
        }
        loadBoxesConfig();
        this.getServer().getPluginManager().registerEvents(new EventListeners(), this);
        this.getServer().getPluginManager().registerEvents(new FormListener(), this);
        if (this.getServer().getPluginManager().getPlugin("Tips") != null) {
            this.getLogger().info(LotteryBoxMain.lang.getTranslation("Tips", "DependencyFound", "Tips"));
            Api.registerVariables("LotteryBox", LotteryBoxTipsVariable.class);
        }

        if (this.getServer().getPluginManager().getPlugin("RsNPC") != null) {
            this.getLogger().info(LotteryBoxMain.lang.getTranslation("Tips", "DependencyFound", "RsNPC"));
            VariableManage.addVariableV2("LotteryBox", LotteryBoxRsNPCVariable.class);
        }
        this.getLogger().info("LotteryBox onEnabled!");
    }

    public void updateConfig() {
        File folder = new File(path + "/boxes/");
        if (folder.exists()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                Config config = new Config(file, Config.YAML);
                if (!config.exists("weightEnabled")) {
                    config.set("weightEnabled", false);
                }
                config.save();
            }
        }
    }

    @Override
    public void onDisable() {
        this.getLogger().info("LotteryBox onDisabled!");
    }

    public static class LotteryBoxTipsVariable extends BaseVariable {

        public LotteryBoxTipsVariable(Player player) {
            super(player);
        }

        @Override
        public void strReplace() {
            for (String ticket : new ArrayList<>(registered_tickets)) {
                this.addStrReplaceString("{lotterybox_tickets_" + ticket + "}", String.valueOf(LotteryBoxAPI.getTicketCounts(player.getName(), ticket)));
            }
            for (LotteryBox box : new ArrayList<>(lotteryBoxList)) {
                this.addStrReplaceString("{lotterybox_playtimes_" + box.getName() + "}", String.valueOf(LotteryBoxAPI.getLotteryPlayTimes(player.getName(), box.getName())));
            }
        }
    }

    public static class LotteryBoxRsNPCVariable extends BaseVariableV2 {
        @Override
        public void onUpdate(Player player, RsNpcConfig rsNpcConfig) {
            if (player == null) {
                return;
            }
            for (String ticket : new ArrayList<>(registered_tickets)) {
                this.addVariable("{lotterybox_tickets_" + ticket + "}", String.valueOf(LotteryBoxAPI.getTicketCounts(player.getName(), ticket)));
            }
            for (LotteryBox box : new ArrayList<>(lotteryBoxList)) {
                this.addVariable("{lotterybox_playtimes_" + box.getName() + "}", String.valueOf(LotteryBoxAPI.getLotteryPlayTimes(player.getName(), box.getName())));
            }
        }
    }
}
