package com.diskree.keeploottable.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.PlayerGeneratesContainerLootCriterion;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity {

    @Unique
    private boolean isLootGenerated;

    @Shadow
    @Nullable
    protected Identifier lootTableId;

    protected LootableContainerBlockEntityMixin(
            BlockEntityType<?> blockEntityType,
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyReturnValue(method = "deserializeLootTable", at = @At("RETURN"))
    private boolean readCustomDataFromNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt) {
        if (original) {
            if (nbt.contains("LootGenerated")) {
                isLootGenerated = nbt.getBoolean("LootGenerated");
            }
            if (isLootGenerated) {
                return false;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "serializeLootTable", at = @At("RETURN"))
    private boolean writeCustomDataToNbt(boolean original, @Local(argsOnly = true) NbtCompound nbt) {
        if (original) {
            nbt.putBoolean("LootGenerated", isLootGenerated);
            if (isLootGenerated) {
                return false;
            }
        }
        return original;
    }

    @WrapOperation(method = "checkLootInteraction", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/LootableContainerBlockEntity;lootTableId:Lnet/minecraft/util/Identifier;", opcode = Opcodes.PUTFIELD))
    private void keepLootTableIdIfNeeded(LootableContainerBlockEntity instance, Identifier value, Operation<Void> original) {

    }

    @Redirect(method = "checkLootInteraction", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/LootableContainerBlockEntity;lootTableId:Lnet/minecraft/util/Identifier;", opcode = Opcodes.GETFIELD, ordinal = 0))
    private @Nullable Identifier modifyLootTableId(LootableContainerBlockEntity instance) {
        if (getWorld() == null || lootTableId == null) {
            return null;
        }
        if (!isLootGenerated) {
            return lootTableId;
        }
        return null;
    }

    @Redirect(method = "checkLootInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/PlayerGeneratesContainerLootCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;)V"))
    private void turnOffVanillaCriteriaTriggerLogic(PlayerGeneratesContainerLootCriterion instance, ServerPlayerEntity player, Identifier id) {
        // nothing
    }

    @Inject(method = "checkLootInteraction", at = @At(value = "HEAD"))
    private void applyCustomCriteriaTriggerLogic(PlayerEntity player, CallbackInfo ci) {
        World world = this.getWorld();
        if (lootTableId != null && world != null && world.getServer() != null && player instanceof ServerPlayerEntity) {
            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity) player, lootTableId);
        }
    }

    @Inject(method = "checkLootInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;supplyInventory(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/loot/context/LootContextParameterSet;J)V"))
    private void onLootGenerated(PlayerEntity player, CallbackInfo ci) {
        isLootGenerated = true;
    }
}
