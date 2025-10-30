package net.p455w0rd.wirelesscraftingterminal.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftingStatus;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiMagnet;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftAmount;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerMagnet;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.helpers.WTCGuiObject;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftingStatus;
import cpw.mods.fml.common.network.IGuiHandler;

public class WCTGuiHandler implements IGuiHandler {

    public void registerRenderers() {}

    @Override
    public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {
        if (guiId == Reference.GUI_MAGNET) {
            return new ContainerMagnet(player, player.inventory);
        }

        final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless()
                .getWirelessTerminalHandler(RandomUtils.getWirelessTerm(player.inventory));
        if (wh == null) {
            return null;
        }

        final WTCGuiObject term = new WTCGuiObject(
                wh,
                RandomUtils.getWirelessTerm(player.inventory),
                player,
                world,
                x,
                y,
                z);

        return switch (guiId) {
            case Reference.GUI_WCT -> updateGui(
                    new ContainerWirelessCraftingTerminal(player.inventory, term),
                    world,
                    x,
                    y,
                    z,
                    ForgeDirection.UNKNOWN,
                    term);
            case Reference.GUI_CRAFTING_STATUS -> updateGui(
                    new ContainerCraftingStatus(player.inventory, term),
                    world,
                    x,
                    y,
                    z,
                    ForgeDirection.UNKNOWN,
                    term);

            case Reference.GUI_CRAFT_AMOUNT -> updateGui(
                    new ContainerCraftAmount(player, term),
                    world,
                    x,
                    y,
                    z,
                    ForgeDirection.UNKNOWN,
                    term);
            case Reference.GUI_CRAFT_CONFIRM -> updateGui(
                    new ContainerCraftConfirm(player, term),
                    world,
                    x,
                    y,
                    z,
                    ForgeDirection.UNKNOWN,
                    term);
            default -> null;
        };
    }

    @Override
    public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {
        final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless()
                .getWirelessTerminalHandler(RandomUtils.getWirelessTerm(player.inventory));
        if (wh != null) {
            final WTCGuiObject obj = new WTCGuiObject(
                    wh,
                    RandomUtils.getWirelessTerm(player.inventory),
                    player,
                    world,
                    x,
                    y,
                    z);
            if (obj != null) {
                final IPortableCell terminal = obj;

                if (guiId == Reference.GUI_WCT) {
                    return new GuiWirelessCraftingTerminal(player.inventory, obj);
                }

                if (guiId == Reference.GUI_CRAFTING_STATUS) {
                    return new GuiCraftingStatus(player.inventory, terminal);
                }

                if (guiId == Reference.GUI_CRAFT_AMOUNT) {
                    return new GuiCraftAmount(player.inventory, terminal);
                }

                if (guiId == Reference.GUI_CRAFT_CONFIRM) {
                    return new GuiCraftConfirm(player.inventory, terminal);
                }
            }
        }
        if (guiId == Reference.GUI_MAGNET) {
            return new GuiMagnet(new ContainerMagnet(player, player.inventory));
        }

        return null;
    }

    public static void launchGui(final int ID, final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        player.openGui(WirelessCraftingTerminal.INSTANCE, ID, world, x, y, z);
    }

    private Object updateGui(final Object newContainer, final World w, final int x, final int y, final int z,
            final ForgeDirection side, final Object myItem) {
        if (newContainer instanceof AEBaseContainer) {
            final AEBaseContainer bc = (AEBaseContainer) newContainer;
            bc.setOpenContext(new ContainerOpenContext(myItem));
            bc.getOpenContext().setWorld(w);
            bc.getOpenContext().setX(x);
            bc.getOpenContext().setY(y);
            bc.getOpenContext().setZ(z);
            bc.getOpenContext().setSide(side);
        }

        return newContainer;
    }
}
