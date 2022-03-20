package net.azisaba.goToAfk

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import net.azisaba.goToAfk.listener.ClearOnQuitOrServerChangeListener
import net.azisaba.goToAfk.listener.RegisterOnKickListener
import net.azisaba.goToAfk.util.ServerInfoResolvable
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GoToAFK @Inject constructor(val server: ProxyServer, val logger: Logger) {
    companion object {
        val serversMap = mutableMapOf<UUID, ServerInfoResolvable>()
        val plsCheck = mutableListOf<UUID>()
        val onlineServers = mutableSetOf<String>()
        val ignoreList = mutableListOf<String>()
        lateinit var instance: GoToAFK
        private var pingerTask: ScheduledTask? = null
    }

    private var number = 0L
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(32) {
        Thread(it).apply { name = "GTA Server Pinger #${++number}" }.apply { isDaemon = true }
    }

    init {
        instance = this
        ReloadableGTAConfig.reload()
    }

    @Subscribe
    fun onProxyInitialization(e: ProxyInitializeEvent) {
        server.commandManager.register("gta", GTACommand(this), "gotoafk")
        server.eventManager.register(this, RegisterOnKickListener)
        server.eventManager.register(this, ClearOnQuitOrServerChangeListener)
        startPingerThread()
        server.scheduler.buildTask(this) {
            val toRemove = mutableListOf<UUID>()
            plsCheck.forEach { uuid ->
                val player = server.getPlayer(uuid)
                if (!player.isPresent) {
                    toRemove.add(uuid)
                    return@forEach
                }
                val name = serversMap[uuid]?.name
                if (name == null) {
                    toRemove.add(uuid)
                    return@forEach
                }
                if (onlineServers.contains(name)) {
                    player.get().createConnectionRequest(server.getServer(name).get()).fireAndForget()
                }
            }
            plsCheck.removeAll(toRemove)
        }.delay(5L, TimeUnit.SECONDS).repeat(5L, TimeUnit.SECONDS).schedule()
    }

    fun startPingerThread() {
        pingerTask?.cancel()
        pingerTask = server.scheduler.buildTask(this) {
            var count = 0
            @Suppress("DEPRECATION") // not deprecated in bungeecord
            server.allServers.forEach { server ->
                if (ignoreList.contains(server.serverInfo.name.lowercase())) return@forEach
                executor.schedule({
                    server.ping().handleAsync { ping, throwable ->
                        if (ping == null || throwable != null) {
                            onlineServers.remove(server.serverInfo.name)
                            return@handleAsync
                        }
                        onlineServers.add(server.serverInfo.name)
                    }
                }, count++ * 10L, TimeUnit.MILLISECONDS)
            }
        }.repeat(ReloadableGTAConfig.checkEvery.toLong(), TimeUnit.SECONDS).schedule()
        logger.info("Reloaded pinger task")
    }
}
