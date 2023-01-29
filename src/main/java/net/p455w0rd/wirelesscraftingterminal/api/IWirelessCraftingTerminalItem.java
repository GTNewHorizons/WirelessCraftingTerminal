package net.p455w0rd.wirelesscraftingterminal.api;

import net.minecraft.item.ItemStack;

import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.features.IAEFeature;
import cofh.api.energy.IEnergyContainerItem;

public interface IWirelessCraftingTerminalItem
        extends IWirelessCraftingTermHandler, IAEItemPowerStorage, IAEFeature, IEnergyContainerItem {

    // checks if an Infinity Booster Caqrd is installed on the WCT
    public boolean checkForBooster(final ItemStack wirelessTerminal);

    // checks if the Wireless Crafting Terminal is enabled on your item (need for ExtraCells Universal Terminal)
    public boolean isWirelessCraftingEnabled(final ItemStack wirelessTerminal);
}
