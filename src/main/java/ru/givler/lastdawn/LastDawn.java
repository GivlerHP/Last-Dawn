package ru.givler.lastdawn;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import ru.givler.lastdawn.client.ClientEventHandler;
import ru.givler.lastdawn.network.LDNetwork;
import ru.givler.lastdawn.registry.BlockRegistration;
import ru.givler.lastdawn.registry.ItemRegistration;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LastDawn.MODID)
public class LastDawn
{
    public static final String MODID = "lastdawn";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LastDawn(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);

        BlockRegistration.BLOCKS.register(modEventBus);
        ItemRegistration.ITEMS.register(modEventBus);
      //  TabRegistration.register(modEventBus);


        modEventBus.addListener(this::commonSetup);
      //  context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        //client only
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            //working on screen dimming and blinking
            //  MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        }
        // modEventBus.addListener(ClientEvents::onClientSetup); — если требуется клиент
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LDNetwork.register();
        LOGGER.info("LastDawn mod initialized");
    }
}