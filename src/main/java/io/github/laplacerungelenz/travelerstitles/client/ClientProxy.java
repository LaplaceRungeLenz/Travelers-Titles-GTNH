package io.github.laplacerungelenz.travelerstitles.client;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import io.github.laplacerungelenz.travelerstitles.CommonProxy;
import io.github.laplacerungelenz.travelerstitles.TravelersTitles;
import io.github.laplacerungelenz.travelerstitles.core.Location;
import io.github.laplacerungelenz.travelerstitles.core.TitleController;
import io.github.laplacerungelenz.travelerstitles.core.TitleStyle;

public final class ClientProxy extends CommonProxy {

    public static ClientProxy instance;
    public final TitleResources resources = new TitleResources();
    private final LocationResolver resolver = new LocationResolver();
    private final HudRenderer renderer = new HudRenderer();
    private TitleController controller;
    private TitleDisplay display;
    private Location location;
    private Object previousPlayer;
    private long tick;
    private int generation;
    private String pendingPreview;
    private volatile boolean disconnected;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        ClientConfig.init(event.getModConfigurationDirectory());
        File overrides = new File(ClientConfig.directory, "overrides.json");
        if (!overrides.exists()) try {
            Files.write(
                overrides.toPath(),
                ("{\n  \"schemaVersion\": 1,\n  \"defaults\": {},\n  \"rules\": []\n}\n")
                    .getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            TravelersTitles.LOG.warn("Cannot create example overrides: {}", ex.toString());
        }
        configure();
    }

    @Override
    public void init() {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(this);
        ((IReloadableResourceManager) Minecraft.getMinecraft()
            .getResourceManager()).registerReloadListener(resources);
        ClientCommandHandler.instance.registerCommand(new TitleCommand(this));
    }

    public void configure() {
        ClientConfig.sync();
        controller = new TitleController(
            ClientConfig.stableTicks,
            ClientConfig.cooldownTicks,
            ClientConfig.recentSize,
            ClientConfig.showOnJoin,
            ClientConfig.resetOnDimension);
        if (location != null) controller.suspend(location);
        display = null;
    }

    public void reload() {
        ClientConfig.config.load();
        configure();
        resources.onResourceManagerReload(
            Minecraft.getMinecraft()
                .getResourceManager());
    }

    @SubscribeEvent
    public void changed(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (TravelersTitles.ID.equals(event.modID)) configure();
    }

    @SubscribeEvent
    public void legacyModOptions(GuiOpenEvent event) {
        // Forge 1.7.10's stock in-world Mod Options screen contains only placeholder entries.
        // Leave other mods' replacement screens intact.
        if (event.gui != null && event.gui.getClass() == GuiIngameModOptions.class) {
            event.gui = new GuiModList(Minecraft.getMinecraft().currentScreen);
        }
    }

    @SubscribeEvent
    public void pauseMenu(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiIngameMenu) {
            event.buttonList.add(new PauseConfigButton(8, 8));
        }
    }

    @SubscribeEvent
    public void pauseAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.gui instanceof GuiIngameMenu && event.button instanceof PauseConfigButton) {
            event.setCanceled(true);
            Minecraft.getMinecraft()
                .displayGuiScreen(new TitleConfigScreen(event.gui));
        }
    }

    @SubscribeEvent
    public void disconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        disconnected = true;
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (disconnected) {
            disconnected = false;
            controller.reset();
            location = null;
            previousPlayer = null;
            display = null;
            pendingPreview = null;
        }
        if (mc.theWorld == null || mc.thePlayer == null) {
            display = null;
            location = null;
            return;
        }
        if (mc.isGamePaused()) return;
        tick++;
        if (generation != resources.generation()) {
            generation = resources.generation();
            display = null;
        }
        if (display != null && tick - display.start >= display.style.duration()) display = null;
        if (tick % ClientConfig.sampleTicks != 0 && pendingPreview == null && previousPlayer == mc.thePlayer) return;
        Location sampled = resolver.sample(mc);
        if (sampled == null) {
            // A transfer can replace the world before its first real chunk arrives.
            location = null;
            display = null;
            return;
        }
        boolean respawn = previousPlayer != null && previousPlayer != mc.thePlayer
            && location != null
            && sampled.dimensionKey.equals(location.dimensionKey);
        previousPlayer = mc.thePlayer;
        location = sampled;
        if (!ClientConfig.enabled || hidden(mc)) {
            controller.suspend(location);
            display = null;
            return;
        }
        if (pendingPreview != null) {
            String kind = pendingPreview;
            pendingPreview = null;
            show(kind);
            return;
        }
        if (respawn && ClientConfig.showOnRespawn) {
            show("dimension");
            controller.suspend(location);
            return;
        }
        TitleController.Kind kind = controller.update(location, tick);
        if (kind == TitleController.Kind.DIMENSION) {
            if (ClientConfig.dimensions) show("dimension");
            else if (ClientConfig.biomes) show("biome");
        } else if (kind == TitleController.Kind.BIOME && ClientConfig.biomes
            && (!ClientConfig.onlySurface || "true".equals(location.get("surfaceVisible")))) show("biome");
    }

    public void preview(String kind) {
        pendingPreview = kind;
    }

    public Location current() {
        return location;
    }

    private void show(String kind) {
        TitleStyle style = resources.resolve(kind, location);
        if (!style.enabled || style.duration() == 0) return;
        String title = TitleResources.name(kind, location, style);
        String subtitle = TitleResources.subtitle(kind, location, style, resources.engine());
        if (title.equals(subtitle)) subtitle = "";
        display = new TitleDisplay(style, title, subtitle, tick, resources);
        if (kind.equals("dimension")) controller.blockUntil(tick + style.duration());
        if (ClientConfig.sounds && !style.sound.isEmpty() && style.volume * ClientConfig.volume > 0) {
            Minecraft.getMinecraft()
                .getSoundHandler()
                .playSound(new TitleSound(style.sound, style.volume * ClientConfig.volume, style.pitch));
        }
    }

    private static boolean hidden(Minecraft mc) {
        return mc.gameSettings.hideGUI || (ClientConfig.hideInDebug && mc.gameSettings.showDebugInfo)
            || (ClientConfig.hideInGui && mc.currentScreen != null);
    }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL && !hidden(Minecraft.getMinecraft())
            && ClientConfig.enabled) renderer.render(display, tick, event.partialTicks);
    }
}
