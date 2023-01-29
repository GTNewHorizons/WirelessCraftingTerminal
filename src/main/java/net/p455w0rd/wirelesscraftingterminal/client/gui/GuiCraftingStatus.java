package net.p455w0rd.wirelesscraftingterminal.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerCraftingStatus;

public class GuiCraftingStatus extends appeng.client.gui.implementations.GuiCraftingStatus {

    private GuiTabButton originalGuiBtn;
    private int originalGui;
    private ItemStack myIcon = null;

    @SuppressWarnings("unused")
    public GuiCraftingStatus(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(inventoryPlayer, te);

        ContainerCraftingStatus status = (ContainerCraftingStatus) this.inventorySlots;
        final Object target = status.getTarget();
        final IDefinitions definitions = AEApi.instance().definitions();
        final IParts parts = definitions.parts();

        if (target instanceof WirelessTerminalGuiObject) {
            for (final ItemStack wirelessTerminalStack : definitions.items().wirelessTerminal().maybeStack(1).asSet()) {
                this.myIcon = wirelessTerminalStack;
            }

            ItemStack is = new ItemStack(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem());
            ((ItemWirelessCraftingTerminal) is.getItem()).injectAEPower(is, 6400001);
            myIcon = is;

            this.originalGui = Reference.GUI_WCT;
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);

        if (btn == this.originalGuiBtn) {
            NetworkHandler.instance.sendToServer(new PacketSwitchGuis(this.originalGui));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();

        if (this.myIcon != null) {
            this.buttonList.add(
                    this.originalGuiBtn = new GuiTabButton(
                            this.guiLeft + 213,
                            this.guiTop - 4,
                            this.myIcon,
                            this.myIcon.getDisplayName(),
                            itemRender));
            this.originalGuiBtn.setHideEdge(13);
        }
    }
}
