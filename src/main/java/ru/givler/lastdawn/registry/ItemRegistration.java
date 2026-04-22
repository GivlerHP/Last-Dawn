package ru.givler.lastdawn.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.givler.lastdawn.LastDawn;
import ru.givler.lastdawn.command.SanityCommand;
import ru.givler.lastdawn.item.FragileKeyItem;
import ru.givler.lastdawn.item.LockingKeyItem;
import ru.givler.lastdawn.item.TorchItem;

public class ItemRegistration {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LastDawn.MODID);

    public static final RegistryObject<Item> TORCH_ITEM =
            ITEMS.register("torch",
                    () -> new TorchItem(
                            BlockRegistration.TORCH.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> LOCKING_KEY =
            ITEMS.register("locking_key",
                    () -> new LockingKeyItem()
            );

    public static final RegistryObject<Item> FRAGILE_KEY =
            ITEMS.register("fragile_key",
                    () -> new FragileKeyItem()
            );
}
