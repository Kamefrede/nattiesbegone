package xyz.kamefrede.nattiesbegone;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NattiesBegone.MODID)
public class NattiesBegone {

    public static final String MODID = "nattiesbegone";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public NattiesBegone() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
    }

    @Mod.EventBusSubscriber(modid = NattiesBegone.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EventListeners {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void checkSpawnEvent(LivingSpawnEvent.CheckSpawn event) {
            if (event.getEntityLiving().getType() == EntityType.BAT && Config.darkKnightFalls) {
                event.setResult(Event.Result.DENY);
            }
            EntityClassification classification = event.getEntityLiving().getClassification(false);
            if (!Config.disallowedSpawnRules.containsKey(classification))
                return;
            if (Config.disallowedSpawnRules.get(classification).contains(event.getSpawnReason())) {
                event.setResult(Event.Result.DENY);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void checkSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
            if (event.getEntityLiving().getType() == EntityType.BAT && Config.darkKnightFalls) {
                event.setCanceled(true);
            }
            EntityClassification classification = event.getEntityLiving().getClassification(false);
            if (!Config.disallowedSpawnRules.containsKey(classification))
                return;
            if (Config.disallowedSpawnRules.get(classification).contains(event.getSpawnReason())) {
                event.setCanceled(true);
            }
        }


        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            NattiesBegoneCommand.register(event.getDispatcher());
        }
    }


}
