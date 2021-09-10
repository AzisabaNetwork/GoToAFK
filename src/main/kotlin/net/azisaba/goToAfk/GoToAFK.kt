package net.azisaba.goToAfk

import net.azisaba.goToAfk.listener.ClearOnQuitOrServerChangeListener
import net.azisaba.goToAfk.listener.RegisterOnKickListener
import net.azisaba.goToAfk.util.ServerInfoResolvable
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GoToAFK: Plugin() {
    companion object {
        val serversMap = mutableMapOf<UUID, ServerInfoResolvable>()
        val plsCheck = mutableListOf<UUID>()
        val onlineServers = mutableSetOf<String>()
        lateinit var instance: GoToAFK
    }

    private var number = 0L
    private val executor: ExecutorService = Executors.newCachedThreadPool {
        Thread(it).apply { name = "GTA Server Pinger #${++number}" }.apply { isDaemon = true }
    }

    init {
        instance = this
        ReloadableGTAConfig.reload()
    }

    override fun onEnable() {
        proxy.pluginManager.registerListener(this, RegisterOnKickListener)
        proxy.pluginManager.registerListener(this, ClearOnQuitOrServerChangeListener)
        proxy.pluginManager.registerCommand(this, GTACommand)
        proxy.scheduler.schedule(this, {
            proxy.servers.forEach { (name, info) ->
                executor.execute {
                    info.ping { ping, throwable ->
                        if (ping == null || throwable != null) {
                            onlineServers.remove(name)
                            return@ping
                        }
                        onlineServers.add(name)
                    }
                }
            }
        }, 10L, 10L, TimeUnit.SECONDS)
        proxy.scheduler.schedule(this, {
            val toRemove = mutableListOf<UUID>()
            plsCheck.forEach { uuid ->
                val player = ProxyServer.getInstance().getPlayer(uuid)
                if (player == null) {
                    toRemove.add(uuid)
                    return@forEach
                }
                val name = serversMap[uuid]?.name
                if (name == null) {
                    toRemove.add(uuid)
                    return@forEach
                }
                if (onlineServers.contains(name)) {
                    player.connect(ProxyServer.getInstance().getServerInfo(name))
                }
            }
            plsCheck.removeAll(toRemove)
        }, 5L, 5L, TimeUnit.SECONDS)
    }
}
