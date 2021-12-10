package net.p455w0rd.wirelesscraftingterminal.common.container.slot;

import appeng.api.AEApi;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class SlotViewCell extends AppEngSlot {

	public SlotViewCell(IInventory inv, int index, int xPos, int yPos) {
		super(inv, index, xPos, yPos);
	}

	@Override
	public boolean isItemValid(ItemStack is) {
		if( is == null || is.getItem() == null) {
			return false;
		}
		return AEApi.instance().definitions().items().viewCell().isSameAs( is );
	}
}
