package glorydark.lotterybox.forms;


import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import glorydark.lotterybox.LotteryBoxMain;
import glorydark.lotterybox.api.LotteryBoxAPI;
import glorydark.lotterybox.event.LotteryForceCloseEvent;
import glorydark.lotterybox.tasks.nonWeight.InventoryChangeTask;
import glorydark.lotterybox.tasks.weight.InventoryChangeTaskV2;
import glorydark.lotterybox.tools.ExchangeCache;
import glorydark.lotterybox.tools.LotteryBox;
import me.onebone.economyapi.EconomyAPI;

import static glorydark.lotterybox.forms.FormFactory.*;

public class FormListener implements Listener {

    @EventHandler
    public void PlayerFormRespondedEvent(PlayerFormRespondedEvent event) {
        Player p = event.getPlayer();
        FormWindow window = event.getWindow();
        if (p == null || window == null) {
            return;
        }
        FormType formType = FormFactory.UI_CACHE.containsKey(p) ? FormFactory.UI_CACHE.get(p).get(event.getFormID()) : null;
        if (formType == null) {
            return;
        }
        FormFactory.UI_CACHE.get(p).remove(event.getFormID());
        if (event.getResponse() == null) {
            return;
        }
        if (event.getWindow() instanceof FormWindowSimple) {
            this.onSimpleClick(p, (FormWindowSimple) window, formType);
        } else if (event.getWindow() instanceof FormWindowCustom) {
            this.onCustomClick(p, (FormWindowCustom) window, formType);
        } else if (event.getWindow() instanceof FormWindowModal) {
            this.onModalClick(p, (FormWindowModal) window, formType);
        }
    }

    private void onSimpleClick(Player player, FormWindowSimple simple, FormType formType) {
        if (simple.getResponse() == null) {
            return;
        }
        if (LotteryBoxMain.playingPlayers.contains(player)) {
            return;
        }
        LotteryBox box = LotteryBoxMain.lotteryBoxList.get(simple.getResponse().getClickedButtonId());
        switch (formType) {
            case SelectLotteryBox:
                if (!LotteryBoxAPI.isPE(player) && !player.isOnGround()) {
                    player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "NoOnGround"));
                    return;
                }
                LotteryBoxMain.playerLotteryBoxes.put(player, box);
                if (!LotteryBoxAPI.isPE(player) && !LotteryBoxMain.forceDefaultMode) {
                    if (box.isWeightEnabled()) {
                        FormFactory.showLotteryPossibilityWindow(player, box);
                        //showPESelectSpinWindow(player);
                    } else {
                        showLotteryBoxWindowV2(player, box);
                    }
                } else {
                    FormFactory.showLotteryPossibilityWindow(player, box);
                    //showPESelectSpinWindow(player);
                }
                break;
            case LotteryPossibility:
                if (simple.getResponse().getClickedButtonId() == 0) {
                    showPESelectSpinWindow(player, box);
                }
                break;
        }
    }

    private void onCustomClick(Player player, FormWindowCustom custom, FormType formType) {
        if (custom.getResponse() == null) {
            return;
        }
        if (formType == FormType.SelectLotterySpin) {
            int spin = (int) custom.getResponse().getSliderResponse(1);
            LotteryBox box = LotteryBoxMain.playerLotteryBoxes.get(player);
            if (box.checkLimit(player.getName(), spin)) {
                if (box.deductNeeds(player, spin)) {
                    if (box.isWeightEnabled()) {
                        Server.getInstance().getScheduler().scheduleRepeatingTask(LotteryBoxMain.instance, new InventoryChangeTaskV2(player, LotteryBoxMain.playerLotteryBoxes.get(player), spin), LotteryBoxMain.default_speed_ticks);
                    } else {
                        Server.getInstance().getScheduler().scheduleRepeatingTask(LotteryBoxMain.instance, new InventoryChangeTask(player, LotteryBoxMain.playerLotteryBoxes.get(player), spin), LotteryBoxMain.default_speed_ticks);
                    }
                } else {
                    Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
                }
            } else {
                player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "TimesLimit"));
                Server.getInstance().getPluginManager().callEvent(new LotteryForceCloseEvent(player));
            }
        }
    }

    private void onModalClick(Player player, FormWindowModal response, FormType formType) {
        if (response.getResponse() == null) {
            return;
        }
        if (formType == FormType.ExchangeConfirmWindow) {
            if (response.getResponse().getClickedButtonId() == 0) {
                ExchangeCache cache = exchangeCaches.get(player);
                if (cache != null) {
                    EconomyAPI.getInstance().reduceMoney(player, cache.getMoneyCost());
                    LotteryBoxAPI.changeTicketCounts(player.getName(), cache.getTicketName(), cache.getTicketBuyCount());
                    player.sendMessage(LotteryBoxMain.lang.getTranslation("Tips", "ExchangeTicketSuccess", cache.getMoneyCost(), cache.getTicketName(), cache.getTicketBuyCount()));
                    exchangeCaches.remove(player);
                }
            }
        }
    }
}
