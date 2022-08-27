package net.p455w0rd.wirelesscraftingterminal.common.container;

import appeng.api.implementations.guiobjects.IPortableCell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

public class ContainerCraftAmount extends appeng.container.implementations.ContainerCraftAmount {
    public ContainerCraftAmount(EntityPlayer player, IPortableCell terminal) {
        super(player.inventory, terminal);
    }

    @Override
    public void openConfirmationGUI(EntityPlayer player, TileEntity te) {
        int x = (int) player.posX;
        int y = (int) player.posY;
        int z = (int) player.posZ;
        WCTGuiHandler.launchGui(Reference.GUI_CRAFT_CONFIRM, player, player.worldObj, x, y, z);
    }
}
