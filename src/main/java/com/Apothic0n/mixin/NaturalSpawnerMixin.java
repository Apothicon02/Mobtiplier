package com.Apothic0n.mixin;

import com.Apothic0n.MobtiplierJsonReader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @Shadow
    protected static boolean isValidPositionForMob(ServerLevel serverLevel, Mob mob, double d) {
        return false;
    }

    @Shadow
    @Nullable
    protected static Mob getMobForSpawn(ServerLevel serverLevel, EntityType<?> entityType) {
        return null;
    }

    @Shadow
    protected static boolean isValidSpawnPostitionForType(ServerLevel serverLevel, MobCategory mobCategory, StructureManager structureManager, ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnerData, BlockPos.MutableBlockPos mutableBlockPos, double d) {
        return false;
    }

    @Shadow
    protected static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, RandomSource randomSource, BlockPos blockPos) {
        return null;
    }

    @Shadow
    protected static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos, double d) {
        return false;
    }

    /**
     * @author Apothicon
     * @reason Spawn multiple times
     */
    @Overwrite
    public static void spawnCategoryForPosition(
            MobCategory mobCategory,
            ServerLevel serverLevel,
            ChunkAccess chunkAccess,
            BlockPos blockPos,
            NaturalSpawner.SpawnPredicate spawnPredicate,
            NaturalSpawner.AfterSpawnCallback afterSpawnCallback
    ) {
        for (int mul = 0; mul < MobtiplierJsonReader.multiplier; mul++) {
            StructureManager structureManager = serverLevel.structureManager();
            ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
            int i = blockPos.getY();
            BlockState blockState = chunkAccess.getBlockState(blockPos);
            if (!blockState.isRedstoneConductor(chunkAccess, blockPos)) {
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                int j = 0;

                for (int k = 0; k < 3; k++) {
                    int l = blockPos.getX();
                    int m = blockPos.getZ();
                    int n = 6;
                    MobSpawnSettings.SpawnerData spawnerData = null;
                    SpawnGroupData spawnGroupData = null;
                    int o = Mth.ceil(serverLevel.random.nextFloat() * 4.0F);
                    int p = 0;

                    for (int q = 0; q < o; q++) {
                        l += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
                        m += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
                        mutableBlockPos.set(l, i, m);
                        double d = (double) l + 0.5;
                        double e = (double) m + 0.5;
                        Player player = serverLevel.getNearestPlayer(d, (double) i, e, -1.0, false);
                        if (player != null) {
                            double f = player.distanceToSqr(d, (double) i, e);
                            if (isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, f)) {
                                if (spawnerData == null) {
                                    Optional<MobSpawnSettings.SpawnerData> optional = getRandomSpawnMobAt(
                                            serverLevel, structureManager, chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos
                                    );
                                    if (optional.isEmpty()) {
                                        break;
                                    }

                                    spawnerData = (MobSpawnSettings.SpawnerData) optional.get();
                                    o = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                                }

                                if (isValidSpawnPostitionForType(serverLevel, mobCategory, structureManager, chunkGenerator, spawnerData, mutableBlockPos, f)
                                        && spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) {
                                    Mob mob = getMobForSpawn(serverLevel, spawnerData.type);
                                    if (mob == null) {
                                        return;
                                    }

                                    mob.moveTo(d, (double) i, e, serverLevel.random.nextFloat() * 360.0F, 0.0F);
                                    if (isValidPositionForMob(serverLevel, mob, f)) {
                                        spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData);
                                        j++;
                                        p++;
                                        serverLevel.addFreshEntityWithPassengers(mob);

                                        afterSpawnCallback.run(mob, chunkAccess);
                                        if (j >= mob.getMaxSpawnClusterSize()) {
                                            return;
                                        }

                                        if (mob.isMaxGroupSizeReached(p)) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}