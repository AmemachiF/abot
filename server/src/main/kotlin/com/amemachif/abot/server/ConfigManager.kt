package com.amemachif.abot.server

import org.apache.commons.lang3.RandomStringUtils
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.FileInputStream

class ConfigManager private constructor() {
    val config: Config
    private val yaml = Yaml(Constructor(Config::class.java))

    init {
        try {
            val file = File(CONFIG_FILENAME)
            if (!file.exists() || file.length() == 0L) {
                file.createNewFile()
                writeConfig(file, yaml.dumpAsMap(Config()))
            }
            val config: Config = yaml.load(FileInputStream(file))
            writeConfig(file, yaml.dumpAsMap(config))
            config
        } catch (e: Exception) {
            e.printStackTrace()
            Config()
        }.also { config = it }
    }

    companion object {
        const val CONFIG_FILENAME = "config.yaml"

        val INSTANCE = ConfigManager()

        private fun writeConfig(file: File, config: String) {
            file.writer().apply {
                write(config)
                flush()
                close()
            }
        }

        class Config {
            var port: Int = 50000
            var authKey: String = RandomStringUtils.randomAlphanumeric(20, 30)
            var manageKey: String = RandomStringUtils.randomAlphanumeric(30, 40)
        }
    }
}
