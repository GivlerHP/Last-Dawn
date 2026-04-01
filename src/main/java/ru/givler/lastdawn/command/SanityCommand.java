package ru.givler.lastdawn.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import ru.givler.lastdawn.network.NetworkLD;
import ru.givler.lastdawn.sanity.ISanity;
import ru.givler.lastdawn.sanity.SanityProvider;
import ru.givler.lastdawn.sanity.SanityStage;

public class SanityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("sanity")
                        .requires(src -> src.hasPermission(2)) // op level 2

                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> setSanity(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "value"), null))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> setSanity(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "value"),
                                                        EntityArgument.getPlayer(ctx, "player"))))
                                )
                        )

                        // /sanity stage <stage> [player]
                        .then(Commands.literal("stage")
                                .then(Commands.literal("sane")
                                        .executes(ctx -> setStage(ctx.getSource(), SanityStage.SANE, null))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> setStage(ctx.getSource(), SanityStage.SANE,
                                                        EntityArgument.getPlayer(ctx, "player")))))
                                .then(Commands.literal("anxious")
                                        .executes(ctx -> setStage(ctx.getSource(), SanityStage.ANXIOUS, null))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> setStage(ctx.getSource(), SanityStage.ANXIOUS,
                                                        EntityArgument.getPlayer(ctx, "player")))))
                                .then(Commands.literal("psychosis")
                                        .executes(ctx -> setStage(ctx.getSource(), SanityStage.PSYCHOSIS, null))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> setStage(ctx.getSource(), SanityStage.PSYCHOSIS,
                                                        EntityArgument.getPlayer(ctx, "player")))))
                                .then(Commands.literal("insanity")
                                        .executes(ctx -> setStage(ctx.getSource(), SanityStage.INSANITY, null))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> setStage(ctx.getSource(), SanityStage.INSANITY,
                                                        EntityArgument.getPlayer(ctx, "player")))))
                        )

                        // /sanity max [player]
                        .then(Commands.literal("max")
                                .executes(ctx -> setSanity(ctx.getSource(), 100, null))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> setSanity(ctx.getSource(), 100,
                                                EntityArgument.getPlayer(ctx, "player"))))
                        )

                        // /sanity clear [player]
                        .then(Commands.literal("clear")
                                .executes(ctx -> setSanity(ctx.getSource(), 0, null))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> setSanity(ctx.getSource(), 0,
                                                EntityArgument.getPlayer(ctx, "player"))))
                        )
        );
    }

    private static int setSanity(CommandSourceStack source, int value, ServerPlayer target) {
        try {
            ServerPlayer player = target != null ? target : source.getPlayerOrException();
            player.getCapability(SanityProvider.SANITY_CAP).ifPresent(sanity -> {
                sanity.setSanity(value);
                syncSanity(player, sanity);
            });
            source.sendSuccess(() -> Component.literal(
                    "Рассудок игрока " + player.getName().getString() + " установлен на " + value
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Укажите игрока или выполните от лица игрока"));
            return 0;
        }
    }

    private static int setStage(CommandSourceStack source, SanityStage stage, ServerPlayer target) {
        int value = switch (stage) {
            case SANE      -> 12;
            case ANXIOUS   -> 37;
            case PSYCHOSIS -> 62;
            case INSANITY  -> 87;
        };
        return setSanity(source, value, target);
    }

    private static void syncSanity(ServerPlayer player, ISanity sanity) {
        NetworkLD.CHANNEL.sendTo(
                new ru.givler.lastdawn.network.packet.SanitySyncPacket(sanity.getSanity()),
                player.connection.connection,
                net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
        );
    }
}
