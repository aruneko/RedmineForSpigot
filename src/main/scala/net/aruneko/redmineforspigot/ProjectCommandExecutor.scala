package net.aruneko.redmineforspigot

import org.bukkit.ChatColor
import org.bukkit.command.{Command, CommandExecutor, CommandSender, TabCompleter}

import collection.JavaConverters._

/**
  * Redmineプロジェクトに関連するコマンドを実行するクラス
  */
class ProjectCommandExecutor(config: Configuration) extends CommandExecutor with TabCompleter {
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
    Utils.canExecCommand(sender, config) match {
      case Right(a) => execCommand(sender, args)
      case Left(e) =>
        sender.sendMessage(e)
        true
    }
  }

  /**
    * 実行するコマンドの振り分け
    * @param sender コマンド送信者
    * @param args コマンドの引数
    * @return
    */
  def execCommand(sender: CommandSender, args: Array[String]): Boolean = {
    args match {
      case Array(arg) if arg.equalsIgnoreCase("list") =>
        projectList(sender)
      case Array(num) if Utils.stringToInt(num).isDefined =>
        projectDetails(sender, Utils.stringToInt(num).get)
      case _ => false
    }
  }

  /**
    * Tab補完の実装
    * @param sender コマンド送信者
    * @param cmd 送信されたコマンド
    * @param alias コマンドの別名
    * @param args コマンドの引数
    * @return 補完候補
    */
  override def onTabComplete(sender: CommandSender, cmd: Command, alias: String, args: Array[String]): java.util.List[String] = {
    args.length match {
      case 1 if args(0).length == 0 => List("list").asJava
      case 1 if "list".startsWith(args(0)) => List("list").asJava
      case _ => List("").asJava
    }
  }

  /**
    * プロジェクトの一覧を簡易表示する
    *
    * @param sender コマンド送信者
    * @return
    */
  def projectList(sender: CommandSender): Boolean = {
    Utils.fetchXmlByApiKey(sender, config, "projects.xml") match {
      case Left(e) =>
        // 取得に失敗した旨を表示
        sender.sendMessage(e)
      case Right(xml) =>
        // メッセージの送信
        sender.sendMessage(ChatColor.AQUA + "===== Projects List =====")
        sender.sendMessage("Project ID : Project Name")

        // プロジェクトごとにIDと名前を表示
        val projects = xml \\ "project"
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
    Utils.fetchXmlByApiKey(sender, config, "projects/" + projectId + ".xml") match {
      case Left(e) =>
        // 取得に失敗した旨を表示
        sender.sendMessage(e)
      case Right(xml) =>
        // パーツの分解
        val project = xml \\ "project"
        val name = project \ "name"
        val identifier = project \ "identifier"
        val description = project \ "description"
        val homepage = project \ "homepage"
        val status = project \ "status"
        val createdOn = project \ "created_on"
        val updatedOn = project \ "updated_on"

        // プロジェクトの詳細を表示
        sender.sendMessage(ChatColor.AQUA + "===== Details of project \"" + name.text + "\" =====")
        sender.sendMessage("ID : " + projectId)
        sender.sendMessage("Identifier : " + identifier.text)
        sender.sendMessage("Description : " + description.text)
        sender.sendMessage("Homepage : " + homepage.text)
        sender.sendMessage("Status : " + status.text)
        sender.sendMessage("Created on : " + createdOn.text)
        sender.sendMessage("Updated on : " + updatedOn.text)
    }
    true
  }
}
