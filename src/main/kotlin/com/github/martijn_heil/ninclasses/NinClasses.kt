package com.github.martijn_heil.ninclasses

import org.bukkit.OfflinePlayer
import org.bukkit.plugin.java.JavaPlugin
import java.lang.IllegalStateException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

private val cache = HashMap<UUID, PlayerClass?>()

class NinClasses : JavaPlugin() {
    val classes = HashMap<String, PlayerClass>()
    var defaultClass: PlayerClass? = null

    override fun onEnable() {
        instance = this
        logger.fine("Saving default config..")
        saveDefaultConfig()

        logger.info("Migrating database if needed..")
        dbUrl = config.getString("db.url")
        dbUsername = config.getString("db.username")
        dbPassword = config.getString("db.password")
        // Storing the password in a char array doesn't improve much..
        // it's stored in plaintext in the "config" object anyway.. :/

        // This is a hack, we use a custom classloader to replace Flyway's VersionPrinter class with
        // Our custom version of that class, which is located in the resources folder.
        // The main reason for this is that with Bukkit, Flyway was having classpath issues determining it's version.
        // Due to that the whole plugin crashed on startup.
        val hackyLoader = HackyClassLoader(this.classLoader)
        val cls = hackyLoader.loadClass("tk.martijn_heil.wac_core.HackyClass")
        val migrationResult = cls!!.getMethod("doStuff", String::class.java, String::class.java, String::class.java, ClassLoader::class.java).invoke(null, dbUrl, dbUsername, dbPassword, this.classLoader) as Boolean
        if(!migrationResult) {
            this.isEnabled = false
            return
        }

        try {
            dbconn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
        } catch (ex: SQLException) {
            logger.severe(ex.message)
            logger.severe("Disabling plugin due to database error..")
            this.isEnabled = false
            return
        }
    }

    fun registerClass(identifier: String, playerClass: PlayerClass, isDefault: Boolean = false) {
        classes[identifier] = playerClass
        if(isDefault) defaultClass = playerClass
    }

    companion object {
        lateinit var instance: NinClasses
            private set
        private lateinit var dbUrl: String
        private lateinit var dbUsername: String
        private lateinit var dbPassword: String

        var dbconn: Connection? = null
            get() {
                if(field!!.isClosed) field = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
                return field
            }
    }
}

var OfflinePlayer.playerClass: PlayerClass?
    get() {
        val tmp = cache[this.uniqueId]
        if(tmp != null) {
            return tmp
        }
        else {
            val stmnt = NinClasses.dbconn!!.prepareStatement("SELECT 1 FROM wac_core_players WHERE uuid=?")
            stmnt.setString(1, this.uniqueId.toCompressedString())
            val result = stmnt.executeQuery()
            if(!result.next()) throw IllegalStateException("Player(uuid: '" + this.uniqueId.toString() + "') not found in database.")
            val playerClassId = result.getString("player_class")
            if(playerClassId == null) { stmnt.close(); return NinClasses.instance.defaultClass }
            val playerClass = NinClasses.instance.classes[playerClassId]
            if(playerClass == null) {
                stmnt.close();
                this.playerClass = NinClasses.instance.defaultClass;
                return NinClasses.instance.defaultClass
            }
            cache[this.uniqueId] = playerClass
            return playerClass
        }
    }
    set(value) { cache[this.uniqueId] = value }