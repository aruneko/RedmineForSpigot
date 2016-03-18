package net.aruneko.redmineforspigot

import org.bukkit.command.{CommandSender, Command, CommandExecutor}
import org.bukkit.entity.Player

/**
  * Redmineプロジェクトに関連するコマンドを実行するクラス
  */
class ProjectCommandExecutor(config: Configuration) extends CommandExecutor {
  /**
    * 実際にコマンドを実行するメソッド
    *
    * @param sender コマンド送信者
    * @param cmd    送信されたコマンド
    * @param label  コマンドのラベル
    * @param args   コマンドの引数
    * @return コマンドを実行した場合true、そうでなければfalse
    */
  override def onCommand(sender: CommandSender, cmd: Command, label: String, args: Array[String]): Boolean = {
    // プレイヤーかどうか
    val isPlayer = sender.isInstanceOf[Player]

    if (isPlayer && args.length == 1) {
      // プレイヤーが実行していてかつ引数が1つの場合
      if (args(0).equalsIgnoreCase("list")) {
        // プロジェクト一覧を引っ張ってくるコマンド
        projectList(sender)
      } else if (Utils.stringToInt(args(0)).isDefined) {
        // プロジェクトの詳細を引っ張ってくるコマンド
        projectDetails(sender, Utils.stringToInt(args(0)).get)
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
      // 想定されていないケースの場合
      false
    }
  }

  /**
    * プロジェクトの一覧を簡易表示する
    *
    * @param sender コマンド送信者
    * @return
    */
  def projectList(sender: CommandSender): Boolean = {
    // XMLの取得
    val fetchedXML = Utils.fetchXML(config.url + "projects.xml?key=" + config.getApiKey(sender))

    if (fetchedXML.isEmpty) {
      // 取得に失敗した旨を表示して終わる
      sender.sendMessage("Projects not found.")
    } else {
      // ここからはXMLの取得に成功した場合
      // プロジェクトの一覧を取得
      val projects = fetchedXML.get \\ "project"

      // メッセージの送信
      sender.sendMessage("===== Projects List =====")
      sender.sendMessage("Project ID : Project Name")

      // プロジェクトごとにIDと名前を表示
      projects foreach {
        project => {
          val issueId = project \ "id"
          val name = project \ "name"
          sender.sendMessage(issueId.text + " : " + name.text)
        }
      }
    }
    true
  }

  /**
    * プロジェクトの詳細を表示するコマンド
    * @param sender コマンド送信者
    * @param projectId プロジェクトID
    * @return
    */
  def projectDetails(sender: CommandSender, projectId: Int): Boolean = {
    // XMLの取得
    val fetchedXML = Utils.fetchXML(config.url + "projects/" + projectId + ".xml?key=" + config.getApiKey(sender))

    if (fetchedXML.isEmpty) {
      // 取得に失敗した旨を表示して終わる
      sender.sendMessage("Project ID " + projectId + " is not found.")
    } else {
      // ここからはXMLの取得に成功した場合
      // プロジェクトの詳細を取得
      val project = fetchedXML.get \\ "project"

      // パーツの分解
      val name = project \ "name"
      val identifier = project \ "identifier"
      val description = project \ "description"
      val homepage = project \ "homepage"
      val status = project \ "status"
      val createdOn = project \ "created_on"
      val updatedOn = project \ "updated_on"

      // メッセージの送信
      sender.sendMessage("===== Details of project \"" + name.text + "\" =====")
      sender.sendMessage("ID: " + projectId)
      sender.sendMessage("Identifier: " + identifier.text)
      sender.sendMessage("Description: " + description.text)
      sender.sendMessage("Homepage: " + homepage.text)
      sender.sendMessage("Status: " + status.text)
      sender.sendMessage("Created on: " + createdOn.text)
      sender.sendMessage("Updated on: " + updatedOn.text)
    }
    true
  }
}
