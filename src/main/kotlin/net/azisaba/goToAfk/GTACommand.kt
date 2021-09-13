package net.azisaba.goToAfk

import net.azisaba.goToAfk.util.ServerInfoResolvable.Companion.toResolvable
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor

object GTACommand: Command("gta", "gotoafk.command.gta", "gotoafk"), TabExecutor {
    override fun execute(sender: CommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            return sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.RED}/gta <reload|ignore <server>|debug <add|remove>>"))
        }
        if (args[0] == "debug") {
            if (args[1] == "add") {
                if (args.size <= 3) return sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.RED}/gta debug add <player> <server>"))
                val player = ProxyServer.getInstance().getPlayer(args[2])
                    ?: return sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.RED}プレイヤーが見つかりません"))
                val server = ProxyServer.getInstance().getServerInfo(args[3])
                    ?: return sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.RED}サーバーが見つかりません"))
                GoToAFK.serversMap[player.uniqueId] = server.toResolvable()
                GoToAFK.plsCheck.add(player.uniqueId)
                sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.GREEN}:done:"))
            } else if (args[1] == "remove") {
                if (args.size <= 3) return sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.RED}/gta debug remove <player>"))
                val player = ProxyServer.getInstance().getPlayer(args[2])
                    ?: return sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.RED}プレイヤーが見つかりません"))
                GoToAFK.serversMap.remove(player.uniqueId)
                GoToAFK.plsCheck.remove(player.uniqueId)
                sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.GREEN}:done:"))
            }
        } else if (args[0] == "reload") {
            ReloadableGTAConfig.reload(sender)
            GoToAFK.instance.startPingerThread()
            sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.GREEN}設定を再読み込みしました。"))
        } else if (args[0] == "ignore") {
            if (args.size == 1) {
                return sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.RED}/gta ignore <server>"))
            }
            val server = ProxyServer.getInstance().getServerInfo(args[1])
                ?: return sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.RED}サーバーが見つかりません。"))
            if (GoToAFK.ignoreList.contains(server.name.lowercase())) {
                GoToAFK.ignoreList.remove(server.name.lowercase())
                sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.GREEN}サーバー'${server.name}'を除外リストから削除しました。"))
            } else {
                GoToAFK.ignoreList.add(server.name.lowercase())
                GoToAFK.onlineServers.remove(server.name.lowercase())
                sender.sendMessage(*TextComponent.fromLegacyText("${ChatColor.GREEN}サーバー'${server.name}'を除外リストに追加しました。"))
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): Iterable<String> {
        if (args.isEmpty()) return emptyList()
        if (args.size == 1) return listOf("debug", "ignore", "reload").filter(args[0])
        if (args.size == 2) {
            if (args[0] == "debug") {
                return listOf("add", "remove").filter(args[1])
            } else if (args[0] == "ignore") {
                return ProxyServer.getInstance().players.map { it.name }.filter(args[1])
            }
        }
        if (args.size == 3) {
            if (args[0] == "debug") {
                if (args[1] == "add" || args[1] == "remove") {
                    return ProxyServer.getInstance().players.map { it.name }.filter(args[2])
                }
            }
        }
        if (args.size == 4) {
            if (args[0] == "debug") {
                if (args[1] == "add") {
                    @Suppress("DEPRECATION")
                    return ProxyServer.getInstance().servers.values.map { it.name }.filter(args[3])
                }
            }
        }
        return emptyList()
    }

    private fun List<String>.filter(s: String): List<String> = distinct().filter { s1 -> s1.lowercase().startsWith(s.lowercase()) }
}
