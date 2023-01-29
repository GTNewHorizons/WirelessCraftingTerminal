package net.p455w0rd.wirelesscraftingterminal.integration.modules.NEIHelpers;

import java.util.regex.Pattern;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;

import codechicken.nei.api.INEIGuiAdapter;

public class NEIGUIHandler extends INEIGuiAdapter {

    protected Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        if (gui instanceof GuiWirelessCraftingTerminal && draggedStack != null && draggedStack.getItem() != null) {
            GuiWirelessCraftingTerminal gmm = (GuiWirelessCraftingTerminal) gui;
            if (gmm.isOverSearchField(mousex, mousey)) {
                gmm.setSearchString(SPECIAL_REGEX_CHARS.matcher(draggedStack.getDisplayName()).replaceAll("\\\\$0"));
                return true;
            }
        }
        return super.handleDragNDrop(gui, mousex, mousey, draggedStack, button);
    }
}
