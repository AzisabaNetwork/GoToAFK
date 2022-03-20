package net.azisaba.goToAfk.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.KickedFromServerEvent.RedirectPlayer
import net.azisaba.goToAfk.GoToAFK
import net.azisaba.goToAfk.ReloadableGTAConfig
import net.azisaba.goToAfk.util.ServerInfoResolvable.Companion.toResolvable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import java.util.concurrent.TimeUnit

object RegisterOnKickListener {
    @Subscribe
    fun onServerKick(e: KickedFromServerEvent) {
        if (e.kickedDuringServerConnect()) return
        if (!e.serverKickReason.isPresent) return
        val reasonWithoutColor = LegacyComponentSerializer.legacySection()
            .serialize(e.serverKickReason.get())
            .replace("(?i)\\u00a7[0-9a-filmnox]".toRegex(), "")
        if (!reasonWithoutColor.matches(ReloadableGTAConfig.pattern)) return
        if (!e.server.serverInfo.name.lowercase().startsWith("afk")) {
            GoToAFK.serversMap[e.player.uniqueId] = e.server.toResolvable()
            GoToAFK.instance.server.scheduler.buildTask(GoToAFK.instance) {
                if (GoToAFK.serversMap.containsKey(e.player.uniqueId)) {
                    GoToAFK.plsCheck.add(e.player.uniqueId)
                }
            }.delay(ReloadableGTAConfig.wait.toLong(), TimeUnit.SECONDS).schedule()
        }
        e.result = RedirectPlayer.create(
            GoToAFK.onlineServers
                .filter { it != e.server.serverInfo.name && it.startsWith("afk") }
                .randomOrNull()
                ?.let { GoToAFK.instance.server.getServer(it).orElse(null) }
                ?: return
        )
        GoToAFK.instance.server.scheduler.buildTask(GoToAFK.instance) {
            if (e.player.isActive) {
                e.player.sendMessage(Component.text("サーバーからキックされました:", NamedTextColor.GOLD))
                e.player.sendMessage(e.serverKickReason.get())
                e.player.showTitle(
                    Title.title(
                        Component.text("サーバー再起動中です", NamedTextColor.GOLD),
                        Component.text("再起動が完了すると自動的にサーバーに接続されます。"),
                    )
                )
                e.player.sendActionBar(Component.text("サーバーを手動で切り替えた場合はキャンセルされます。", NamedTextColor.GOLD))
            }
        }.delay(1, TimeUnit.SECONDS).schedule()
    }
}
