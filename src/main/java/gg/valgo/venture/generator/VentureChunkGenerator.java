package gg.valgo.venture.generator;

import gg.valgo.venture.generator.biome.VentureBiome;
import gg.valgo.venture.generator.biome.VentureBiomes;
import gg.valgo.venture.generator.biome.WeightedVentureBiome;
import gg.valgo.venture.noise.BlazeNoiseGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VentureChunkGenerator extends ChunkGenerator {
    private static final double SLOPE_DX = 0.01;
    private HashMap<ChunkLocation, ChunkData> ungeneratedChunks = new HashMap<>();
    private ChunkData data;
    private ChunkLocation chunkLocation;
    private World generatingWorld;

    private static double heightmap(ArrayList<WeightedVentureBiome> weightedBiomes, double weight, BlazeNoiseGenerator noise, double tx, double tz) {
        double totalWeight = 0;
        double totalValue = 0;

        for (WeightedVentureBiome weightedBiome : weightedBiomes) {
            double biasedWeight = weightedBiome.getBiasedWeight(weight);

            if (biasedWeight > 0.01) {
                totalWeight += biasedWeight;
                totalValue += weightedBiome.getBiome().heightmap(noise, tx, tz) * biasedWeight;
            }
        }

        return totalValue / totalWeight;
    }

    public ChunkData chunkData(World world) {
        return createChunkData(world);
    }

    public void setBlock(int x, int y, int z, Material material, BlockData blockData) {
        ChunkLocation blockLocation = new ChunkLocation(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        int cx = x - blockLocation.getX() * 16;
        int cz = z - blockLocation.getZ() * 16;

        if (blockData == null) {
            blockData = Bukkit.createBlockData(material);
        }

        if (blockLocation.equals(chunkLocation)) {
            data.setBlock(cx, y, cz, material);
            data.setBlock(cx, y, cz, blockData);
        } else {
            boolean chunkLoaded = false;

            try {
                chunkLoaded = generatingWorld.isChunkLoaded(blockLocation.getX() * 16, blockLocation.getZ() * 16);

                if (!chunkLoaded) {
                    chunkLoaded = generatingWorld.isChunkGenerated(blockLocation.getX() * 16, blockLocation.getZ() * 16);
                }
            } catch (Exception e) {}

            if (chunkLoaded) {
                Block block = generatingWorld.getBlockAt(x, y, z);
                block.setType(material, false);
                block.setBlockData(blockData, false);
                generatingWorld.unloadChunkRequest(blockLocation.getX() * 16, blockLocation.getZ() * 16);

                Bukkit.broadcastMessage("Placing block in existing chunk");
            } else {
                ChunkData data;
                if (ungeneratedChunks.containsKey(blockLocation)) {
                    data = ungeneratedChunks.get(blockLocation);
                } else {
                    data = createChunkData(generatingWorld);
                    ungeneratedChunks.put(blockLocation, data);
                }

                data.setBlock(cx, y, cz, material);
                data.setBlock(cx, y, cz, blockData);
            }
        }
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        generatingWorld = world;
        chunkLocation = new ChunkLocation(x, z);

        if (ungeneratedChunks.containsKey(chunkLocation)) {
            data = ungeneratedChunks.get(chunkLocation);
            ungeneratedChunks.remove(chunkLocation);
        } else {
            data = chunkData(world);
        }

        BlazeNoiseGenerator noise = new BlazeNoiseGenerator(world.getSeed());
        ChunkColumnData[] columns = new ChunkColumnData[256];

        for (int cx = 0; cx < 16; cx++) {
            for (int cz = 0; cz < 16; cz++) {
                int tx = x * 16 + cx;
                int tz = z * 16 + cz;

                ArrayList<WeightedVentureBiome> weightedBiomes = VentureBiomes.getWeightedBiomes(noise, tx, tz);
                WeightedVentureBiome topWeightedBiome = weightedBiomes.get(0);
                VentureBiome ventureBiome = topWeightedBiome.getBiome();
                double topWeight = topWeightedBiome.getWeight();

                double heightmap = heightmap(weightedBiomes, topWeight, noise, tx, tz);
                double heightmapX = heightmap(weightedBiomes, topWeight, noise, tx + SLOPE_DX, tz);
                double heightmapZ = heightmap(weightedBiomes, topWeight, noise, tx, tz + SLOPE_DX);
                double slopeX = (heightmapX - heightmap) / SLOPE_DX;
                double slopeZ = (heightmapZ - heightmap) / SLOPE_DX;
                double slope = Math.sqrt(slopeX * slopeX + slopeZ * slopeZ);

                ventureBiome.generateColumn(this, data, noise, world, heightmap, slope, cx, cz, tx, tz);

                for (int y = 0; y < 256; y++) {
                    biome.setBiome(cx, y, cz, ventureBiome.getBiome(tx, y, tz));
                }

                columns[cx + cz * 16] = new ChunkColumnData(ventureBiome, heightmap, slope);
            }
        }

        for (int cx = 0; cx < 16; cx++) {
            for (int cz = 0; cz < 16; cz++) {
                int tx = x * 16 + cx;
                int tz = z * 16 + cz;
                ChunkColumnData columnData = columns[cx + cz * 16];

                VentureBiome ventureBiome = columnData.getBiome();
                double heightmap = columnData.getHeightmap();
                double slope = columnData.getSlope();

                ventureBiome.decorateColumn(this, data, noise, world, heightmap, slope, cx, cz, tx, tz);
            }
        }

        return data;
    }
}