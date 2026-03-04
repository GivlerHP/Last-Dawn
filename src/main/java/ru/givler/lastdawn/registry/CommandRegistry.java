package ru.givler.lastdawn.registry;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import ru.givler.lastdawn.command.SanityCommand;

public class CommandRegistry {
    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        SanityCommand.register(dispatcher);
    }
}
