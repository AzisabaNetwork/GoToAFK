package net.azisaba.goToAfk.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.azisaba.goToAfk.GoToAFK

object ClearOnQuitOrServerChangeListener {
    @Subscribe
    fun onPlayerDisconnect(e: DisconnectEvent) {
        GoToAFK.serversMap.remove(e.player.uniqueId)
        GoToAFK.plsCheck.remove(e.player.uniqueId)
    }

    @Subscribe
    fun onServerConnected(e: ServerConnectedEvent) {
        if (e.server.serverInfo.name.startsWith("afk")) return
        GoToAFK.serversMap.remove(e.player.uniqueId)
        GoToAFK.plsCheck.remove(e.player.uniqueId)
    }
}
