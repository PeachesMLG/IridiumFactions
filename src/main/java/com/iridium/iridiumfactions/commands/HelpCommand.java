package com.iridium.iridiumfactions.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumfactions.IridiumFactions;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Command which shows users a list of all IridiumFactions commands.
 */
public class HelpCommand extends Command {

    /**
     * The default constructor.
     */
    public HelpCommand() {
        super(Collections.singletonList("help"), "Show a list of all commands", "", false, Duration.ZERO);
    }

    /**
     * Executes the command for the specified {@link CommandSender} with the provided arguments.
     * Not called when the command execution was invalid (no permission, no player or command disabled).
     * Shows a list of all IridiumSkyblock commands.
     *
     * @param sender    The CommandSender which executes this command
     * @param arguments The arguments used with this command. They contain the sub-command
     */
    @Override
    public boolean execute(CommandSender sender, String[] arguments) {
        List<Command> availableCommands = IridiumFactions.getInstance().getCommandManager().commands.stream()
                .filter(command -> sender.hasPermission(command.permission) || command.permission.isEmpty())
                .collect(Collectors.toList());

        int page = 1;
        int maxPage = (int) Math.ceil(availableCommands.size() / 8.0);

        // Read optional page argument
        if (arguments.length > 1) {
            String pageArgument = arguments[1];
            if (pageArgument.matches("[0-9]+")) {
                page = Integer.parseInt(pageArgument);
            }
        }

        // Correct requested page if it's out of bounds
        if (page > maxPage) {
            page = maxPage;
        } else if (page < 1) {
            page = 1;
        }

        // Prepare the footer
        TextComponent footerText = new TextComponent(StringUtils.color(IridiumFactions.getInstance().getMessages().helpCommandFooter
                .replace("%page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(maxPage))));
        TextComponent previousButton = new TextComponent(StringUtils.color(IridiumFactions.getInstance().getMessages().helpCommandPreviousPage));
        TextComponent nextButton = new TextComponent(StringUtils.color(IridiumFactions.getInstance().getMessages().helpCommandNextPage));
        if (page != 1) {
            previousButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factions help " + (page - 1)));
            previousButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringUtils.color(IridiumFactions.getInstance().getMessages().helpCommandPreviousPageHover)).create()));
        }
        if (page != maxPage) {
            nextButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factions help " + (page + 1)));
            nextButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringUtils.color(IridiumFactions.getInstance().getMessages().helpCommandNextPageHover)).create()));
        }

        // Send all messages
        sender.sendMessage(StringUtils.color(IridiumFactions.getInstance().getMessages().helpCommandHeader));
        availableCommands.stream()
                .skip((page - 1) * 8L)
                .limit(8)
                .map(command -> StringUtils.color(IridiumFactions.getInstance().getMessages().helpCommandMessage
                        .replace("%command%", command.aliases.get(0))
                        .replace("%description%", command.description)))
                .forEach(sender::sendMessage);

        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(previousButton, footerText, nextButton);
        }

        return true;
    }

    /**
     * Handles tab-completion for this command.
     *
     * @param commandSender The CommandSender which tries to tab-complete
     * @param cmd           The command
     * @param label         The label of the command
     * @param args          The arguments already provided by the sender
     * @return The list of tab completions for this command
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length == 2) {
            int availableCommandAmount = (int) IridiumFactions.getInstance().getCommandManager().commands.stream()
                    .filter(command -> commandSender.hasPermission(command.permission) || command.permission.isEmpty())
                    .count();

            // Return all numbers from 1 to the max page
            return IntStream.rangeClosed(1, (int) Math.ceil(availableCommandAmount / 8.0))
                    .boxed()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }

        // We currently don't want to tab-completion here
        // Return a new List so it isn't a list of online players
        return Collections.emptyList();
    }

}
