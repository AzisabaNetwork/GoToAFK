package net.azisaba.goToAfk.util

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import net.azisaba.goToAfk.GoToAFK
import net.md_5.bungee.api.Callback
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.connection.PingHandler
import net.md_5.bungee.netty.HandlerBoss
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.ProtocolConstants

object Util {
    fun String.toRegexOr(regex: Regex): Regex {
        return try {
            this.toRegex()
        } catch (e: Exception) {
            regex
        }
    }

    fun ServerInfo.doPing(callback: Callback<ServerPing>) {
        val listener = ChannelFutureListener { future ->
            if (future.isSuccess) {
                val pv = ProtocolConstants.SUPPORTED_VERSION_IDS[ProtocolConstants.SUPPORTED_VERSION_IDS.size - 1]
                future.channel().pipeline().get(HandlerBoss::class.java).setHandler(PingHandler(this, callback, pv))
            } else {
                callback.done(null, future.cause())
            }
        }
        Bootstrap()
            .channel(PipelineUtils.getChannel(socketAddress))
            .handler(PipelineUtils.BASE)
            .group(GoToAFK.eventLoopGroup)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .remoteAddress(socketAddress)
            .connect()
            .addListener(listener)
    }
}
