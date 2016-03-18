package net.aruneko.redmineforspigot

import java.nio.file.{Files, FileSystems}

import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

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

  def getApiKey(sender: CommandSender) = {
    if (conf.getString("apiKeys." + sender.getName).isEmpty) {
      // 見つからなかった注意書きを出す
      sender.sendMessage("NOTE: You don't set API Key.")
      ""
    } else {
      conf.getString("apiKeys." + sender.getName)
    }
  }

  def setApiKey(sender: CommandSender, apiKey: String) = {
    conf.set("apiKeys." + sender.getName, apiKey)
    p.saveConfig
    sender.sendMessage("Saved configuration file.")
  }
}
