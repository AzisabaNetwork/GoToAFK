package net.azisaba.goToAfk.listener

import net.azisaba.goToAfk.GoToAFK
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

object ClearOnQuitOrServerChangeListener: Listener {
    @EventHandler
    fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
        GoToAFK.serversMap.remove(e.player.uniqueId)
        GoToAFK.plsCheck.remove(e.player.uniqueId)
    }

    @EventHandler
    fun onServerConnected(e: ServerConnectedEvent) {
        if (e.server.info.name.startsWith("afk")) return
        GoToAFK.serversMap.remove(e.player.uniqueId)
        GoToAFK.plsCheck.remove(e.player.uniqueId)
    }
}
