package ru.givler.lastdawn;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import ru.givler.lastdawn.config.Config;
import ru.givler.lastdawn.registration.BlocksRegistration;
import ru.givler.lastdawn.registration.ItemsRegistration;
import ru.givler.lastdawn.registration.TabRegistration;
import ru.givler.lastdawn.sanity.ISanity;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LastDawn.MODID)
public class LastDawn
{
    public static final String MODID = "lastdawn";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LastDawn(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

      //  BlocksRegistration.register(modEventBus);
      //  ItemsRegistration.register(modEventBus);
      //  TabRegistration.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this); // только если тут нужны события

      //  context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(this::commonSetup);
        // modEventBus.addListener(ClientEvents::onClientSetup); — если требуется клиент
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("LastDawn mod initialized");
    }
}