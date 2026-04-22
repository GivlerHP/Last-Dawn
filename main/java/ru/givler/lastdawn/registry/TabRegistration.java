package ru.givler.lastdawn.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.givler.lastdawn.LastDawn;

public class TabRegistration {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LastDawn.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB =
            TABS.register("main_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + LastDawn.MODID + ".main_tab"))
                    .icon(() -> new ItemStack(ItemRegistration.LOCKING_KEY.get()))
                    .displayItems((params, output) -> {
                        // Предметы
                        output.accept(ItemRegistration.TORCH_ITEM.get());
                        output.accept(ItemRegistration.LOCKING_KEY.get());

                        // Блоки
                        output.accept(BlockRegistration.GHOST_WALL.get());
                    })
                    .build()
            );

    public static void register() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        TABS.register(bus);
    }
}