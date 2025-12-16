package net.p455w0rd.wirelesscraftingterminal.common;

import java.io.File;

import net.minecraft.creativetab.CreativeTabs;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.creativetab.CreativeTabWCT;
import net.p455w0rd.wirelesscraftingterminal.handlers.AchievementHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.ConfigHandler;
import net.p455w0rd.wirelesscraftingterminal.handlers.RecipeHandler;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationRegistry;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.proxy.CommonProxy;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(
        modid = Reference.MODID,
        acceptedMinecraftVersions = "[1.7.10]",
        name = Reference.NAME,
        version = Reference.VERSION,
        guiFactory = Reference.GUI_FACTORY,
        dependencies = "" + "required-after:Forge@["
                + net.minecraftforge.common.ForgeVersion.majorVersion
                + '.' // majorVersion
                + net.minecraftforge.common.ForgeVersion.minorVersion
                + '.' // minorVersion
                + net.minecraftforge.common.ForgeVersion.revisionVersion
                + '.' // revisionVersion
                + net.minecraftforge.common.ForgeVersion.buildVersion
                + ",);"
                + "required-after:"
                + "appliedenergistics2@[rv3-beta-754,);after:"
                + "NotEnoughItems;")
public class WirelessCraftingTerminal {

    private static LoaderState WCTState = LoaderState.NOINIT;
    public static CreativeTabs creativeTab;

    @Instance(Reference.MODID)
    public static WirelessCraftingTerminal INSTANCE;

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        WirelessCraftingTerminal.WCTState = LoaderState.PREINITIALIZATION;
        WirelessCraftingTerminal.INSTANCE = this;
        creativeTab = new CreativeTabWCT(CreativeTabs.getNextID(), Reference.MODID).setNoScrollbar();
        WirelessCraftingTerminal.proxy.registerItems();
        ConfigHandler.init(new File(event.getModConfigurationDirectory(), Reference.CONFIG_FILE));
        FMLCommonHandler.instance().bus().register(proxy);
        AEApi.instance().registries().wireless()
                .registerWirelessHandler((IWirelessTermHandler) ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        IntegrationRegistry.INSTANCE.init();
        WirelessCraftingTerminal.WCTState = LoaderState.INITIALIZATION;
        if (!Loader.isModLoaded("dreamcraft")) {
            RecipeHandler.loadRecipes(!Reference.WCT_MINETWEAKER_OVERRIDE);
        }
        AchievementHandler.init();
    }

    @EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        IntegrationRegistry.INSTANCE.postInit();
        NetworkRegistry.INSTANCE.registerGuiHandler(WirelessCraftingTerminal.INSTANCE, new WCTGuiHandler());
        NetworkHandler.instance = new NetworkHandler("WCT");
        WirelessCraftingTerminal.proxy.removeItemsFromNEI();
    }

    public static LoaderState getLoaderState() {
        return WirelessCraftingTerminal.WCTState;
    }
}
