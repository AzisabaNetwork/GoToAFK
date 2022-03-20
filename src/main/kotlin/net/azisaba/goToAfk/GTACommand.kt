package net.azisaba.goToAfk

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.command.SimpleCommand.Invocation
import net.azisaba.goToAfk.util.ServerInfoResolvable.Companion.toResolvable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class GTACommand(private val plugin: GoToAFK): SimpleCommand {
    override fun execute(invocation: Invocation) {
        val sender = invocation.source()
        val args = invocation.arguments()
        if (args.isEmpty()) {
            return sender.sendMessage(Component.text("/gta <reload|ignore <server>|debug <add|remove>>", NamedTextColor.RED))
        }
        if (args[0] == "debug") {
            if (args[1] == "add") {
                if (args.size <= 3) return sender.sendMessage(Component.text("/gta debug add <player> <server>", NamedTextColor.RED))
                val player = plugin.server.getPlayer(args[2]).orElse(null)
                    ?: return sender.sendMessage(Component.text("プレイヤーが見つかりません", NamedTextColor.RED))
                val server = plugin.server.getServer(args[3]).orElse(null)
                    ?: return sender.sendMessage(Component.text("サーバーが見つかりません", NamedTextColor.RED))
                GoToAFK.serversMap[player.uniqueId] = server.toResolvable()
                GoToAFK.plsCheck.add(player.uniqueId)
                sender.sendMessage(Component.text(":done:", NamedTextColor.GREEN))
            } else if (args[1] == "remove") {
                if (args.size <= 3) return sender.sendMessage(Component.text("/gta debug remove <player>", NamedTextColor.RED))
                val player = plugin.server.getPlayer(args[2]).orElse(null)
                    ?: return sender.sendMessage(Component.text("プレイヤーが見つかりません", NamedTextColor.RED))
                GoToAFK.serversMap.remove(player.uniqueId)
                GoToAFK.plsCheck.remove(player.uniqueId)
                sender.sendMessage(Component.text(":done:", NamedTextColor.GREEN))
            }
        } else if (args[0] == "reload") {
            ReloadableGTAConfig.reload(sender)
            GoToAFK.instance.startPingerThread()
            sender.sendMessage(Component.text("設定を再読み込みしました。", NamedTextColor.GREEN))
        } else if (args[0] == "ignore") {
            if (args.size == 1) {
                return sender.sendMessage(Component.text("/gta ignore <server>", NamedTextColor.RED))
            }
            val server = plugin.server.getServer(args[1]).orElse(null)?.serverInfo
                ?: return sender.sendMessage(Component.text("サーバーが見つかりません。", NamedTextColor.RED))
            if (GoToAFK.ignoreList.contains(server.name.lowercase())) {
                GoToAFK.ignoreList.remove(server.name.lowercase())
                sender.sendMessage(Component.text("サーバー'${server.name}'を除外リストから削除しました。", NamedTextColor.GREEN))
            } else {
                GoToAFK.ignoreList.add(server.name.lowercase())
                GoToAFK.onlineServers.remove(server.name.lowercase())
                sender.sendMessage(Component.text("サーバー'${server.name}'を除外リストに追加しました。", NamedTextColor.GREEN))
            }
        }
    }

    override fun suggest(invocation: Invocation): List<String> {
        val args = invocation.arguments()
        if (args.isEmpty()) return emptyList()
        if (args.size == 1) return listOf("debug", "ignore", "reload").filter(args[0])
        if (args.size == 2) {
            if (args[0] == "debug") {
                return listOf("add", "remove").filter(args[1])
            } else if (args[0] == "ignore") {
                return plugin.server.allPlayers.map { it.username }.filter(args[1])
            }
        }
        if (args.size == 3) {
            if (args[0] == "debug") {
                if (args[1] == "add" || args[1] == "remove") {
                    return plugin.server.allPlayers.map { it.username }.filter(args[2])
                }
            }
        }
        if (args.size == 4) {
            if (args[0] == "debug") {
                if (args[1] == "add") {
                    return plugin.server.allServers.map { it.serverInfo.name }.filter(args[3])
                }
            }
        }
        return emptyList()
    }

    override fun hasPermission(invocation: Invocation): Boolean = invocation.source().hasPermission("gotoafk.command.gta")

    private fun List<String>.filter(s: String): List<String> = distinct().filter { s1 -> s1.lowercase().startsWith(s.lowercase()) }
}
