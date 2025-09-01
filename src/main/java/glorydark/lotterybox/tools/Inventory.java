package glorydark.lotterybox.tools;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inventory {

    private static final Pattern ITEM_STRING_PATTERN = Pattern.compile(
            "^(?:" +
                    "(\\d+)(?::(-?\\d+))?(?::(\\d+))?(?::([\\w+/=-]+))?" +  // 数字ID格式
                    "|" +
                    "(?:([a-z_]\\w*):)?([a-z._]\\w*)(?::(-?\\d+))?(?::(\\d+))?(?::([\\w+/=-]+))?" +  // 命名空间ID格式
                    ")$",
            Pattern.CASE_INSENSITIVE
    );

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String saveItemToString(Item item) {
        if (NukkitTypeUtils.getNukkitType() == NukkitTypeUtils.NukkitType.NUKKITX) {
            if (item.hasCompoundTag()) {
                return item.getId() + ":" + item.getDamage() + ":" + item.getCount() + ":" + bytesToHexString(item.getCompoundTag());
            } else {
                return item.getId() + ":" + item.getDamage() + ":" + item.getCount() + ":null";
            }
        } else {
            if (item.hasCompoundTag()) {
                return item.getNamespaceId() + ":" + item.getDamage() + ":" + item.getCount() + ":" + bytesToHexString(item.getCompoundTag());
            } else {
                return item.getNamespaceId() + ":" + item.getDamage() + ":" + item.getCount() + ":null";
            }
        }
    }

    // from: GameAPI -> ItemTools
    public static Item getItem(String itemString) {
        if (NukkitTypeUtils.getNukkitType() == NukkitTypeUtils.NukkitType.NUKKITX) {
            return getItemForNukkitX(itemString);
        }
        if (itemString == null || itemString.isEmpty() || itemString.startsWith("0:")) {
            return Item.AIR_ITEM.clone();
        }

        Matcher matcher = ITEM_STRING_PATTERN.matcher(itemString.trim().replace(' ', '_'));
        if (!matcher.matches()) {
            return Item.AIR_ITEM.clone();
        }

        try {
            // 解析数字ID格式
            if (matcher.group(1) != null) {
                int id = Integer.parseInt(matcher.group(1));
                int meta = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                int count = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 1;
                byte[] nbt = hexStringToBytes(matcher.group(4));

                Item item = Item.get(id, meta, count);
                item.setCompoundTag(nbt);
                return item;
            }

            // 解析命名空间ID格式
            String namespace = matcher.group(5);
            String id = matcher.group(6);
            String fullId = (namespace != null) ? namespace + ":" + id : "minecraft:" + id;

            int meta = matcher.group(7) != null ? Integer.parseInt(matcher.group(7)) : 0;
            int count = matcher.group(8) != null ? Integer.parseInt(matcher.group(8)) : 1;
            byte[] nbt = hexStringToBytes(matcher.group(9));

            Item item = Item.fromString(fullId + (meta != 0 ? ":" + meta : ""));
            item.setCount(count);
            item.setCompoundTag(nbt);
            return item;
        } catch (Exception e) {
            return Item.AIR_ITEM.clone(); // 解析失败返回空气
        }
    }

    public static Item getItemForNukkitX(String itemString) {
        try {
            String[] a = itemString.split(":");
            if (a.length != 4) {
                return null;
            }
            Item item = Item.get(Integer.parseInt(a[0]), Integer.parseInt(a[1]), Integer.parseInt(a[2]));
            if (!a[3].equals("null")) {
                CompoundTag tag = Item.parseCompoundTag(hexStringToBytes(a[3]));
                item.setNamedTag(tag);
            }
            return item;
        } catch (Throwable t) {
            return Item.AIR_ITEM.clone();
        }
    }
}
