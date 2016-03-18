package net.aruneko.redmineforspigot

import org.bukkit.command.{CommandExecutor, Command, CommandSender}
import org.bukkit.entity.Player

/**
  * Created by aruneko on 16/03/18.
  */
class RedmineCommandExecutor(config: Configuration) extends CommandExecutor {
  override def onCommand(sender: CommandSender, cmd: Command, label: String, args: Array[String]): Boolean = {
    // プレイヤーかどうか
    val isPlayer = sender.isInstanceOf[Player]

    if (Utils.canPingRedmine(config.url) && isPlayer && args.length >= 0) {
      if (args.length == 2 && args(0).equalsIgnoreCase("setApiKey")) {
        setApiKey(sender, args(1))
      } else {
        false
      }
    } else if (!Utils.canPingRedmine(config.url)) {
      // Redmineに接続できなかったとき
      sender.sendMessage("Can't connect Redmine.")
      true
    } else if (!isPlayer) {
      // サーバー側から実行された場合
      sender.sendMessage("Only player can execute this command.")
      true
    } else {
      false
    }
  }

  def setApiKey(sender: CommandSender, apiKey: String): Boolean = {
    if (apiKey.matches("[a-f0-9]{40}")) {
      // フォーマットが間違ってなかった場合にのみ実行
      config.setApiKey(sender, apiKey)
    } else {
      // 間違ってた旨を表示
      sender.sendMessage("Incorrect API Key format.")
    }
    true
  }
}
