package net.azisaba.goToAfk.listener

import net.azisaba.goToAfk.GoToAFK
import net.azisaba.goToAfk.ReloadableGTAConfig
import net.azisaba.goToAfk.util.ServerInfoResolvable.Companion.toResolvable
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ServerKickEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.util.concurrent.TimeUnit

object RegisterOnKickListener: Listener {
    @EventHandler
    fun onServerKick(e: ServerKickEvent) {
        if (!ChatColor.stripColor(BaseComponent.toLegacyText(*e.kickReasonComponent)).matches(ReloadableGTAConfig.pattern)) return
        if (!e.kickedFrom.name.lowercase().startsWith("afk")) {
            GoToAFK.serversMap[e.player.uniqueId] = e.kickedFrom.toResolvable()
            ProxyServer.getInstance().scheduler.schedule(GoToAFK.instance, {
                if (GoToAFK.serversMap.containsKey(e.player.uniqueId)) {
                    GoToAFK.plsCheck.add(e.player.uniqueId)
                }
            }, ReloadableGTAConfig.wait.toLong(), TimeUnit.SECONDS)
        }
        e.cancelServer = GoToAFK.onlineServers
            .filter { it != e.kickedFrom.name && it.startsWith("afk") }
            .randomOrNull()
            ?.let { ProxyServer.getInstance().getServerInfo(it) }
            ?: return
        e.isCancelled = true
        ProxyServer.getInstance().scheduler.schedule(GoToAFK.instance, {
            if (e.player.isConnected) {
                e.player.sendMessage(*TextComponent.fromLegacyText("${ChatColor.GOLD}サーバーからキックされました:"))
                e.player.sendMessage(*e.kickReasonComponent)
                val title = ProxyServer.getInstance().createTitle()
                title.title(*TextComponent.fromLegacyText("${ChatColor.GOLD}サーバー再起動中です"))
                title.subTitle(*TextComponent.fromLegacyText("再起動が完了すると自動的にサーバーに接続されます。"))
                e.player.sendTitle(title)
                e.player.sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText("${ChatColor.GOLD}サーバーを手動で切り替えた場合はキャンセルされます。"))
            }
        }, 1, TimeUnit.SECONDS)
    }
}
