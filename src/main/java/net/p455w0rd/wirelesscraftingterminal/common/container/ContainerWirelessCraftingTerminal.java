package net.p455w0rd.wirelesscraftingterminal.common.container;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.client.gui.WCTPrimaryGui;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotArmor;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotBooster;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.container.slot.SlotTrash;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryBooster;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryCrafting;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryTrash;
import net.p455w0rd.wirelesscraftingterminal.common.inventory.WCTInventoryViewCell;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.helpers.WTCGuiObject;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.parts.ICraftingTerminal;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.container.PrimaryGui;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotInaccessible;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.IPrimaryGuiIconProvider;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class ContainerWirelessCraftingTerminal extends ContainerMEMonitorable
        implements IAEAppEngInventory, IContainerCraftingPacket, IViewCellStorage {

    public static final int CRAFTING_SLOT_X_POS = 80;
    public static final int CRAFTING_SLOT_Y_POS = 83;

    private final ItemStack containerstack;
    public final WCTInventoryCrafting craftingGrid;
    public final WCTInventoryViewCell viewCellInventory;
    public final WCTInventoryBooster boosterInventory;
    public final WCTInventoryMagnet magnetInventory;
    public final WCTInventoryTrash trashInventory;
    public final InventoryPlayer inventoryPlayer;
    public ItemStack[] craftMatrixInventory;
    private final EntityPlayer player;

    private final SlotCraftingMatrix[] craftMatrixSlot;
    private final SlotCraftingTerm craftingSlot;
    public SlotTrash trashSlot;

    private final ICraftingTerminal ct;

    private final IWirelessCraftingTerminalItem thisItem;
    private IRecipe currentRecipe;

    /**
     * Constructor for our custom container
     *
     * @author p455w0rd
     */
    public ContainerWirelessCraftingTerminal(InventoryPlayer inventoryPlayer, ICraftingTerminal host) {
        super(inventoryPlayer, host, false);
        final EntityPlayer player = inventoryPlayer.player;
        this.ct = host;

        this.setCustomName("WCTContainer");

        final ItemStack wirelessTerminal = RandomUtils.getWirelessTerm(inventoryPlayer);

        this.boosterInventory = new WCTInventoryBooster(wirelessTerminal);
        this.magnetInventory = new WCTInventoryMagnet(wirelessTerminal);
        this.trashInventory = new WCTInventoryTrash(wirelessTerminal);
        this.containerstack = wirelessTerminal;
        this.thisItem = (IWirelessCraftingTerminalItem) this.containerstack.getItem();
        this.craftingGrid = new WCTInventoryCrafting(this, 3, 3, containerstack);
        this.viewCellInventory = new WCTInventoryViewCell(wirelessTerminal);
        this.inventoryPlayer = inventoryPlayer;
        this.player = player;
        craftMatrixInventory = new ItemStack[9];
        Slot[] hotbarSlot = new Slot[9];
        Slot[] inventorySlot = new Slot[27];
        SlotArmor[] armorSlot = new SlotArmor[4];
        craftMatrixSlot = new SlotCraftingMatrix[9];

        // Dummy slot at index 0 to work around ME slots all having their slot number set to 0.
        this.addSlotToContainer(new SlotInaccessible(new AppEngInternalInventory(null, 1), 0, -1000, -1000));

        if (Reference.WCT_BOOSTER_ENABLED) {
            SlotBooster boosterSlot = new SlotBooster(this.boosterInventory, 0, 134, -20);
            this.addSlotToContainer(boosterSlot);
        } else {
            this.addSlotToContainer(new SlotInaccessible(new AppEngInternalInventory(null, 1), 0, -1000, -1000));
        }

        // Add hotbar slots
        for (int i = 0; i < 9; ++i) {
            if (player.inventory.getStackInSlot(i) != null
                    && player.inventory.getStackInSlot(i).getItem() == this.thisItem) {
                hotbarSlot[i] = new SlotDisabled(this.inventoryPlayer, i, i * 18 + 8, 58);
            } else {
                hotbarSlot[i] = new SlotPlayerHotBar(this.inventoryPlayer, i, i * 18 + 8, 58);
            }
            this.addSlotToContainer(hotbarSlot[i]);
        }

        int k = 0;

        // Add player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (player.inventory.getStackInSlot(j + i * 9 + 9) != null
                        && player.inventory.getStackInSlot(j + i * 9 + 9).getItem() == this.thisItem) {
                    inventorySlot[k] = new SlotDisabled(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, i * 18);
                } else {
                    inventorySlot[k] = new SlotPlayerInv(this.inventoryPlayer, j + i * 9 + 9, j * 18 + 8, i * 18);
                }
                this.addSlotToContainer(inventorySlot[k]);
                k++;
            }
        }

        // Add armor slots
        for (int i = 0; i < 4; ++i) {
            armorSlot[i] = new SlotArmor(
                    this.player,
                    this.inventoryPlayer,
                    this.inventoryPlayer.getSizeInventory() - 1 - i,
                    (int) 8.5,
                    (i * 18) - 76,
                    i);
            this.addSlotToContainer(armorSlot[i]);
        }
        k = 0;
        // Add crafting grid slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                craftMatrixSlot[k] = new SlotCraftingMatrix(
                        this,
                        this.craftingGrid,
                        j + i * 3,
                        80 + j * 18,
                        (i * 18) - 76);
                this.addSlotToContainer(craftMatrixSlot[k]);
                k++;
            }
        }

        AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
        craftingSlot = new SlotCraftingTerm(
                this.getPlayerInv().player,
                this.getActionSource(),
                this.getPowerSource(),
                this.ct,
                this.craftingGrid,
                this.craftingGrid,
                output,
                174,
                -58,
                this);
        // Add crafting result slot
        this.addSlotToContainer(craftingSlot);

        SlotMagnet magnetSlot = new SlotMagnet(this.magnetInventory, 152, -20);
        this.addSlotToContainer(magnetSlot);

        trashSlot = new SlotTrash(this.trashInventory, 80, -20, player);
        trashSlot.setContainer(this);
        this.addSlotToContainer(trashSlot);

        updateCraftingMatrix();

        this.onCraftMatrixChanged(this.craftingGrid);

        for (var slot : this.getCellViewSlots()) {
            slot.xDisplayPosition += 1;
        }

        thisItem.checkForBooster(containerstack);
    }

    private IRecipe findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn) {
        for (Object recipe : CraftingManager.getInstance().getRecipeList()) {
            if (((IRecipe) recipe).matches(craftMatrix, worldIn)) {
                return (IRecipe) recipe;
            }
        }
        return null;
    }

    @Override
    public void onCraftMatrixChanged(final IInventory iinv) {

        final ContainerNull cn = new ContainerNull();
        final InventoryCrafting craftingInv = new InventoryCrafting(cn, 3, 3);

        for (int x = 0; x < 9; x++) {
            craftingInv.setInventorySlotContents(x, craftMatrixSlot[x].getStack());
        }
        if (currentRecipe == null || !currentRecipe.matches(craftingInv, getPlayerInv().player.worldObj)) {
            currentRecipe = findMatchingRecipe(craftingInv, getPlayerInv().player.worldObj);
        }
        if (currentRecipe == null) {
            craftingSlot.putStack(null);
        } else {
            final ItemStack craftingResult = currentRecipe.getCraftingResult(craftingInv);
            craftingSlot.putStack(craftingResult);
        }
        writeToNBT("crafting");
    }

    private void updateCraftingMatrix() {
        if (!this.containerstack.hasTagCompound()) {
            this.containerstack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound stack = this.containerstack.getTagCompound();
        readMatrixNBT(stack);
        for (int i = 0; i < 9; i++) {
            this.craftingGrid.setInventorySlotContents(i, craftMatrixInventory[i]);
        }
    }

    @Override
    public void saveChanges() {
        // :P
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        // <3
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    @Override
    public IInventory getInventoryByName(final String name) {
        if (name.equals("player") || name.equals("container.inventory")) {
            return this.getInventoryPlayer();
        }
        if (name.equals("crafting")) {
            return this.craftingGrid;
        }
        return null;
    }

    @Override
    public IInventory getViewCellStorage() {
        return viewCellInventory;
    }

    public static WTCGuiObject getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x,
            final int y, final int z, final int slotIndex) {
        if (it != null) {
            final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries()
                    .wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return new WTCGuiObject(wh, it, player, w, x, y, z, slotIndex);
            }
        }

        return null;
    }

    public boolean isPowered() {
        double pwr = this.thisItem.getAECurrentPower(this.containerstack);
        return (pwr > 0.0);
    }

    /**
     * Fetches crafting matrix ItemStacks
     *
     * @param nbtTagCompound
     * @author p455w0rd
     */
    private void readMatrixNBT(NBTTagCompound nbtTagCompound) {
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = nbtTagCompound.getTagList("CraftingMatrix", 10);
        craftMatrixInventory = new ItemStack[9];
        for (int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            int slot = tagCompound.getByte("Slot");
            if (slot >= 0 && slot < craftMatrixInventory.length) {
                craftMatrixInventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    /**
     * Initiates external NBT saving methods that store inventory contents
     */
    public void writeToNBT(String which) {
        if (!this.containerstack.hasTagCompound()) {
            this.containerstack.setTagCompound(new NBTTagCompound());
        }
        switch (which) {
            case "booster":
                boosterInventory.writeNBT(this.containerstack.getTagCompound());
                break;
            case "crafting":
                craftingGrid.writeNBT(this.containerstack.getTagCompound());
                break;
            case "viewCell":
                viewCellInventory.writeNBT(this.containerstack.getTagCompound());
                break;
            case "magnet":
                magnetInventory.writeNBT(this.containerstack.getTagCompound());
                break;
            case "trash":
                trashInventory.writeNBT(this.containerstack.getTagCompound());
                break;
            case "all":
            default:
                boosterInventory.writeNBT(this.containerstack.getTagCompound());
                craftingGrid.writeNBT(this.containerstack.getTagCompound());
                viewCellInventory.writeNBT(this.containerstack.getTagCompound());
                magnetInventory.writeNBT(this.containerstack.getTagCompound());
                trashInventory.writeNBT(this.containerstack.getTagCompound());
                break;
        }
    }

    @Override
    @Nonnull
    public List<AppEngSlot> getValidDestinationSlots(boolean isPlayerSideSlot, @Nonnull ItemStack stackInSlot) {
        List<AppEngSlot> list = super.getValidDestinationSlots(isPlayerSideSlot, stackInSlot);
        list.removeIf(slot -> slot instanceof SlotTrash);
        return list;
    }

    @Override
    public ItemStack slotClick(int slot, int button, int flag, EntityPlayer player) {
        try {
            if (slot >= 0 && getSlot(slot) != null
                    && getSlot(slot).getStack() == RandomUtils.getWirelessTerm(player.inventory)) {
                return null;
            }
            return super.slotClick(slot, button, flag, player);
        } catch (IndexOutOfBoundsException e) {
            // When clicking super fast, for some reason, MC tried to access this inv size (max index + 1)
        }
        return null;
    }

    @Override
    public PrimaryGui createPrimaryGui() {
        ContainerOpenContext context = getOpenContext();
        return new WCTPrimaryGui(
                Reference.GUI_WCT,
                ((IPrimaryGuiIconProvider) ct).getPrimaryGuiIcon(),
                context.getTile(),
                context.getSide());
    }
}
