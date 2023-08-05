package sh.talonfox.enhancedweather.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "enhancedweather")
public class EnhancedWeatherConfig implements ConfigData {
    @ConfigEntry.Category("client")
    @Comment("We recommended disabling this if you're using shaders")
    public static boolean Client_ParticleRain = true;
}