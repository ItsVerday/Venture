package gg.valgo.venture.noise;

import org.bukkit.World;
import org.bukkit.util.noise.NoiseGenerator;

public class BlazeNoiseGenerator extends NoiseGenerator {
    private long seed;

    public BlazeNoiseGenerator(long seed) {
        this.seed = seed;
    }

    public BlazeNoiseGenerator(World world) {
        this(world.getSeed());
    }

    private static long doShift(long value, int shift) {
        return value ^ (value >>> shift);
    }

    private static long permute(long value) {
        long original = value;
        value = doShift(value, 32) * ~value;
        value = doShift(value, 16) * ~value;
        return value * original;
    }

    public static double hash(long x, long y, long z, long seed) {
        long result = permute(seed);
        result = 31 * result + permute(x * result);
        result = 31 * result + permute(y * result);
        result = 31 * result + permute(z * result);
        result = 31 * result + permute(seed * result);
        return ((double) (result & 0xFFFFFFFFL)) / 0x100000000L;
    }

    public double hash(long x, long y, long z) {
        return hash(x, y, z, seed);
    }

    @Override
    public double noise(double x, double y, double z) {
        double weight = 0;
        double total = 0;

        int x_floor = floor(x);
        double x_fract = x - x_floor;
        int y_floor = floor(y);
        double y_fract = y - y_floor;
        int z_floor = floor(z);
        double z_fract = z - z_floor;

        for (int xo = -1; xo <= 1; xo++) {
            for (int yo = -1; yo <= 1; yo++) {
                for (int zo = -1; zo <= 1; zo++) {
                    int true_x = x_floor + xo;
                    int true_y = y_floor + yo;
                    int true_z = z_floor + zo;

                    double pt_x = xo - x_fract + hash(true_x, true_y, true_z, seed + 1) * 0.8 + 0.1;
                    double pt_y = yo - y_fract + hash(true_x, true_y, true_z, seed + 2) * 0.8 + 0.1;
                    double pt_z = zo - z_fract + hash(true_x, true_y, true_z, seed + 3) * 0.8 + 0.1;

                    double dist_sq = (pt_x * pt_x + pt_y * pt_y + pt_z * pt_z) * 0.9;
                    if (dist_sq >= 1) {
                        continue;
                    }

                    double dist = dist_sq;
                    dist = (dist_sq / dist + dist) * 0.5;
                    dist = (dist_sq / dist + dist) * 0.5;

                    dist = dist * dist * (3 - 2 * dist);
                    dist = 1 - dist;
                    double value = hash(true_x, true_y, true_z);

                    weight += dist;
                    total += value * dist;
                }
            }
        }

        double result = weight > 0 ? total / weight : 0.5;
        return result * 2 - 1;
    }
}