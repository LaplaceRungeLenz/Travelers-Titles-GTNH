package io.github.laplacerungelenz.travelerstitles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = TravelersTitles.ID,
    name = "Traveler's Titles GTNH",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*",
    guiFactory = "io.github.laplacerungelenz.travelerstitles.client.ConfigGuiFactory")
public final class TravelersTitles {

    public static final String ID = "travelerstitlesgtnh";
    public static final Logger LOG = LogManager.getLogger(ID);
    @SidedProxy(
        clientSide = "io.github.laplacerungelenz.travelerstitles.client.ClientProxy",
        serverSide = "io.github.laplacerungelenz.travelerstitles.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init();
    }
}
