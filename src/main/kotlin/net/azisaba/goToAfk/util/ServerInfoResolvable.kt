package net.azisaba.goToAfk.util

import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import net.azisaba.goToAfk.GoToAFK

data class ServerInfoResolvable(val name: String) {
    companion object {
        fun RegisteredServer.toResolvable() = ServerInfoResolvable(this.serverInfo.name)

        fun ServerInfo.toResolvable() = ServerInfoResolvable(this.name)
    }

    fun get(): RegisteredServer = GoToAFK.instance.server.getServer(name).orElseThrow { IllegalArgumentException("No such server '$name'") }
}
