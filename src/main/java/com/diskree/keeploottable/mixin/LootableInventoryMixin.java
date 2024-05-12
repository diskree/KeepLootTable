package com.diskree.keeploottable.mixin;

import com.diskree.keeploottable.LootableContainerBlockEntityAccessor;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.PlayerGeneratesContainerLootCriterion;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableInventory.class)
public interface LootableInventoryMixin {

    @Shadow
    @Nullable
    World getWorld();

    @Shadow
    @Nullable
    RegistryKey<LootTable> getLootTable();

    @ModifyReturnValue(
        method = "readLootTable",
        at = @At("RETURN")
    )
    private boolean readCustomDataFromNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt) {
        if (original && this instanceof LootableContainerBlockEntityAccessor lootableContainerBlockEntity) {
            lootableContainerBlockEntity.keeploottable$readDataFromNbt(nbt);
            if (!lootableContainerBlockEntity.keeploottable$shouldGenerateLoot()) {
                return false;
            }
        }
        return original;
    }

    @ModifyReturnValue(
        method = "writeLootTable",
        at = @At("RETURN")
    )
    private boolean writeCustomDataToNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt) {
        if (original && this instanceof LootableContainerBlockEntityAccessor lootableContainerBlockEntity) {
            lootableContainerBlockEntity.keeploottable$writeDataToNbt(nbt);
            if (!lootableContainerBlockEntity.keeploottable$shouldGenerateLoot()) {
                return false;
            }
        }
        return original;
    }

    @WrapOperation(
        method = "generateLoot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/LootableInventory;setLootTable(Lnet/minecraft/registry/RegistryKey;)V"
        )
    )
    private void keepLootTableIdIfNeeded(
        LootableInventory lootableInventory,
        RegistryKey<LootTable> lootTableRegistryKey,
        Operation<Void> original
    ) {
        if (!(this instanceof LootableContainerBlockEntity) || lootTableRegistryKey != null) {
            original.call(lootableInventory, lootTableRegistryKey);
        }
    }

    @ModifyExpressionValue(
        method = "generateLoot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/LootableInventory;getLootTable()Lnet/minecraft/registry/RegistryKey;"
        )
    )
    private RegistryKey<LootTable> modifyLootTableId(RegistryKey<LootTable> original) {
        if (getWorld() == null || getLootTable() == null) {
            return null;
        }
        if (this instanceof LootableContainerBlockEntityAccessor lootableContainerBlockEntity &&
            lootableContainerBlockEntity.keeploottable$shouldGenerateLoot()) {
            return original;
        }
        return null;
    }

    @Redirect(
        method = "generateLoot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancement/criterion/PlayerGeneratesContainerLootCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/registry/RegistryKey;)V"
        )
    )
    private void turnOffVanillaCriteriaTriggerLogic(
        PlayerGeneratesContainerLootCriterion criterion,
        ServerPlayerEntity player,
        RegistryKey<LootTable> lootTable
    ) {
        // nothing
    }

    @Inject(
        method = "generateLoot",
        at = @At(value = "HEAD")
    )
    private void applyCustomCriteriaTriggerLogic(PlayerEntity player, CallbackInfo ci) {
        World world = this.getWorld();
        RegistryKey<LootTable> lootTable = this.getLootTable();
        if (lootTable != null && world != null && world.getServer() != null && player instanceof ServerPlayerEntity) {
            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity) player, lootTable);
        }
    }

    @Inject(
        method = "generateLoot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/loot/LootTable;supplyInventory(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/loot/context/LootContextParameterSet;J)V"
        )
    )
    private void onLootGenerated(PlayerEntity player, CallbackInfo ci) {
        if (this instanceof LootableContainerBlockEntityAccessor lootableContainerBlockEntity) {
            lootableContainerBlockEntity.keeploottable$onLootGenerated();
        }
    }
}
