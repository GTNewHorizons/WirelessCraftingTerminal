package net.p455w0rd.wirelesscraftingterminal.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.networking.security.WCTIActionHost;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.networking.IGridNode;
import appeng.items.contents.WirelessCraftingTerminalGuiObject;

public class WTCGuiObject extends WirelessCraftingTerminalGuiObject implements WCTIActionHost {

    private final int slotIndex;

    public WTCGuiObject(IWirelessTermHandler wh, ItemStack is, EntityPlayer ep, World w, int x, int y, int z,
            final int slotIndex) {
        super(wh, is, ep, w, x, y, z);
        this.slotIndex = slotIndex;
    }

    @Override
    public IGridNode getActionableNode(boolean ignoreRange) {
        IGridNode node = this.getActionableNode();
        if (node != null) return node;
        if (ignoreRange && this.getGrid() != null) {
            return this.getGrid().getPivot();
        }
        return null;
    }

    @Override
    public int getInventorySlot() {
        return this.slotIndex;
    }
}
