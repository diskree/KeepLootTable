package com.diskree.keeploottable;

import net.minecraft.nbt.NbtCompound;

public interface LootableContainerBlockEntityAccessor {

    void keeploottable$writeDataToNbt(NbtCompound nbtCompound);

    void keeploottable$readDataFromNbt(NbtCompound nbtCompound);

    boolean keeploottable$shouldGenerateLoot();

    void keeploottable$onLootGenerated();
}
