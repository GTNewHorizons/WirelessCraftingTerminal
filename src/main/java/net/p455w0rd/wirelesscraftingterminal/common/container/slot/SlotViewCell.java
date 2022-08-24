package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import appeng.container.slot.SlotRestrictedInput;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

public class SlotViewCell extends SlotRestrictedInput {

    public SlotViewCell(IInventory inv, int index, int xPos, int yPos, InventoryPlayer playerInv) {
        super(PlacableItemType.VIEW_CELL, inv, index, xPos, yPos, playerInv);
    }
}
