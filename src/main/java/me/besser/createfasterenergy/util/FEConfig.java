package me.besser.createfasterenergy.util;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FEConfig {
    public static final ModConfigSpec SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ModConfigSpec.IntValue baseStressImpact;
        public final ModConfigSpec.IntValue minRpm;
        public final ModConfigSpec.IntValue maxRpm;
        public final ModConfigSpec.DoubleValue fePerRpm;

        public final ModConfigSpec.IntValue optimalRpm;
        public final ModConfigSpec.DoubleValue maxEfficiency;
        public final ModConfigSpec.DoubleValue minEfficiency;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("alternator_settings");

            baseStressImpact = builder
                    .comment("The base stress impact. Create will multiply this to get the final stress impact.")
                    .defineInRange("baseStressImpact", 128, 0, 1024);

            minRpm = builder
                    .comment("The minimum RPM required for the alternator to operate.")
                    .defineInRange("minRpm", 32, 0, 1024);

            maxRpm = builder
                    .comment("The maximum RPM the alternator can handle. This value should match Create's max RPM.")
                    .defineInRange("maxRpm", 256, 0, 1024);

            fePerRpm = builder
                    .comment("How much FE is generated per 1 RPM at 100% efficiency.")
                    .defineInRange("fePerRpm", 2.0d, 0.0d, 1000.0d);

            builder.pop();

            builder.push("alternator_efficiency_curve");

            optimalRpm = builder
                    .comment("The RPM at which the alternator runs most efficiently.")
                    .defineInRange("optimalRpm", 128, 0, 1024);

            maxEfficiency = builder
                    .comment("The peak efficiency multiplier.")
                    .defineInRange("maxEfficiency", 0.96d, 0.0d, 1.0d);

            minEfficiency = builder
                    .comment("The base efficiency multiplier. " +
                            "Values below ~0.7 will lead to less FE at high RPMs than lower RPMs.")
                    .defineInRange("minEfficiency", 0.7d, 0.0d, 1.0d);

            builder.pop();
        }
    }
}