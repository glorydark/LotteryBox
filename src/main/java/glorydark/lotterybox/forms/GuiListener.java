package glorydark.lotterybox.forms;


import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import glorydark.lotterybox.MainClass;

import java.sql.SQLException;
import static glorydark.lotterybox.forms.CreateGui.showLotteryBoxWindowV2;

public class GuiListener implements Listener {

    @EventHandler
    public void PlayerFormRespondedEvent(PlayerFormRespondedEvent event) throws SQLException {
        Player p = event.getPlayer();
        FormWindow window = event.getWindow();
        if (p == null || window == null) {
            return;
        }
        GuiType guiType = CreateGui.UI_CACHE.containsKey(p) ? CreateGui.UI_CACHE.get(p).get(event.getFormID()) : null;
        if(guiType == null){
            return;
        }
        CreateGui.UI_CACHE.get(p).remove(event.getFormID());
        if (event.getResponse() == null) {
            return;
        }
        if (event.getWindow() instanceof FormWindowSimple) {
            this.onSimpleClick(p, (FormWindowSimple) window, guiType);
        }
    }

    private void onSimpleClick(Player player, FormWindowSimple simple, GuiType guiType) throws SQLException {
        if(simple.getResponse() == null){ return; }
        if (guiType == GuiType.SelectLotteryBox) {
            if(MainClass.inSky){
                showLotteryBoxWindowV2(player, MainClass.lotteryBoxList.get(simple.getResponse().getClickedButtonId()));
                return;
            }
            if(player.isOnGround()) {
                showLotteryBoxWindowV2(player, MainClass.lotteryBoxList.get(simple.getResponse().getClickedButtonId()));
            }else{
                player.sendMessage("不支持在空中抽奖！");
            }
        }
    }
}
