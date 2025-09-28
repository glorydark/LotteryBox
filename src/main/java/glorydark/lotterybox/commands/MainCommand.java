package glorydark.lotterybox.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import glorydark.lotterybox.LotteryBoxMain;
import glorydark.lotterybox.api.LotteryBoxAPI;
import glorydark.lotterybox.forms.FormFactory;
import glorydark.lotterybox.languages.Lang;
import glorydark.lotterybox.tools.Inventory;
import glorydark.lotterybox.tools.LotteryBox;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import static glorydark.lotterybox.forms.FormFactory.showLotteryBoxWindowV2;

public class MainCommand extends Command {

    public MainCommand(String command) {
        super(command);
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (strings.length < 1) {
            return false;
        }
        switch (strings[0]) {
            case "menu":
                switch (strings.length) {
                    case 1:
                        if (commandSender instanceof Player) {
                            FormFactory.showSelectLotteryBoxWindow(((Player) commandSender).getPlayer());
                        } else {
                            commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "CommandShouldBeUsedInGame"));
                        }
                        break;
                    case 2:
                        if (commandSender.isPlayer()) {
                            Player player = (Player) commandSender;
                            if (!LotteryBoxAPI.isPE(player) && !player.isOnGround()) {
                                player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "NoOnGround"));
                                return true;
                            }
                            Optional<LotteryBox> optional = LotteryBoxMain.lotteryBoxList.stream().filter(lotteryBox -> lotteryBox.getName().equals(strings[1])).findFirst();
                            if (optional.isPresent()) {
                                LotteryBox box = optional.get();
                                LotteryBoxMain.playerLotteryBoxes.put(player, box);
                                if (!LotteryBoxAPI.isPE(player) && !LotteryBoxMain.forceDefaultMode) {
                                    if (box.isWeightEnabled()) {
                                        FormFactory.showLotteryPossibilityWindow(player, box);
                                    } else {
                                        showLotteryBoxWindowV2(player, box);
                                    }
                                } else {
                                    FormFactory.showLotteryPossibilityWindow(player, box);
                                }
                            } else {
                                commandSender.sendMessage("Can not find the lottery from the name given accordingly!");
                            }
                        }
                        break;
                }
                break;
            case "give": //give player ticket amount
                if (!(commandSender instanceof Player) || commandSender.isOp()) {
                    if (strings.length == 4) {
                        if (Server.getInstance().lookupName(strings[1]).isPresent()) {
                            if (LotteryBoxMain.registered_tickets.contains(strings[2])) {
                                LotteryBoxAPI.changeTicketCounts(strings[1], strings[2], Integer.valueOf(strings[3]));
                                Player player = Server.getInstance().getPlayer(strings[1]);
                                if (player != null) {
                                    player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "ReceiveTicket", strings[2], strings[3]));
                                }
                                commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "GiveTicketSuccess"));
                            } else {
                                commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "TicketWithoutRegistration", LotteryBoxMain.registered_tickets));
                            }
                        } else {
                            commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "PlayerNotFound"));
                        }
                    } else {
                        commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "UseCommandInWrongFormat"));
                    }
                } else {
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "NoPermission"));
                }
                break;
            case "help":
                if (!(commandSender instanceof Player) || commandSender.isOp()) {
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "Title"));
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "GiveCommand"));
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "OpenMenuCommand"));
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "HelpCommand"));
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "SaveItemCommand"));
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "ReloadCommand"));
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "CreateLotteryBoxCommand"));
                } else {
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "Title"));
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "OpenMenuCommand"));
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Helps", "HelpCommand"));
                }
                break;
            case "saveitem":
                if (commandSender instanceof Player) {
                    if (commandSender.isOp()) {
                        if (strings.length == 2) {
                            Config config = new Config(LotteryBoxMain.path + File.separator + "saveitem.yml", Config.YAML);
                            if (!config.exists(strings[1])) {
                                config.set(strings[1], Inventory.saveItemToString(((Player) commandSender).getInventory().getItemInHand()));
                                commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "SaveItemSuccessfully"));
                            } else {
                                config.set(strings[1] + "-copy", Inventory.saveItemToString(((Player) commandSender).getInventory().getItemInHand()));
                                commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "SaveItemExists", strings[1] + "-copy"));
                            }
                            config.save();
                        } else {
                            commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "UseCommandInWrongFormat"));
                        }
                    } else {
                        commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "NoPermission"));
                    }
                } else {
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "CommandShouldBeUsedInGame"));
                }
                break;
            case "reload":
                if (!(commandSender instanceof Player) || commandSender.isOp()) {
                    Config config = new Config(LotteryBoxMain.path + File.separator + "config.yml", Config.YAML);
                    LotteryBoxMain.forceDefaultMode = config.getBoolean("force_default_mode", false);
                    LotteryBoxMain.default_speed_ticks = config.getInt("default_speed_ticks", 4);
                    LotteryBoxMain.chest_speed_ticks = config.getInt("chest_speed_ticks", 4);
                    LotteryBoxMain.banWorlds = new ArrayList<>(config.getStringList("ban_worlds"));
                    LotteryBoxMain.banWorldPrefix = new ArrayList<>(config.getStringList("ban_worlds_prefixs"));
                    LotteryBoxMain.show_reward_window = config.getBoolean("show_reward_window", true);
                    LotteryBoxMain.showType = config.getString("show_type", "actionbar");
                    LotteryBoxMain.inventory_cache_paths = new ArrayList<>(config.getStringList("inventory_cache_paths"));
                    LotteryBoxMain.save_bag_enabled = config.getBoolean("save_bag_enabled", true);
                    LotteryBoxMain.registered_tickets = new ArrayList<>(config.getStringList("registered_tickets"));
                    String language = config.getString("language");
                    LotteryBoxMain.lang = new Lang(new File(LotteryBoxMain.path + File.separator + "languages" + File.separator + language + ".yml"));
                    LotteryBoxMain.loadBoxesConfig();
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "ReloadFinish"));
                } else {
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "NoPermission"));
                }
                break;
            case "createbox":
                if (!(commandSender instanceof Player) || commandSender.isOp()) {
                    if (strings.length == 2) {
                        File file = new File(LotteryBoxMain.path + File.separator + "boxes" + File.separator + strings[1] + ".yml");
                        if (!file.exists()) {
                            LotteryBoxMain.instance.saveResource("default.yml", "boxes" + File.separator + strings[1] + ".yml", false);
                            commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "CreateLotteryBoxSuccessfully", strings[1]));
                        } else {
                            commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "LotteryBoxExisted", strings[1]));
                        }
                    } else {
                        commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "UseCommandInWrongFormat"));
                    }
                } else {
                    commandSender.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "NoPermission"));
                }
                break;
        }
        return true;
    }
}
