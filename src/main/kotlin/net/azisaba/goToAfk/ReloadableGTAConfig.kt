package net.azisaba.goToAfk

import net.azisaba.goToAfk.util.Util.toRegexOr
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import org.intellij.lang.annotations.Language
import util.ResourceLocator
import util.base.Bytes
import util.yaml.YamlConfiguration
import util.yaml.YamlObject
import java.io.File
import kotlin.math.max
import kotlin.math.min

object ReloadableGTAConfig {
    private lateinit var cfg: YamlObject

    fun reload(sender: CommandSender? = null) {
        val dir = File("./plugins/GoToAFK")
        dir.mkdir()
        val file = File(dir, "config.yml")
        if (!file.exists()) {
            val input = ResourceLocator.getInstance(ReloadableGTAConfig::class.java).getResourceAsStream("/config.yml")
            if (input == null) {
                GoToAFK.instance.logger.severe("Could not find config.yml in jar file!")
            } else {
                Bytes.copy(input, file)
                GoToAFK.instance.logger.info("Generated default config.yml")
            }
        }
        cfg = YamlConfiguration(file).asObject()
        try {
            cfg.getString("pattern").toRegex()
        } catch (e: Exception) {
            GoToAFK.instance.logger.warning("Invalid pattern: ${cfg.getString("pattern")}")
            sender?.sendMessage(*TextComponent.fromLegacyText("Invalid pattern: ${cfg.getString("pattern")}"))
            e.printStackTrace()
        }
        if (cfg.getInt("wait") < 0) {
            GoToAFK.instance.logger.warning("Invalid wait time: ${cfg.getInt("wait", 60)}")
            sender?.sendMessage(*TextComponent.fromLegacyText("Invalid wait time: ${cfg.getInt("wait", 60)}"))
        }
        if (cfg.getInt("checkEvery") < 0) {
            GoToAFK.instance.logger.warning("Invalid checkEvery time: ${cfg.getInt("checkEvery", 10)}")
            sender?.sendMessage(*TextComponent.fromLegacyText("Invalid checkEvery time: ${cfg.getInt("checkEvery", 10)}"))
        }
        println("Reloaded config.yml:")
        println("  pattern: $pattern")
        println("  wait: $wait")
        println("  checkEvery: $checkEvery")
    }

    @Language("RegExp")
    private val defaultRegex = ".*(Server closed|restart).*"
    val pattern get() = cfg.getString("pattern", defaultRegex).toRegexOr(defaultRegex.toRegex())
    val wait get() = max(0, cfg.getInt("wait", 80))
    val checkEvery get() = min(300, max(0, cfg.getInt("checkEvery", 10)))
}
