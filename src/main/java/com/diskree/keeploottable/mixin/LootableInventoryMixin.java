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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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
    @Nullable World getWorld();

    @Shadow
    @Nullable Identifier getLootTableId();

    @ModifyReturnValue(method = "readLootTable", at = @At("RETURN"))
    private boolean readCustomDataFromNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt) {
        if (original && this instanceof LootableContainerBlockEntityAccessor lootableContainerBlockEntity) {
            lootableContainerBlockEntity.keeploottable$readDataFromNbt(nbt);
            if (!lootableContainerBlockEntity.keeploottable$shouldGenerateLoot()) {
                return false;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "writeLootTable", at = @At("RETURN"))
    private boolean writeCustomDataToNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt) {
        if (original && this instanceof LootableContainerBlockEntityAccessor lootableContainerBlockEntity) {
            lootableContainerBlockEntity.keeploottable$writeDataToNbt(nbt);
            if (!lootableContainerBlockEntity.keeploottable$shouldGenerateLoot()) {
                return false;
            }
        }
        return original;
    }

    @WrapOperation(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;setLootTableId(Lnet/minecraft/util/Identifier;)V"))
    private void keepLootTableIdIfNeeded(LootableInventory instance, @Nullable Identifier identifier, Operation<Void> original) {
        if (!(this instanceof LootableContainerBlockEntity) || identifier != null) {
            original.call(instance, identifier);
        }
    }

    @ModifyExpressionValue(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/LootableInventory;getLootTableId()Lnet/minecraft/util/Identifier;"))
    private Identifier modifyLootTableId(Identifier original) {
        if (getWorld() == null || getLootTableId() == null) {
            return null;
        }
        if (this instanceof LootableContainerBlockEntityAccessor lootableContainerBlockEntity && lootableContainerBlockEntity.keeploottable$shouldGenerateLoot()) {
            return original;
        }
        return null;
    }

    @Redirect(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/PlayerGeneratesContainerLootCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;)V"))
    private void turnOffVanillaCriteriaTriggerLogic(PlayerGeneratesContainerLootCriterion instance, ServerPlayerEntity player, Identifier id) {
        // nothing
    }

    @Inject(method = "generateLoot", at = @At(value = "HEAD"))
    private void applyCustomCriteriaTriggerLogic(PlayerEntity player, CallbackInfo ci) {
        World world = this.getWorld();
        Identifier identifier = this.getLootTableId();
        if (identifier != null && world != null && world.getServer() != null && player instanceof ServerPlayerEntity) {
            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity) player, identifier);
        }
    }

    @Inject(method = "generateLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;supplyInventory(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/loot/context/LootContextParameterSet;J)V"))
    private void onLootGenerated(PlayerEntity player, CallbackInfo ci) {
        if (this instanceof LootableContainerBlockEntityAccessor lootableContainerBlockEntity) {
            lootableContainerBlockEntity.keeploottable$onLootGenerated();
        }
    }
}
