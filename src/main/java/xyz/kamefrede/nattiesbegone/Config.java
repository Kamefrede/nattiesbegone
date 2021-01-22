package xyz.kamefrede.nattiesbegone;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.SpawnReason;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = NattiesBegone.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
	public static final Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;
	public static final Type MAPTYPE = new TypeToken<HashMap<String, HashSet<SpawnReason>>>() {
	}.getType();
	public static HashMap<EntityClassification, HashSet<SpawnReason>> disallowedSpawnRules;
	public static boolean darkKnightFalls;

	static {
		final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	public static void cacheConfig() {
		darkKnightFalls = SERVER.darkKnightFalls.get();
		try {
			Gson gson = new Gson();
			HashMap<String, HashSet<SpawnReason>> jsonMap = gson.fromJson(SERVER.disallowedSpawnRules.get(), MAPTYPE);
			Config.disallowedSpawnRules = new HashMap<>(jsonMap.entrySet().stream().map(obj -> {
				EntityClassification classification = EntityClassification.getClassificationByName(obj.getKey());
				if (classification == null) {
					throw new JsonSyntaxException("Unable to parse key:" + obj.getKey());
				}
				return Pair.of(classification, obj.getValue());
			}).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
		} catch (JsonSyntaxException e) {
			NattiesBegone.LOGGER.error("Well, this is awkward.");
			NattiesBegone.LOGGER.error("Failed to serialize hashmap from json in the config file.");
			throw e;
		}
	}

	public static void updateConfig() {
		SERVER.darkKnightFalls.set(darkKnightFalls);
		Gson gson = new Gson();
		HashMap<String, HashSet<SpawnReason>> jsonMap = new HashMap<>(disallowedSpawnRules.entrySet().stream().map(obj -> Pair.of(obj.getKey().getName(), obj.getValue())).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
		String json = gson.toJson(jsonMap, MAPTYPE);
		SERVER.disallowedSpawnRules.set(json);
	}

	@SubscribeEvent
	public static void configEvent(ModConfig.ModConfigEvent configEvent) {
		if (configEvent.getConfig().getSpec() == Config.SERVER_SPEC) {
			cacheConfig();
		}
	}

	public static class Server {
		public final ForgeConfigSpec.ConfigValue<String> disallowedSpawnRules;
		public final ForgeConfigSpec.BooleanValue darkKnightFalls;
		Gson gson = new Gson();
		String defaultmap = gson.toJson(new HashMap<>(), MAPTYPE);

		public Server(ForgeConfigSpec.Builder builder) {
			disallowedSpawnRules = builder.comment("Controls what types of spawn reasons are disallowed")
					.comment("Please don't set this manually. Rely on the ingame /nettiesbegone command.")
					.define("common.disallowedSpawnReasons", defaultmap);
			darkKnightFalls = builder.comment("Gets rid of bat spawning when set to true.")
					.define("common.darkKnightFalls", false);
		}
	}
}
