package gg.valgo.venture.generator;

import gg.valgo.venture.generator.biome.VentureBiome;

public class ChunkColumnData {
    private VentureBiome biome;
    private double heightmap;
    private double slope;

    public ChunkColumnData(VentureBiome biome, double heightmap, double slope) {
        this.biome = biome;
        this.heightmap = heightmap;
        this.slope = slope;
    }

    public double getHeightmap() {
        return heightmap;
    }

    public double getSlope() {
        return slope;
    }

    public VentureBiome getBiome() {
        return biome;
    }
}