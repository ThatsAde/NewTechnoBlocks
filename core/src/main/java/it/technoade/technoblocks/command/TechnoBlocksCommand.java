package it.technoade.technoblocks.command;

import it.technoade.technoblocks.TechnoBlocksPlugin;
import it.technoade.technoblocks.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * /technoblocks [reload|clear], also usable from console.
 */
public final class TechnoBlocksCommand implements CommandExecutor, TabCompleter {

    public static final String ADMIN_PERMISSION = "technoblocks.admin";
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "clear");

    private final TechnoBlocksPlugin plugin;

    public TechnoBlocksCommand(TechnoBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = plugin.getMessages();
        if (args.length == 0) {
            for (String line : messages.getList("info", "version", plugin.getDescription().getVersion())) {
                sender.sendMessage(line);
            }
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (!SUBCOMMANDS.contains(sub)) {
            sender.sendMessage(messages.get("usage", "label", label));
            return true;
        }
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(messages.get("command-no-permission"));
            return true;
        }
        if (sub.equals("reload")) {
            plugin.reload();
            sender.sendMessage(plugin.getMessages().get("reload-done"));
        } else {
            int count = plugin.clearBlocks();
            sender.sendMessage(messages.get("clear-done", "count", String.valueOf(count)));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !sender.hasPermission(ADMIN_PERMISSION)) return Collections.emptyList();
        List<String> out = new ArrayList<String>();
        for (String sub : SUBCOMMANDS) {
            if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(sub);
        }
        return out;
    }
}
