package net.p455w0rd.wirelesscraftingterminal.integration.modules.NEIHelpers;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import codechicken.nei.api.INEIGuiAdapter;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;

public class NEIGUIHandler extends INEIGuiAdapter
{
    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button)
    {
        if (gui instanceof GuiWirelessCraftingTerminal && draggedStack != null && draggedStack.getItem() != null)
        {
            GuiWirelessCraftingTerminal gmm = (GuiWirelessCraftingTerminal)gui;
            if (gmm.isOverSearchField(mousex, mousey))
            {
                gmm.setSearchString(draggedStack.getDisplayName());
                return true;
            }
        }
        return super.handleDragNDrop(gui, mousex, mousey, draggedStack, button);
    }
}
