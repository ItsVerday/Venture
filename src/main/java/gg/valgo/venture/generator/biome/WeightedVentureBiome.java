package gg.valgo.venture.generator.biome;

public class WeightedVentureBiome {
    private VentureBiome biome;
    private double weight;

    public WeightedVentureBiome(VentureBiome biome, double weight) {
        this.biome = biome;
        this.weight = weight;
    }

    public VentureBiome getBiome() {
        return biome;
    }

    public double getWeight() {
        return weight;
    }

    public double getBiasedWeight(double baseline) {
        return Math.pow(weight / baseline, 9);
    }
}