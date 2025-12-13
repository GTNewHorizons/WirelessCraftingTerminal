package net.p455w0rd.wirelesscraftingterminal.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.GuiTrashButton;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotTrash;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketEmptyTrash;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketInventoryAction;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.parts.ICraftingTerminal;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMEPinSlot;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.helpers.InventoryAction;
import cpw.mods.fml.common.Optional;
import yalter.mousetweaks.api.IMTModGuiContainer;

@Optional.Interface(modid = "MouseTweaks", iface = "yalter.mousetweaks.api.IMTModGuiContainer")
public class GuiWirelessCraftingTerminal extends GuiMEMonitorable implements IMTModGuiContainer {

    private float xSize_lo;
    private float ySize_lo;

    final ResourceLocation BackgroundTexture = new ResourceLocation(
            Reference.MODID,
            "textures/gui/crafting_viewcell.png");
    private final ContainerWirelessCraftingTerminal containerWCT;
    public static int craftingGridOffsetX = 80;
    public static int craftingGridOffsetY;

    private GuiImgButton clearBtn;
    private GuiTrashButton trashBtn;

    private int currentMouseX = 0;
    private int currentMouseY = 0;

    public GuiWirelessCraftingTerminal(final InventoryPlayer inventoryPlayer, ICraftingTerminal host) {
        super(inventoryPlayer, host, new ContainerWirelessCraftingTerminal(inventoryPlayer, host));
        this.setReservedSpace(73);
        this.offsetRepoX -= 1;

        this.containerWCT = (ContainerWirelessCraftingTerminal) this.inventorySlots;
        ((ContainerWirelessCraftingTerminal) this.inventorySlots).setGui(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (this.clearBtn == btn) {
            Slot s = null;
            final Container c = this.inventorySlots;
            for (final Object j : c.inventorySlots) {
                if (j instanceof SlotCraftingMatrix) {
                    s = (Slot) j;
                }
            }

            if (s != null) {
                final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, s.slotNumber, 0);
                NetworkHandler.instance.sendToServer(p);
            }
        } else if (this.trashBtn == btn) {
            Slot s = null;
            final Container c = this.inventorySlots;
            for (final Object j : c.inventorySlots) {
                if (j instanceof SlotTrash) {
                    s = (Slot) j;
                }
            }

            if (s != null) {
                if (s.getHasStack()) {
                    containerWCT.trashSlot.clearStack();
                    NetworkHandler.instance.sendToServer(new PacketEmptyTrash());
                }
            }
        } else {
            super.actionPerformed(btn);
        }
    }

    /**
     * Initializes the GUI
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(
                this.clearBtn = new GuiImgButton(
                        this.guiLeft + 134,
                        this.guiTop + this.ySize - 160,
                        Settings.ACTIONS,
                        ActionItems.STASH));
        this.buttonList.add(this.trashBtn = new GuiTrashButton(this.guiLeft + 98, this.guiTop + this.ySize - 104));
        this.clearBtn.setHalfSize(true);

        craftingGridOffsetX = Integer.MAX_VALUE;
        craftingGridOffsetY = Integer.MAX_VALUE;

        for (final Object s : this.inventorySlots.inventorySlots) {
            if (s instanceof AppEngSlot) {
                if (((Slot) s).xDisplayPosition < 197) {
                    this.repositionSlot((AppEngSlot) s);
                }
            }

            if (s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix) {
                final Slot g = (Slot) s;
                if (g.xDisplayPosition > 0 && g.yDisplayPosition > 0) {
                    craftingGridOffsetX = Math.min(craftingGridOffsetX, g.xDisplayPosition);
                    craftingGridOffsetY = Math.min(craftingGridOffsetY, g.yDisplayPosition);
                }
            }
        }
    }

    @Override
    protected int getPinButtonY() {
        return super.getPinButtonY() + 31;
    }

    /**
     * Draws the screen and tooltips.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        this.xSize_lo = (float) mouseX;
        this.ySize_lo = (float) mouseY;
    }

    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {

        String s = LocaleHandler.WirelessTermLabel.getLocal();
        this.mc.fontRenderer.drawString(s, 7, 5, 4210752);
        this.mc.fontRenderer.drawString(I18n.format("container.inventory"), 7, this.ySize - 172 + 3, 4210752);

        VirtualMEPinSlot.drawSlotsBackground(this.pinSlots, this.mc, this.zLevel);

        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;
    }

    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        // draw "over inventory area"
        this.mc.getTextureManager().bindTexture(BackgroundTexture);
        final int x_width = 199;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

        for (int x = 0; x < this.rows; x++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }

        // draw player inv
        this.drawTexturedModalRect(
                offsetX,
                offsetY + 16 + this.rows * 18,
                0,
                106 - 18 - 18,
                x_width,
                99 + this.getReservedSpace());

        // draw view cells background
        this.drawTexturedModalRect(offsetX + x_width, offsetY, x_width, 0, 32, 104);

        if (Reference.WCT_BOOSTER_ENABLED) {
            this.drawTexturedModalRect(this.guiLeft + 132, (this.guiTop + this.rows * 18) + 83, 237, 237, 19, 19);
        }
        GuiInventory.func_147046_a(
                this.guiLeft + 51,
                (this.guiTop + this.rows * 18) + 94,
                32,
                (float) (this.guiLeft + 51) - this.xSize_lo,
                (float) ((this.guiTop + this.rows * 18) + 50) - this.ySize_lo,
                this.mc.thePlayer);

        this.searchField.drawTextBox();

        this.updateViewCells();
    }

    // MouseTweaks compat

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public int getAPIVersion() {
        return 1;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public String getModName() {
        return Reference.NAME;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public boolean isMouseTweaksDisabled() {
        return true;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public boolean isWheelTweakDisabled() {
        return true;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public boolean isCraftingOutputSlot(Object modContainer, Object slot) {
        return slot == containerWCT.getSlot(ContainerWirelessCraftingTerminal.CRAFT_RESULT);
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public Object getModContainer() {
        return containerWCT;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public int getModSlotCount(Object modContainer) {
        return containerWCT.inventorySlots.size();
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public Object getModSlot(Object modContainer, int slotNumber) {
        return slotNumber < containerWCT.inventorySlots.size() ? containerWCT.inventorySlots.get(slotNumber) : null;
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public Object getModSelectedSlot(Object modContainer, int slotCount) {
        return getSlot(currentMouseX, currentMouseY);
    }

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public void clickModSlot(Object modContainer, Object slotO, int mouseButton, boolean shiftPressed) {}

    @Override
    @Optional.Method(modid = "MouseTweaks")
    public void disableRMBDragIfRequired(Object modContainer, Object firstSlot, boolean shouldClick) {}
}
