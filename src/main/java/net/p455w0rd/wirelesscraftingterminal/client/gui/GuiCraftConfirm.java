package net.p455w0rd.wirelesscraftingterminal.client.gui;

import appeng.api.storage.ITerminalHost;
import net.minecraft.entity.player.InventoryPlayer;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class GuiCraftConfirm extends appeng.client.gui.implementations.GuiCraftConfirm {
    public GuiCraftConfirm(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(inventoryPlayer, te);
    }

    @Override
    public void switchToOriginalGUI() {
        NetworkHandler.instance.sendToServer(new PacketSwitchGuis(Reference.GUI_WCT));
    }
}
