package net.japanpvpserver.commandforwardplus;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.google.common.base.Joiner;
import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@CommandAlias("forward|f")
public class ForwardCommand extends BaseCommand {

    private final CommandForwardPlus plus;
    private final ProxyServer server = ProxyServer.getInstance();

    private final int ARG_START = 1;

    public ForwardCommand(CommandForwardPlus plus) {
        this.plus = plus;
    }

    @Default
    private void help(CommandSender sender) {
        sender.sendMessage(new MineDown("&b[/forward](show_text=&cClick me for execute suggest_command=/forward) &7- Shows you this message").toComponent());
        sender.sendMessage(new MineDown("&b[/forward <player> <command> [args...]](show_text=&cClick me for execute suggest_command=/forward ) &7- Execute as <player>").toComponent());
        sender.sendMessage(new MineDown("&b[/forward @p <command> [args...]](show_text=&cClick me for execute suggest_command=/forward @p) &7- Execute as You").toComponent());
        sender.sendMessage(new MineDown("&b[/forward @a <command> [args...]](show_text=&cClick me for execute suggest_command=/forward @a) &7- Execute as Proxy Online players").toComponent());
        sender.sendMessage(new MineDown("&b[/forward @s <command> [args...]](show_text=&cClick me for execute suggest_command=/forward @s) &7- Execute as Current Server players").toComponent());
        sender.sendMessage(new MineDown("&b[/forward @lobby <command> [args...]](show_text=&cClick me for execute suggest_command=/forward @) &7- Execute as all player in the 'lobby' server").toComponent());
    }

    @Default
    @CommandPermission("forward")
    @CommandCompletion("@target @cmd @nothing")
    private void forward(CommandSender sender, String target, String[] args) {
        final Set<ProxiedPlayer> targets = new HashSet<>();
        final ProxiedPlayer proxiedPlayer = server.getPlayer(sender.getName());

        if (args.length == 0) {
            sendErrorMessage(sender, "Wrong command, Missing arguments");
            return;
        }

        if (proxiedPlayer == null) return;
        switch (target) {
            case "@a" -> targets.addAll(plus.getOwnServerPlayers(proxiedPlayer));
            case "@s" -> targets.addAll(plus.getBungeeOnlinePlayers());
            case "@p" -> targets.add(proxiedPlayer);
            default -> {
                if (target.startsWith("@")) {
                    final String serverName = target.substring(1);
                    targets.addAll(plus.getServerPlayers(serverName));
                    break;
                }
                final ProxiedPlayer player = plus.getProxy().getPlayer(target);
                if (player != null) targets.add(player);
            }
        }
        targets.forEach(player -> invokeCommand(player, args[0], dropFirstArgs(args, ARG_START)));
    }

    private void invokeCommand(CommandSender invoker, String command, String[] arguments) {
        PluginManager pluginManager = server.getPluginManager();

        pluginManager.getCommands()
                .stream()
                .filter(entry -> !plus.getSettings().getIgnoreCommands().contains(entry.getKey()))
                .filter(entry -> !entry.getKey().equals("forward"))
                .filter(entry -> !entry.getKey().equals("f"))
                .filter(entry -> entry.getKey().equals(command.toLowerCase()))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(pluginCmd -> {
                    pluginCmd.execute(invoker, Joiner.on(' ').join(arguments).split(" "));
                    return pluginCmd;
                }).orElseGet(() -> {
                    sendErrorMessage(invoker, "Unknown command : " + command);
                    return null;
                });
    }

    private void sendErrorMessage(CommandSender sender, String message) {
        String prefix = String.format("[%s] %s", "forward+", message);
        BaseComponent[] components = new ComponentBuilder(prefix)
                .color(ChatColor.RED)
                .create();
        sender.sendMessage(components);
    }

    private String[] dropFirstArgs(String[] args, int pos) {
        return Arrays.copyOfRange(args, pos, args.length);
    }
}
