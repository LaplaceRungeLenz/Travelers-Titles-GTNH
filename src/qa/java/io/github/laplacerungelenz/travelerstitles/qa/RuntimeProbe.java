package io.github.laplacerungelenz.travelerstitles.qa;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;

import com.google.gson.GsonBuilder;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.github.laplacerungelenz.travelerstitles.client.ClientProxy;
import io.github.laplacerungelenz.travelerstitles.client.SpaceCompat;
import io.github.laplacerungelenz.travelerstitles.client.TitleCommand;
import io.github.laplacerungelenz.travelerstitles.client.TitleResources;
import io.github.laplacerungelenz.travelerstitles.core.Location;

/** Test-only automated visit probe. Requires an explicit marker in an isolated game directory. */
@Mod(
    modid = "ttgtnh_runtime_probe",
    name = "TTGTNH Runtime Probe",
    version = "1",
    dependencies = "required-after:travelerstitlesgtnh")
public final class RuntimeProbe {

    private final List<Integer> targets = new ArrayList<>();
    private final List<Map<String, Object>> results = new ArrayList<>();
    private final Map<String, Object> resourceChecks = new LinkedHashMap<>();
    private boolean enabled, launched, catalogued, finished;
    private int age, stage = -1, stageAge, readyAge;
    private volatile Integer requested;
    private volatile String transferError;
    private boolean screenshot;
    private File output;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        enabled = new File(mc.mcDataDir, ".ttgtnh-qa").isFile();
        if (!enabled) return;
        output = new File(mc.mcDataDir, "ttgtnh-qa-results.json");
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SubscribeEvent
    public void client(TickEvent.ClientTickEvent event) {
        if (!enabled || event.phase != TickEvent.Phase.END || finished) return;
        Minecraft mc = Minecraft.getMinecraft();
        age++;
        if (!launched && age > 60) {
            launched = true;
            mc.gameSettings.pauseOnLostFocus = false;
            mc.launchIntegratedServer(
                "TTGTNH-QA",
                "Traveler Titles QA",
                new WorldSettings(7192026L, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT)
                    .enableCommands());
            return;
        }
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!catalogued && age > 160) {
            try {
                new TitleCommand(ClientProxy.instance).dump();
                resourceChecks();
                targets.add(0);
                for (Map.Entry<String, Object> entry : SpaceCompat.registry()
                    .entrySet()) {
                    String key = entry.getKey()
                        .toLowerCase(java.util.Locale.ROOT);
                    if (!(key.equals("moon.moon") || key.equals("planet.mars")
                        || key.equals("planet.asteroids")
                        || key.equals("planet.venus")
                        || key.equals("moon.iojupiter")
                        || key.equals("planet.barnarda2")
                        || key.equals("planet.ross128b")
                        || key.equals("moon.ross128ba")
                        || key.equals("planet.anubis"))) continue;
                    Object dim = SpaceCompat.call(entry.getValue(), "getDimensionID");
                    if (dim instanceof Integer && DimensionManager.isDimensionRegistered((Integer) dim)
                        && !targets.contains(dim)) targets.add((Integer) dim);
                }
                // Ordinary registered dimensions are identified by the provider, not modpack numeric constants.
                for (Integer id : DimensionManager.getStaticDimensionIDs()) {
                    try {
                        String provider = DimensionManager.createProviderFor(id)
                            .getClass()
                            .getName();
                        if (provider.startsWith("twilightforest.") && !targets.contains(id)) targets.add(id);
                    } catch (RuntimeException ignored) {}
                }
                targets.add(-1);
                targets.add(1);
                targets.add(0);
                catalogued = true;
                next();
            } catch (Exception ex) {
                recordFailure("catalogue", ex);
                finished = true;
            }
            return;
        }
        if (!catalogued || stage < 0) return;
        stageAge++;
        if (transferError != null) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("dimensionId", targets.get(stage));
            result.put("status", "transfer failed");
            result.put("error", transferError);
            results.add(result);
            save();
            next();
            return;
        }
        Location ready = ClientProxy.instance.current();
        if (requested != null || mc.thePlayer.dimension != targets.get(stage)
            || ready == null
            || !Integer.toString(targets.get(stage))
                .equals(ready.get("dimensionId"))) {
            readyAge = 0;
            if (stageAge > 2400) {
                recordFailure("visit-timeout", new IllegalStateException("dimension " + targets.get(stage)));
                next();
            }
            return;
        }
        readyAge++;
        if (mc.currentScreen != null) mc.displayGuiScreen(null);
        if (readyAge == 100) ClientProxy.instance.preview("dimension");
        if (readyAge == 130) screenshot = true;
        if (readyAge >= 170) {
            Location location = ClientProxy.instance.current();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("dimensionId", targets.get(stage));
            result.put("status", location == null ? "location missing" : "visited");
            if (location != null) {
                result.put("attributes", location.attributes);
                result.put(
                    "title",
                    TitleResources
                        .name("dimension", location, ClientProxy.instance.resources.resolve("dimension", location)));
                result.put(
                    "biomeTitle",
                    TitleResources.name("biome", location, ClientProxy.instance.resources.resolve("biome", location)));
            }
            results.add(result);
            save();
            next();
        }
    }

    private void next() {
        stage++;
        stageAge = 0;
        readyAge = 0;
        transferError = null;
        if (stage >= targets.size()) {
            finished = true;
            save();
            return;
        }
        requested = targets.get(stage);
    }

    @SubscribeEvent
    public void server(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || requested == null) return;
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager().playerEntityList.isEmpty()) return;
        EntityPlayerMP player = (EntityPlayerMP) server.getConfigurationManager().playerEntityList.get(0);
        int target = requested;
        try {
            if (player.dimension != target) {
                int previousDimension = player.dimension;
                WorldServer destination = server.worldServerForDimension(target);
                // Forge transfer is sufficient for provider/biome HUD verification. No progression or travel gameplay
                // is tested.
                server.getConfigurationManager()
                    .transferPlayerToDimension(player, target, new Teleporter(destination) {

                        @Override
                        public void placeInPortal(Entity entity, double x, double y, double z, float yaw) {
                            entity.setLocationAndAngles(0.5, 120, 0.5, 0, 0);
                        }
                    });
                // Vanilla's generic transfer omits entity placement when leaving the End;
                // ordinary gameplay uses the respawn/credits path instead. This probe needs
                // the player ticking in the destination so its chunk queue is sent.
                if (previousDimension == 1) {
                    destination.spawnEntityInWorld(player);
                    destination.updateEntityWithOptionalForce(player, false);
                }
            }
            player.mountEntity(null);
            player.capabilities.allowFlying = true;
            player.capabilities.isFlying = true;
            player.sendPlayerAbilities();
            player.setPositionAndUpdate(0.5, 120, 0.5);
            player.worldObj.getGameRules()
                .setOrCreateGameRule("doMobSpawning", "false");
            player.worldObj.getGameRules()
                .setOrCreateGameRule("doDaylightCycle", "false");
            player.worldObj.setWorldTime(6000);
        } catch (Exception ex) {
            transferError = ex.toString();
        } finally {
            requested = null;
        }
    }

    @SubscribeEvent
    public void render(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !screenshot) return;
        screenshot = false;
        Minecraft mc = Minecraft.getMinecraft();
        ScreenShotHelper.saveScreenshot(
            mc.mcDataDir,
            "ttgtnh-" + stage + "-dim-" + targets.get(stage) + ".png",
            mc.displayWidth,
            mc.displayHeight,
            mc.getFramebuffer());
    }

    private void recordFailure(String action, Exception ex) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("action", action);
        result.put("error", ex.toString());
        results.add(result);
        save();
    }

    private void save() {
        try {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("finished", finished);
            report.put("targets", targets);
            report.put("results", results);
            report.put("resourceChecks", resourceChecks);
            Files.write(
                output.toPath(),
                new GsonBuilder().setPrettyPrinting()
                    .create()
                    .toJson(report)
                    .getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void resourceChecks() throws Exception {
        ClientProxy client = ClientProxy.instance;
        resourceChecks.put(
            "missingTextureFallback",
            !client.resources.textureAvailable("travelerstitlesgtnh:textures/missing.png"));
        resourceChecks
            .put("invalidImageFallback", !client.resources.textureAvailable("travelerstitlesgtnh:titles/index.json"));
        resourceChecks.put(
            "examplePngDecoded",
            client.resources.textureAvailable("travelerstitlesgtnh:textures/titles/moon.png"));
        File local = new File(
            io.github.laplacerungelenz.travelerstitles.client.ClientConfig.directory,
            "overrides.json");
        byte[] original = Files.readAllBytes(local.toPath());
        try {
            Files.write(local.toPath(), "{invalid".getBytes(StandardCharsets.UTF_8));
            client.reload();
            resourceChecks.put(
                "invalidJsonWarned",
                !client.resources.engine()
                    .warnings()
                    .isEmpty());
            Files.write(
                local.toPath(),
                ("{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"title\":\"QA 热重载\"}}}")
                    .getBytes(StandardCharsets.UTF_8));
            client.reload();
            Location sample = new io.github.laplacerungelenz.travelerstitles.client.LocationResolver()
                .sample(Minecraft.getMinecraft());
            resourceChecks.put(
                "localOverrideReload",
                sample != null && "QA 热重载"
                    .equals(TitleResources.name("dimension", sample, client.resources.resolve("dimension", sample))));
        } finally {
            Files.write(local.toPath(), original);
            client.reload();
        }
        int generation = client.resources.generation();
        Minecraft.getMinecraft()
            .refreshResources();
        resourceChecks.put("fullResourceReload", client.resources.generation() > generation);
        resourceChecks
            .put("pngAfterReload", client.resources.textureAvailable("travelerstitlesgtnh:textures/titles/moon.png"));
        resourceChecks.put(
            "soundRegistered",
            Minecraft.getMinecraft()
                .getSoundHandler()
                .getSound(new net.minecraft.util.ResourceLocation("travelerstitlesgtnh:arrival")) != null);
        File png = new File(
            Minecraft.getMinecraft().mcDataDir,
            "resourcepacks/TTGTNH-QA-resources/assets/travelerstitlesgtnh/textures/titles/moon.png");
        if (png.isFile()) {
            byte[] valid = Files.readAllBytes(png.toPath());
            try {
                Files.write(png.toPath(), "damaged after first load".getBytes(StandardCharsets.UTF_8));
                Minecraft.getMinecraft()
                    .refreshResources();
                resourceChecks.put(
                    "damagedImageReloadFallback",
                    !client.resources.textureAvailable("travelerstitlesgtnh:textures/titles/moon.png"));
            } finally {
                Files.write(png.toPath(), valid);
                Minecraft.getMinecraft()
                    .refreshResources();
            }
            resourceChecks.put(
                "repairedImageReload",
                client.resources.textureAvailable("travelerstitlesgtnh:textures/titles/moon.png"));
        }
    }
}
