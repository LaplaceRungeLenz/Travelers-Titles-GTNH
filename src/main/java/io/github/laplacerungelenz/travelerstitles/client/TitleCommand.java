package io.github.laplacerungelenz.travelerstitles.client;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

import com.google.gson.GsonBuilder;

import io.github.laplacerungelenz.travelerstitles.core.Location;

public final class TitleCommand extends CommandBase {

    private final ClientProxy client;

    public TitleCommand(ClientProxy client) {
        this.client = client;
    }

    @Override
    public String getCommandName() {
        return "ttgtnh";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("travelertitles");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ttgtnh <preview [dimension|biome]|reload|toggle|inspect|dump>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "preview", "reload", "toggle", "inspect", "dump");
        if (args.length == 2 && args[0].equals("preview"))
            return getListOfStringsMatchingLastWord(args, "dimension", "biome");
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            reply(sender, getCommandUsage(sender));
            return;
        }
        switch (args[0]) {
            case "preview":
                String kind = args.length > 1 ? args[1] : "dimension";
                if (!kind.equals("dimension") && !kind.equals("biome")) {
                    reply(sender, getCommandUsage(sender));
                    return;
                }
                client.preview(kind);
                break;
            case "reload":
                client.reload();
                reply(
                    sender,
                    "Title config/resources reloaded. Warnings: " + client.resources.engine()
                        .warnings()
                        .size());
                break;
            case "toggle":
                ClientConfig.config.get("general", "enabled", true)
                    .set(!ClientConfig.enabled);
                client.configure();
                reply(sender, "Titles: " + ClientConfig.enabled);
                break;
            case "inspect":
                Location current = client.current();
                if (current == null) {
                    reply(sender, "Location not ready.");
                    return;
                }
                reply(sender, current.attributes.toString());
                reply(sender, "Dimension rule: " + client.resources.resolve("dimension", current).source);
                reply(sender, "Biome rule: " + client.resources.resolve("biome", current).source);
                break;
            case "dump":
                try {
                    reply(sender, "Saved " + dump().getAbsolutePath());
                } catch (IOException ex) {
                    reply(sender, "Cannot write diagnostics: " + ex.getMessage());
                }
                break;
            default:
                reply(sender, getCommandUsage(sender));
        }
    }

    public File dump() throws IOException {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schemaVersion", 1);
        if (client.current() != null) report.put("current", client.current().attributes);
        List<Map<String, String>> biomes = new ArrayList<>();
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) if (biome != null) {
            Map<String, String> values = LocationResolver.biomeAttributes(biome);
            Location location = new Location("catalog", values.get("biomeId"), values);
            values.put(
                "resolvedTitle",
                TitleResources.name("biome", location, client.resources.resolve("biome", location)));
            biomes.add(values);
        }
        report.put("biomes", biomes);
        report.put("registeredDimensionIds", DimensionManager.getStaticDimensionIDs());
        List<Map<String, String>> bodies = new ArrayList<>();
        for (Map.Entry<String, Object> entry : SpaceCompat.registry()
            .entrySet()) {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("key", entry.getKey());
            for (String method : new String[] { "getName", "getLocalizedName", "getDimensionID", "getReachable",
                "getWorldProvider" }) body.put(method, SpaceCompat.string(SpaceCompat.call(entry.getValue(), method)));
            bodies.add(body);
        }
        report.put("celestialBodies", bodies);
        report.put(
            "warnings",
            client.resources.engine()
                .warnings());
        File output = new File(ClientConfig.directory, "location-catalog.json");
        Files.write(
            output.toPath(),
            new GsonBuilder().setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .toJson(report)
                .getBytes(StandardCharsets.UTF_8));
        return output;
    }

    private static void reply(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText("[TT] " + text));
    }
}
