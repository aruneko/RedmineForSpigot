package net.aruneko.redmineforspigot

import org.bukkit.plugin.java.JavaPlugin

/**
  * Created by aruneko on 16/03/11.
  */
class Main extends JavaPlugin {
  override def onEnable(): Unit = {
    val config = new Configuration(this)

    getCommand("project").setExecutor(new ProjectCommandExecutor(config))
    getCommand("issue").setExecutor(new IssueCommandExecutor(config))
    getCommand("redmine").setExecutor(new RedmineCommandExecutor(config))
  }

  override def onDisable(): Unit = {}
}
