package gg.valgo.venture.generator.biome;

import gg.valgo.venture.generator.VentureChunkGenerator;
import gg.valgo.venture.noise.BlazeNoiseGenerator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;

public abstract class VentureBiome {
    public void setBlock(VentureChunkGenerator chunkGenerator, int x, int y, int z, Material material, BlockData blockData) {
        chunkGenerator.setBlock(x, y, z, material, blockData);
    }

    public void setBlock(VentureChunkGenerator chunkGenerator, int x, int y, int z, Material material) {
        setBlock(chunkGenerator, x, y, z, material, null);
    }

    public abstract double heightmap(BlazeNoiseGenerator noise, double x, double z);

    public abstract void generateColumn(VentureChunkGenerator generator, ChunkGenerator.ChunkData chunkData, BlazeNoiseGenerator noise, World world, double heightmap, double slope, int cx, int cz, int tx, int tz);
    public abstract void decorateColumn(VentureChunkGenerator generator, ChunkGenerator.ChunkData chunkData, BlazeNoiseGenerator noise, World world, double heightmap, double slope, int cx, int cz, int tx, int tz);

    public abstract Biome getBiome(int x, int y, int z);
}