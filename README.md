This mod DOES NOT require a Fabric API because there are only a couple of mixins inside!

Currently, supported Minecraft versions range from 1.14.4 to 1.20.4.

# KeepLootTable

This simple mod causes the lootable block to not reset the NBT tag of the loot table after generating loot and changes the logic slightly to prevent loot from re-generating.

This mod should also work with all other types of lootable blocks: Barrel, Shulker Box, Trapped Chest, Dropper, etc.

### Where can this be useful?

This allows multiple players to get advancements for looting Chests. For example, "War Pigs" for looting a Chest in a Bastion Remnant from Vanilla, or the "I am Loot!" advancement for looting each type of chest from BlazeandCave's. This will eliminate the situation in which a player finds a rare Chest but does not get an advancement for opening it, because another player opened it first before him.

There is an advancement in BlazeandCave's that requires you to loot the Dispenser in the Jungle Pyramid. However, if a player or mob touches the Tripwire and the Dispenser shoots an Arrow, the Dispenser will generate loot first, which will remove the loot table tag from the NBT, and that Dispenser will no longer be usable for completing advancements. So, this non-obvious point is also solved by this mod, since the loot table tag will be saved after the Dispenser shoots with an Arrow.

I'm not sure, but if it is possible to make custom textures for loot Chest, then this mod makes it possible to determine the texture by the NBT tag of the loot table, since this tag will never be deleted.

### How do I contact you?

If you have problems with this mod, or if you want to ask to port the mod to the Minecraft version you need, please write about it [here](https://github.com/diskree/KeepLootTable/issues).

### Client/server?

This is a server-side mod; that is, it can work both in your single-player game and on a dedicated server.
