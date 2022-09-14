package sh.talonfox.enhancedweather.weather;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.BiomeTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import sh.talonfox.enhancedweather.Enhancedweather;
import sh.talonfox.enhancedweather.particles.CloudParticle;
import sh.talonfox.enhancedweather.particles.ParticleRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cloud {
    private Manager HostManager;
    public Vec3d Position = null;
    public int Layer = 0;
    public int Water = 0;
    public int Size = 1;
    public boolean Precipitating = false;
    public boolean Placeholder = false;
    public boolean Expanding = true;
    private int ticks = 0;
    public Random rand;
    @Environment(EnvType.CLIENT)
    public List<Particle> ParticlesCloud = new ArrayList<Particle>();

    public Cloud(Manager manager, Vec3d pos) {
        HostManager = manager;
        Position = pos;
        rand = new Random();
        Size = rand.nextInt(1,300);
    }

    public void tickClient() {
        if (Placeholder)
            return;
        if ((HostManager.getWorld().getTime() % ((Math.max(1, (int)(100F / Size))) + 2)) == 0) {
            Vec3i playerPos = new Vec3i(MinecraftClient.getInstance().player.getX(), Position.y, MinecraftClient.getInstance().player.getZ());
            Vec3i spawnPos = new Vec3i(Position.x + (Math.random() * Size) - (Math.random() * Size), Position.y, Position.z + (Math.random() * Size) - (Math.random() * Size));
            if (ParticlesCloud.size() < Size && playerPos.getManhattanDistance(spawnPos) < 512) {
                CloudParticle newParticle = (CloudParticle)MinecraftClient.getInstance().particleManager.addParticle(ParticleRegister.CLOUD, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), Precipitating ? 0.2F : 0.7F, Precipitating ? 0.2F : 0.7F, Precipitating ? 0.2F : 0.7F);
                newParticle.setVelocity(-Math.sin(Math.toRadians(Enhancedweather.CLIENT_WIND.AngleGlobal)) * Enhancedweather.CLIENT_WIND.SpeedGlobal * 0.1D,0D,Math.cos(Math.toRadians(Enhancedweather.CLIENT_WIND.AngleGlobal)) * Enhancedweather.CLIENT_WIND.SpeedGlobal * 0.1D);
                ParticlesCloud.add(newParticle);
            }
        }
        for (int i = 0; i < ParticlesCloud.size(); i++) {
            if (!ParticlesCloud.get(i).isAlive()) {
                ParticlesCloud.remove(i);
                i -= 1;
            }
        }
    }
    public void tickServer() {
        if(Layer != 0) {
            Size = 300;
        } else if (Size < 300 && Expanding) {
            Size++;
        }
        ticks++;
        if((ticks % 60) == 0 && !Placeholder) {
            boolean waterCollected = false;
            if (rand.nextInt(100) == 0) {
                Water += 10;
                waterCollected = true;
            }
            if(rand.nextInt(15) == 0 && !waterCollected) {
                RegistryEntry<Biome> biome = this.HostManager.getWorld().getBiome(new BlockPos(Position.x, Position.y, Position.z));
                if(biome.isIn(BiomeTags.IS_JUNGLE) || biome.matchesId(new Identifier("minecraft:swamp")) || biome.matchesId(new Identifier("minecraft:mangrove_swamp")) || biome.isIn(BiomeTags.IS_RIVER) || biome.isIn(BiomeTags.IS_OCEAN) || biome.isIn(BiomeTags.IS_DEEP_OCEAN)) {
                    Water += 10;
                    waterCollected = true;
                }
            }
            if (Water > 1000) {
                Water = 1000;
            }
            if (Precipitating) {
                Water = Math.max(0, Water - 3);
                if (Water == 0)
                    Precipitating = false;
            }
            if ((Water >= 100 && rand.nextInt(150) == 0)) {
                Precipitating = true;
            }
        }
    }

    public NbtCompound generateUpdate() {
        NbtCompound data = new NbtCompound();
        data.putDouble("X",Position.getX());
        data.putDouble("Y",Position.getY());
        data.putDouble("Z",Position.getZ());
        data.putInt("Layer",Layer);
        data.putInt("Water",Water);
        data.putInt("Size",Size);
        data.putBoolean("Precipitating",Precipitating);
        data.putBoolean("Placeholder",Placeholder);
        data.putBoolean("Expanding",Expanding);
        return data;
    }

    public void applyUpdate(NbtCompound data) {
        Position = new Vec3d(data.getDouble("X"),data.getDouble("Y"),data.getDouble("Z"));
        Layer = data.getInt("Layer");
        Water = data.getInt("Water");
        Size = data.getInt("Size");
        Precipitating = data.getBoolean("Precipitating");
        Placeholder = data.getBoolean("Placeholder");
        Expanding = data.getBoolean("Expanding");
    }
}
