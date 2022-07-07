package glorydark.lotterybox.forms;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockGlassStained;
import cn.nukkit.block.BlockLever;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBookEnchanted;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import glorydark.lotterybox.MainClass;
import glorydark.lotterybox.tools.LotteryBox;
import glorydark.lotterybox.tools.Prize;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static glorydark.lotterybox.MainClass.chestList;
import static glorydark.lotterybox.forms.GuiType.SelectLotteryBox;

public class CreateGui {
    public static final HashMap<Player, HashMap<Integer, GuiType>> UI_CACHE = new HashMap<>();

    public static void showFormWindow(Player player, FormWindow window, GuiType guiType) {
        UI_CACHE.computeIfAbsent(player, i -> new HashMap<>()).put(player.showFormWindow(window), guiType);
    }

    public static void showSelectLotteryBoxWindow(Player player){
        FormWindowSimple windowSimple = new FormWindowSimple(MainClass.lang.getTranslation("SelectLotteryBoxWindow","Title"), MainClass.lang.getTranslation("SelectLotteryBoxWindow","Content"));
        for(LotteryBox lotteryBox: MainClass.lotteryBoxList){
            windowSimple.addButton(new ElementButton(lotteryBox.getName()));
        }
        showFormWindow(player, windowSimple, SelectLotteryBox);
    }

    public static void showLotteryBoxWindow(Player player, LotteryBox box){
        if(chestList.containsKey(player)){ return; }
        Location pos = new Location(player.getFloorX(), player.getFloorY() + 3, player.getFloorZ(), player.getLevel());
        chestList.put(player, player.getLevel().getBlock(pos));
        player.level.setBlock(pos, Block.get(54));
        player.level.sendBlocks(new Player[]{player}, new Vector3[]{player.level.getBlock(pos)});
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag("Items"))
                .putString("id", "Chest")
                .putInt("x", (int) pos.x)
                .putInt("y", (int) pos.y)
                .putInt("z", (int) pos.z)
                .putString("CustomName", box.getDisplayName());
        BlockEntityChest chest = new BlockEntityChest(player.chunk, nbt);

        chest.getInventory().clearAll();
        //player.addWindow(chest.getInventory());

        Integer index = 0;
        for(Prize prize: box.getPrizes()){
            Item place = prize.getDisplayitem();
            if(!prize.getDescription().equals("")) {
                place.setLore(prize.getDescription());
            }
            if(!prize.isShowOriginName()) {
                place.setCustomName(prize.getName());
            }
            place.setNamedTag(place.getNamedTag().remove("ench"));
            chest.getInventory().setItem(index, place);
            index++;
            if(index == 4){
                index++;
            }
            if(index==12){
                index+=3;
            }
            if(index==22){
                index++;
            }
        }
        Item item = new Item(-161,0,1);
        item.setCustomName(MainClass.lang.getTranslation("PlayLotteryWindow","BlockedArea"));
        Item lever = new BlockLever().toItem();
        lever.setCustomName(MainClass.lang.getTranslation("PlayLotteryWindow","StartLotteryItemName"));
        Item book = new ItemBookEnchanted();
        book.setCustomName(MainClass.lang.getTranslation("PlayLotteryWindow","ShowDescriptionItemName"));
        StringBuilder out = new StringBuilder();
        List<String> stringList = box.getDescription();
        for(String s: stringList){
            if(stringList.indexOf(s) == stringList.size()-1){
                out.append(s);
            }else{
                out.append(s).append("\n");
            }
        }
        book.setLore(out.toString());
        chest.getInventory().setItem(4, book);
        chest.getInventory().setItem(12, item);
        chest.getInventory().setItem(13, lever);
        chest.getInventory().setItem(14, item);
        chest.getInventory().setItem(22, book);
        for(int i=box.getPrizes().size(); i<22; i++){
            Item add = new BlockGlassStained().toItem();
            Integer[] arr = new Integer[]{0,1,2,3,5,6,7,8,9,10,11,15,16,17,18,19,20,21,23,24,25,26};
            List<Integer> allowIndex = Arrays.asList(arr);
            add.setCustomName(MainClass.lang.getTranslation("PlayLotteryWindow","BlockAir"));
            chest.getInventory().setItem(allowIndex.get(i), add);
        }
        MainClass.playerLotteryBoxes.put(player, box);

        player.addWindow(chest.getInventory());
    }
}
