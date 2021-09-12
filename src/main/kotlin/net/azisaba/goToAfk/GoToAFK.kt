package net.azisaba.goToAfk

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.EventLoopGroup
import net.azisaba.goToAfk.listener.ClearOnQuitOrServerChangeListener
import net.azisaba.goToAfk.listener.RegisterOnKickListener
import net.azisaba.goToAfk.util.ServerInfoResolvable
import net.azisaba.goToAfk.util.Util.doPing
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.netty.PipelineUtils
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.max

class GoToAFK: Plugin() {
    companion object {
        val serversMap = mutableMapOf<UUID, ServerInfoResolvable>()
        val plsCheck = mutableListOf<UUID>()
        val onlineServers = mutableSetOf<String>()
        val ignoreList = mutableListOf<String>()
        lateinit var instance: GoToAFK
        lateinit var eventLoopGroup: EventLoopGroup
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

    override fun onEnable() {
        eventLoopGroup = PipelineUtils.newEventLoopGroup(
            0,
            ThreadFactoryBuilder().setNameFormat("GTA Server Pinger IO Thread #%1\$d").build(),
        )
        proxy.pluginManager.registerListener(this, RegisterOnKickListener)
        proxy.pluginManager.registerListener(this, ClearOnQuitOrServerChangeListener)
        proxy.pluginManager.registerCommand(this, GTACommand)
        startPingerThread()
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

    override fun onDisable() {
        logger.info("Closing IO threads")
        eventLoopGroup.shutdownGracefully()
        eventLoopGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        logger.info("Goodbye!")
    }

    fun startPingerThread() {
        pingerTask?.cancel()
        pingerTask = proxy.scheduler.schedule(this, {
            var count = 0
            @Suppress("DEPRECATION") // not deprecated in bungeecord
            proxy.servers.forEach { (name, info) ->
                if (ignoreList.contains(name.lowercase())) return@forEach
                executor.schedule({
                    info.doPing { ping, throwable ->
                        if (ping == null || throwable != null) {
                            onlineServers.remove(name)
                            return@doPing
                        }
                        onlineServers.add(name)
                    }
                }, count++ * 10L, TimeUnit.MILLISECONDS)
            }
        }, 2L, ReloadableGTAConfig.checkEvery.toLong(), TimeUnit.SECONDS)
        logger.info("Reloaded pinger task")
    }
}
