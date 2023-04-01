package it.technoade.technoblocks;

import it.technoade.technoblocks.utils.Items;
import it.technoade.technoblocks.utils.ItemsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player p) {
            if(args.length == 1) {
                String a = args[0];
                if(a.equals("reload")) {
                    Main.plugin.reloadConfig();
                    ItemsManager.item.clear();
                    Bukkit.getScheduler().runTaskLater(Main.plugin, ItemsManager::registerItems, 1);
                    p.sendMessage("§bReload succefully");
                }
            } else {
                p.sendMessage("§8<§7--------------------------------------§8>");
                p.sendMessage("§r");
                p.sendMessage("§b§lTechnoBlocks 2.0 §7developed by §b§lTechnoAde");
                p.sendMessage("§7for support join this discord: §bhttps://discord.gg/RPyF7Hjvc9");
                p.sendMessage("§r");
                p.sendMessage("§8<§7--------------------------------------§8>");
            }
        }

        return false;
    }
}
