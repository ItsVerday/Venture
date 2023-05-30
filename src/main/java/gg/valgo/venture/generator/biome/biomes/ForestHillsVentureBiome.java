package gg.valgo.venture.generator.biome.biomes;

import gg.valgo.venture.generator.VentureChunkGenerator;
import gg.valgo.venture.generator.biome.VentureBiome;
import gg.valgo.venture.noise.BlazeNoiseGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.generator.ChunkGenerator;

public class ForestHillsVentureBiome extends VentureBiome {
    public double heightmap(BlazeNoiseGenerator noise, double x, double z) {
        double longRangeNoise = noise.noise(x / 128, -5, z / 128) * 8 + 8;
        double mediumRangeNoise = noise.noise(x / 32, -10, z / 32) * 6 + 6;

        double ridgeFilter = Math.max(noise.noise(x / 64, -15, z / 64), 0);
        ridgeFilter = ridgeFilter * ridgeFilter * (3 - 2 * ridgeFilter);
        double ridgeHeight = noise.noise(x / 40, -20, z / 40) * 16 + 32;
        double ridgeValue = noise.noise(x / 20, -25, z / 20) * 0.5 + 0.5;
        if (ridgeValue < 2d / 3) {
            ridgeValue *= ridgeValue * 2.25;
        } else {
            ridgeValue -= 1;
            ridgeValue *= ridgeValue;
            ridgeValue += 8d / 9;
        }

        double ridges = ridgeValue * ridgeHeight * ridgeFilter;

        double upEdges = 1 - Math.abs(noise.noise(x / 32,-30, z / 32));
        upEdges = Math.max(upEdges * upEdges - 0.5, 0);
        double upEdgesHeight = noise.noise(x / 48, -35, z / 48) * 2 + 3;
        upEdges *= upEdgesHeight;

        double downEdges = 1 - Math.abs(noise.noise(x / 32,-35, z / 32));
        downEdges = Math.max(downEdges * downEdges - 0.5, 0);
        double downEdgesHeight = noise.noise(x / 48, -35, z / 48) * 2 + 3;
        downEdges *= -downEdgesHeight;

        return longRangeNoise + mediumRangeNoise + ridges + upEdges + downEdges + 60;
    }

    @Override
    public void generateColumn(VentureChunkGenerator generator, ChunkGenerator.ChunkData chunkData, BlazeNoiseGenerator noise, World world, double heightmap, double slope, int cx, int cz, int tx, int tz) {
        double dirtDepth = Math.max(4 - slope, 0);

        double grassTypeSelector = Math.abs(noise.noise(tx / 64d, heightmap / 64d + 1000, tz / 64d) - noise.noise(tx / 32d, heightmap / 64d + 2000, tz / 32d));

        double stoneQuarryDepth = Math.max(10 - slope * 10, 0);
        stoneQuarryDepth *= Math.min(Math.max(noise.noise(tx / 16d, heightmap / 64d + 3000, tz / 16d) - 0.3, 0) * 5, 1);

        double andesiteQuarryDepth = Math.max(5 - slope * 5, 0);
        andesiteQuarryDepth *= Math.min(Math.max(noise.noise(tx / 16d, heightmap / 64d + 4000, tz / 16d) - 0.5, 0) * 5, 1);

        double dioriteQuarryDepth = Math.max(5 - slope * 5, 0);
        dioriteQuarryDepth *= Math.min(Math.max(noise.noise(tx / 16d, heightmap / 64d + 5000, tz / 16d) - 0.5, 0) * 5, 1);

        double graniteQuarryDepth = Math.max(5 - slope * 5, 0);
        graniteQuarryDepth *= Math.min(Math.max(noise.noise(tx / 16d, heightmap / 64d + 6000, tz / 16d) - 0.5, 0) * 5, 1);

        double stoneRidges = Math.max(1 - Math.abs(noise.noise(tx / 64d, heightmap / 64d + 7000, tz / 64d) - noise.noise(tx / 32d, heightmap / 64d + 8000, tz / 32d)) * 10, 0);
        stoneRidges = Math.sqrt(stoneRidges);
        double stoneRidgesFilter = Math.min(Math.max(noise.noise(tx / 32d, heightmap / 64d + 9000, tz / 32d) * 2 - 0.5, 0), 1);
        double stoneRidgesHeight = noise.noise(tx / 4d, heightmap / 64d + 10000, tz / 4d) * 4 + 8;

        double gravelFactor = Math.min(Math.max(noise.noise(tx / 16d, heightmap / 64d + 11000, tz / 16d) * 4 - 1.5, 0), 1);

        int heightmapFloor = (int) heightmap;
        for (int y = 0; y <= heightmapFloor; y++) {
            double depth = heightmap - y;
            Material material = Material.STONE;

            if (depth < dirtDepth) {
                material = Material.DIRT;
            } else {
                depth -= dirtDepth;
            }

            if (y == heightmapFloor) {
                if (slope < 2) {
                    if (grassTypeSelector < 0.02) {
                        material = Material.DIRT;
                    } else if (grassTypeSelector < 0.04) {
                        material = Material.COARSE_DIRT;
                    } else {
                        material = Material.GRASS_BLOCK;
                    }

                    if (slope < 1.5) {
                        double randomHash = BlazeNoiseGenerator.hash(tx, heightmapFloor, tz, world.getSeed());
                        if (randomHash < gravelFactor) {
                            material = Material.GRAVEL;
                        }
                    }
                } else if (slope < 2.5) {
                    material = Material.COARSE_DIRT;
                }
            }

            depth = heightmap - y;
            if (stoneQuarryDepth > 0 || andesiteQuarryDepth > 0 || dioriteQuarryDepth > 0 || graniteQuarryDepth > 0) {
                if (depth < stoneQuarryDepth) {
                    material = Material.STONE;
                } else if (depth < andesiteQuarryDepth || depth < dioriteQuarryDepth || depth < graniteQuarryDepth) {
                    if (andesiteQuarryDepth > dioriteQuarryDepth && andesiteQuarryDepth > graniteQuarryDepth) {
                        material = Material.ANDESITE;
                    } else if (dioriteQuarryDepth > graniteQuarryDepth) {
                        material = Material.DIORITE;
                    } else {
                        material = Material.GRANITE;
                    }
                }
            }

            chunkData.setBlock(cx, y, cz, material);
        }

        double stoneRidgesTotal = stoneRidges * stoneRidgesFilter * stoneRidgesHeight;
        double stoneRidgesUpper = heightmap + stoneRidgesTotal;
        double stoneRidgesLower = heightmap - stoneRidgesTotal;

        for (int height = (int) Math.floor(stoneRidgesLower - 1); height <= stoneRidgesUpper + 1; height++) {
            if (height > stoneRidgesLower && height < stoneRidgesUpper) {
                chunkData.setBlock(cx, height, cz, Material.STONE);
            }
        }
    }

    @Override
    public void decorateColumn(VentureChunkGenerator generator, ChunkGenerator.ChunkData chunkData, BlazeNoiseGenerator noise, World world, double heightmap, double slope, int cx, int cz, int tx, int tz) {
        int heightmapFloor = (int) heightmap;

        Material topMaterial = chunkData.getType(cx, heightmapFloor, cz);
        if (topMaterial.equals(Material.GRASS_BLOCK)) {
            double selector = BlazeNoiseGenerator.hash(tx, heightmapFloor, tz, world.getSeed());
            if (selector < 0.02) {
                Bisected bisectedBottom = (Bisected) Bukkit.createBlockData(Material.TALL_GRASS);
                bisectedBottom.setHalf(Bisected.Half.BOTTOM);
                chunkData.setBlock(cx, heightmapFloor + 1, cz, bisectedBottom);

                Bisected bisectedTop = (Bisected) Bukkit.createBlockData(Material.TALL_GRASS);
                bisectedTop.setHalf(Bisected.Half.TOP);
                chunkData.setBlock(cx, heightmapFloor + 2, cz, bisectedTop);
            } else if (selector < 0.075) {
                chunkData.setBlock(cx, heightmapFloor + 1, cz, Material.GRASS);
            } else if (selector < 0.08) {
                chunkData.setBlock(cx, heightmapFloor + 1, cz, Material.BLUE_ORCHID);
            } else if (selector < 0.085) {
                chunkData.setBlock(cx, heightmapFloor + 1, cz, Material.CORNFLOWER);
            } else if (selector < 0.09) {
                chunkData.setBlock(cx, heightmapFloor + 1, cz, Material.POPPY);
            } else if (selector < 0.095) {
                chunkData.setBlock(cx, heightmapFloor + 1, cz, Material.ALLIUM);
            } else if (selector < 0.1) {
                chunkData.setBlock(cx, heightmapFloor + 1, cz, Material.LILY_OF_THE_VALLEY);
            } else if (selector < 0.12) {
                int treeHeight = (int) (BlazeNoiseGenerator.hash(tx, heightmapFloor, tz, world.getSeed() + 1) * 3) + 2;
                for (int i = 1; i < treeHeight; i++) {
                    setBlock(generator, tx, heightmapFloor + i, tz, Material.OAK_LOG);
                }

                Leaves leaves = (Leaves) Bukkit.createBlockData(Material.OAK_LEAVES);
                leaves.setDistance(1);
                int leavesHeight = heightmapFloor + treeHeight;
                for (int leavesY = 0; leavesY < 4; leavesY++) {
                    int radius = leavesY < 2 ? 2 : 1;

                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (Math.abs(x) == radius && Math.abs(z) == radius) {
                                boolean spawn = leavesY % 2 == 0 && BlazeNoiseGenerator.hash(tx + x, leavesHeight + leavesY, tz + z, world.getSeed() + 2) < 0.5;
                                if (spawn) {
                                    setBlock(generator, tx + x, leavesHeight + leavesY, tz + z, Material.OAK_LEAVES, leaves);
                                }
                            } else if (x == 0 && z == 0) {
                                if (leavesY == 0) {
                                    setBlock(generator, tx + x, leavesHeight + leavesY, tz + z, Material.OAK_LOG);
                                } else if (leavesY == 1 && BlazeNoiseGenerator.hash(tx + x, leavesHeight + leavesY, tz + z, world.getSeed() + 2) < 0.4) {
                                    setBlock(generator, tx + x, leavesHeight + leavesY, tz + z, Material.OAK_LOG);
                                } else {
                                    setBlock(generator, tx + x, leavesHeight + leavesY, tz + z, Material.OAK_LEAVES, leaves);
                                }
                            } else {
                                setBlock(generator, tx + x, leavesHeight + leavesY, tz + z, Material.OAK_LEAVES, leaves);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return Biome.FOREST;
    }
}