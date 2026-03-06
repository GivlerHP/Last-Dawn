package ru.givler.lastdawn.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.command.SanityCommand;

public class ItemRegistration {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LastDawn.MODID);
}
