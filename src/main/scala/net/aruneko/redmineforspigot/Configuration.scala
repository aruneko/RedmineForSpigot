package net.aruneko.redmineforspigot

import java.nio.file.{Files, FileSystems}

import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

import scala.util.control.Exception._

/**
  * Created by aruneko on 16/03/12.
  */
class Configuration(p: JavaPlugin) {

  // 設定ファイルを入れるディレクトリが存在しない場合は作成する
  if (!p.getDataFolder.exists()) {
    p.getDataFolder.mkdirs()
  }

  // 設定ファイル名やそのパスを設定
  val configFileName = "config.yml"
  val configFilePath = FileSystems.getDefault.getPath(p.getDataFolder.toString, configFileName)

  // 設定ファイルが存在しなかったらデフォルトのをコピー
  if (!Files.exists(configFilePath)) {
    p.getLogger.info("config.yml is not found, creating.")
    p.saveDefaultConfig()
  } else {
    p.getLogger.info("config.yml is found, loading.")
  }

  // 設定ファイルの読み込み
  val conf = p.getConfig

  // RedmineURLの取得
  val url = conf.getString("url")

  def getApiKey(sender: CommandSender): String = {
    val apiKey = catching(classOf[NullPointerException]) opt conf.getString("apiKeys." + sender.getName)

    apiKey match {
      case None =>
        sender.sendMessage("NOTE: You don't set API Key.")
        ""
      case Some(key) =>
        key
    }
  }

  def setApiKey(sender: CommandSender, apiKey: String): Boolean = {
    if (apiKey.matches("[a-f0-9]{40}")) {
      conf.set("apiKeys." + sender.getName, apiKey)
      p.saveConfig
      sender.sendMessage("Saved configuration file.")
    } else {
      sender.sendMessage("Incorrect API Key format.")
    }
    true
  }
}
