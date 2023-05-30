package gg.valgo.venture.generator.biome;

import gg.valgo.venture.generator.biome.biomes.ForestHillsVentureBiome;
import gg.valgo.venture.noise.BlazeNoiseGenerator;

import java.util.ArrayList;

public class VentureBiomes {
    public static final VentureBiome FOREST_HILLS = new ForestHillsVentureBiome();

    public static ArrayList<WeightedVentureBiome> getWeightedBiomes(BlazeNoiseGenerator noise, double x, double z) {
        ArrayList<WeightedVentureBiome> weightedBiomes = new ArrayList<>();

        int i = 0;
        weightedBiomes.add(new WeightedVentureBiome(VentureBiomes.FOREST_HILLS, noise.noise(x / 128d, i++ * -1000000, z / 128d) * 0.5 + 0.5));

        double maxWeight = -1;
        int maxIndex = -1;

        for (int index = 0; index < weightedBiomes.size(); index++) {
            WeightedVentureBiome weightedBiome = weightedBiomes.get(index);
            if (weightedBiome.getWeight() > maxWeight) {
                maxWeight = weightedBiome.getWeight();
                maxIndex = index;
            }
        }

        weightedBiomes.add(0, weightedBiomes.remove(maxIndex));

        return weightedBiomes;
    }
}