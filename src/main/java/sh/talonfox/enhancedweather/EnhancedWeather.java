package sh.talonfox.enhancedweather;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.talonfox.enhancedweather.block.BlockRegistry;
import sh.talonfox.enhancedweather.config.ConfigRegistry;
import sh.talonfox.enhancedweather.config.EnhancedWeatherConfig;
import sh.talonfox.enhancedweather.network.UpdateConditions;
import sh.talonfox.enhancedweather.util.FastNoiseLite;

public class EnhancedWeather implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("enhancedweather");
	public static EnhancedWeatherConfig CONFIG;
	public static final DefaultParticleType EW_RAIN = FabricParticleTypes.simple(true);
	public static final DefaultParticleType EW_SNOW = FabricParticleTypes.simple(true);
	public static final DefaultParticleType EW_HAIL = FabricParticleTypes.simple(true);
	public static long noiseTick = 0;
	public static double cloudX = 0;
	public static double cloudZ = 0;

	@Override
	public void onInitialize() {
		ConfigRegistry.init();
		Registry.register(Registries.PARTICLE_TYPE, new Identifier("enhancedweather", "rain"), EW_RAIN);
		Registry.register(Registries.PARTICLE_TYPE, new Identifier("enhancedweather", "snow"), EW_SNOW);
		Registry.register(Registries.PARTICLE_TYPE, new Identifier("enhancedweather", "hail"), EW_HAIL);
		BlockRegistry.register();
		ServerWorldEvents.LOAD.register((server, world) -> {
			WindManager.reset();
			Random r = Random.create();
			cloudX = r.nextBetween(-16777216,16777216);
			cloudZ = r.nextBetween(-16777216,16777216);
			EnhancedWeather.LOGGER.info("Starting cloud position will be: X={},Z={}",cloudX,cloudZ);
		});
		ServerTickEvents.START_WORLD_TICK.register((world) -> {
			if(world.getDimensionKey().equals(DimensionTypes.OVERWORLD)) {
				world.setRainGradient(0F);
				world.setThunderGradient(0F);
				noiseTick += 1;
				WindManager.tick();
				float windX = (float)-Math.sin(Math.toRadians(WindManager.windAngle))*(WindManager.windSpeed/25F);
				float windZ = (float)Math.cos(Math.toRadians(WindManager.windAngle))*(WindManager.windSpeed/25F);
				float moveX = (float)-Math.sin(Math.toRadians(WindManager.windAngle))*Math.min(1.5F,WindManager.windSpeed/25F);
				float moveZ = (float)Math.cos(Math.toRadians(WindManager.windAngle))*Math.min(1.5F,WindManager.windSpeed/25F);
				cloudX += (moveX * 0.002) * 32;
				cloudZ += (moveZ * 0.002) * 32;
				if (noiseTick % 20 == 0) {
					for (ServerPlayerEntity player : PlayerLookup.all(world.getServer())) {
						UpdateConditions.send(world.getServer(), player, windX, windZ, cloudX, cloudZ);
					}
				}
			}
		});
	}
}