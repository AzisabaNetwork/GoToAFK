package net.azisaba.goToAfk.util

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo

data class ServerInfoResolvable(val name: String) {
    companion object {
        fun ServerInfo.toResolvable() = ServerInfoResolvable(name)
    }

    fun get() = ProxyServer.getInstance().getServerInfo(name) ?: error("No such server '$name'")
}
