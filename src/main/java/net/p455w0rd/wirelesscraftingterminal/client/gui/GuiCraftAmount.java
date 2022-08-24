package net.p455w0rd.wirelesscraftingterminal.client.gui;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.helpers.Reflected;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.GuiTabButton;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class GuiCraftAmount extends appeng.client.gui.implementations.GuiCraftAmount {
    private GuiTabButton originalGuiBtn;

    private ItemStack myIcon;

    private int originalGui;

    @Reflected
    public GuiCraftAmount(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(inventoryPlayer, te);
        ItemStack is = new ItemStack(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem());
        ((ItemWirelessCraftingTerminal) is.getItem()).injectAEPower(is, 6400001);
        myIcon = is;
    }

    @SuppressWarnings({"unchecked", "unused"})
    @Override
    public void initGui() {
        super.initGui();

        final Object target = ((ContainerCraftAmount) this.inventorySlots).getTarget();
        final IDefinitions definitions = AEApi.instance().definitions();
        final IParts parts = definitions.parts();

        if (target instanceof WirelessTerminalGuiObject) {
            this.originalGui = Reference.GUI_WCT;
        }

        if (this.originalGui > 0 && myIcon != null) {
            this.buttonList.add(
                    this.originalGuiBtn = new GuiTabButton(
                            this.guiLeft + 154, this.guiTop - 4, myIcon, myIcon.getDisplayName(), itemRender));
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);

        if (btn == this.originalGuiBtn) {
            NetworkHandler.instance.sendToServer(new PacketSwitchGuis(this.originalGui));
        }
    }
}
