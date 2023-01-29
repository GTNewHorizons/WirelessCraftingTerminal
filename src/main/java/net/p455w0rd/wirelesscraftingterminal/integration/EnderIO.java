package net.p455w0rd.wirelesscraftingterminal.integration;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import crazypants.enderio.enchantment.EnchantmentSoulBound;
import crazypants.enderio.enchantment.Enchantments;

/**
 * @author p455w0rd
 *
 */
public class EnderIO {

    private static Field soulBound;
    private static Field id;

    static {
        try {
            soulBound = Enchantments.class.getDeclaredField("soulBound");
            soulBound.setAccessible(true);

            id = EnchantmentSoulBound.class.getDeclaredField("id");
            id.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static EnchantmentSoulBound getEnchantmentSoulBound(Enchantments enchantments)
            throws IllegalAccessException {
        return (EnchantmentSoulBound) soulBound.get(enchantments);
    }

    private static int getEnchantmentId(EnchantmentSoulBound enchantmentSoulBound) throws IllegalAccessException {
        return id.getInt(enchantmentSoulBound);
    }

    private static Boolean isEnderIOLoaded = null;

    public static boolean isLoaded() {
        if (isEnderIOLoaded == null) {
            isEnderIOLoaded = Loader.isModLoaded("EnderIO");
        }
        return isEnderIOLoaded;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isSoulBound(ItemStack stack) {
        if (isLoaded() && crazypants.enderio.config.Config.enchantmentSoulBoundEnabled) {
            int soulboundId;
            try {
                soulboundId = getEnchantmentId(getEnchantmentSoulBound(Enchantments.getInstance()));
            } catch (IllegalAccessException e) {
                return false;
            }

            Map<Short, Short> enchants = EnchantmentHelper.getEnchantments(stack);
            if (enchants != null) {
                Iterator i = enchants.keySet().iterator();
                while (i.hasNext()) {
                    int enchId = (int) i.next();
                    if (soulboundId != enchId) {
                        continue;
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
