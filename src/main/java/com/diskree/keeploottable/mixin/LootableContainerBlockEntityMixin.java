package com.diskree.keeploottable.mixin;

import com.diskree.keeploottable.LootableContainerBlockEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin
        extends LockableContainerBlockEntity
        implements LootableInventory, LootableContainerBlockEntityAccessor {

    @Unique
    private boolean isLootGenerated;

    protected LootableContainerBlockEntityMixin(
            BlockEntityType<?> blockEntityType,
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void keeploottable$writeDataToNbt(@NotNull NbtCompound nbt) {
        nbt.putBoolean("LootGenerated", isLootGenerated);
    }

    @Override
    public void keeploottable$readDataFromNbt(@NotNull NbtCompound nbt) {
        if (nbt.contains("LootGenerated")) {
            isLootGenerated = nbt.getBoolean("LootGenerated");
        }
    }

    @Override
    public boolean keeploottable$shouldGenerateLoot() {
        return !isLootGenerated;
    }

    @Override
    public void keeploottable$onLootGenerated() {
        isLootGenerated = true;
    }
}
