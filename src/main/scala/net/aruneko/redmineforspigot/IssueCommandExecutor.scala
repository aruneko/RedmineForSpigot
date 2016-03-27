package net.aruneko.redmineforspigot

import dispatch._
import dispatch.Defaults._
import org.bukkit.ChatColor
import org.bukkit.command.{Command, CommandExecutor, CommandSender, TabCompleter}

import collection.JavaConverters._
import scala.util.{Failure, Success}

/**
  * Redmineチケットに関するコマンドを実行するクラス
  */
class IssueCommandExecutor(config: Configuration) extends CommandExecutor with TabCompleter {
  /**
    * コマンドを実行するメソッド
    *
    * @param sender コマンド送信者
    * @param cmd    送信されたコマンド
    * @param label  コマンドのラベル
    * @param args   コマンドの引数
    * @return コマンドを実行した場合true、そうでなければfalse
    */
  override def onCommand(sender: CommandSender, cmd: Command, label: String, args: Array[String]): Boolean = {
    if (Utils.canExecCommand(sender, config)) {
      execCommand(sender, args)
    } else {
      true
    }
  }

  /**
    * 実行するコマンドの振り分け
    * @param sender
    * @param args
    * @return
    */
  def execCommand(sender: CommandSender, args: Array[String]): Boolean = {
    args match {
      case Array(arg) if arg.equalsIgnoreCase("list") =>
        printIssueList(sender)
      case Array(num) if Utils.stringToInt(num).isDefined =>
        issueDetails(sender, Utils.stringToInt(num).get)
      case Array(arg, num) if arg.equalsIgnoreCase("list") && Utils.stringToInt(num).isDefined =>
        issueListByProjectId(sender, Utils.stringToInt(num).get)
      case Array(arg, _, _, _, _*) if arg.equalsIgnoreCase("time") && checkTimeCommandArgs(args) =>
        createNewTimeEntry(sender, args)
      case Array(arg, _, _, _, _, _*) if arg.equalsIgnoreCase("new") && checkNewCommandArgs(args) =>
        pushNewIssue(sender, args)
      case _ => false
    }
  }

  /**
    * Tab補完の実装
    * @param sender
    * @param cmd
    * @param alias
    * @param args
    * @return 補完候補
    */
  override def onTabComplete(sender: CommandSender, cmd: Command, alias: String, args: Array[String]): java.util.List[String] = {
    args.length match {
      case 1 if args(0).length == 0 => List("list", "time", "new").asJava
      case 1 if "list".startsWith(args(0)) => List("list").asJava
      case 1 if "new".startsWith(args(0)) => List("new").asJava
      case 1 if "time".startsWith(args(0)) => List("time").asJava
      case _ => List("").asJava
    }
  }

  /**
    * チケット一覧を取得するコマンド
    *
    * @param sender コマンド送信者
    * @return
    */
  def printIssueList(sender: CommandSender): Boolean = {
    // XMLを取得
    val fetchedXML = Utils.fetchXML(config.url + "issues.xml?key=" + config.getApiKey(sender))

    fetchedXML match {
      case None =>
        // 取得に失敗した旨を表示
        sender.sendMessage("Issues not found.")
      case Some(xml) =>
        // メッセージの送信
        sender.sendMessage(ChatColor.AQUA + "===== Issues List =====")
        sender.sendMessage("Project ID : Issue ID : Issue Subject")

        // すべてのチケットを表示
        val issues = xml \\ "issue"
        issues foreach {
          issue => {
            val issueId = issue \ "id"
            val pid = issue \ "project" \ "@id"
            val subject = issue \ "subject"
            sender.sendMessage(pid.text + " : " + issueId.text + " : " + subject.text)
          }
        }
    }
    true
  }

  /**
    * 指定したプロジェクトIDのチケット一覧を取得するコマンド
    *
    * @param sender コマンド送信者
    * @param projectId プロジェクトID
    * @return
    */
  def issueListByProjectId(sender: CommandSender, projectId: Int): Boolean = {
    // XMLを取得
    val fetchedXML = Utils.fetchXML(config.url + "issues.xml?project_id=" + projectId + "&key=" + config.getApiKey(sender))

    fetchedXML match {
      case None =>
        // 取得に失敗した旨を表示
        sender.sendMessage("Issues not found.")
      case Some(xml) =>
        // メッセージの送信
        sender.sendMessage(ChatColor.AQUA + "===== Issues List =====")
        sender.sendMessage("Issue ID : Issue Subject")

        // 該当プロジェクトのすべてのチケットを表示
        val issues = xml \\ "issue"
        issues foreach {
          issue => {
            val issueId = issue \ "id"
            val subject = issue \ "subject"
            sender.sendMessage(issueId.text + " : " + subject.text)
          }
        }
    }
    true
  }

  /**
    * チケットの詳細を表示するコマンド
    *
    * @param sender コマンド送信者
    * @param issueId チケットID
    * @return
    */
  def issueDetails(sender: CommandSender, issueId: Int): Boolean = {
    // XMLを取得
    val fetchedXML = Utils.fetchXML(config.url + "issues/" + issueId + ".xml?key=" + config.getApiKey(sender))

    fetchedXML match {
      case None =>
        // 取得に失敗した旨を表示
        sender.sendMessage("Issue ID " + issueId + " is not found.")
      case Some(xml) =>
        // パーツの分解
        val issue = xml \\ "issue"
        val project = issue \ "project" \ "@name"
        val tracker = issue \ "tracker" \ "@name"
        val status = issue \ "status" \ "@name"
        val priority = issue \ "priority" \ "@name"
        val subject = issue \ "subject"
        val description = issue \ "description"
        val startDate = issue \ "start_date"
        val dueDate = issue \ "due_date"
        val doneRatio = issue \ "done_ratio"

        // チケットの詳細を表示
        sender.sendMessage(ChatColor.AQUA + "===== Details of Issue \"" + subject.text + "\" =====")
        sender.sendMessage("Issue ID : " + issueId)
        sender.sendMessage("Project name : " + project.text)
        sender.sendMessage("Tracker : " + tracker.text)
        sender.sendMessage("Status : " + status.text)
        sender.sendMessage("Priority : " + priority.text)
        sender.sendMessage("Description : " + description.text)
        sender.sendMessage("Start date : " + startDate.text)
        sender.sendMessage("Due date : " + dueDate.text)
        sender.sendMessage("Done ratio : " + doneRatio.text + "%")
    }
    true
  }

  /**
    * 新規作成モードの引数の妥当性をチェック
    *
    * @param args チェックする引数
    * @return
    */
  def checkNewCommandArgs(args: Array[String]): Boolean = {
    args match {
      case Array(_, projectId, _*) if Utils.stringToInt(projectId).isEmpty => false
      case Array(_, _, trackerId, _*) if Utils.stringToInt(trackerId).isEmpty => false
      case Array(_, _, _, priorityId, _*) if Utils.stringToInt(priorityId).isEmpty => false
      case _ => true
    }
  }

  /**
    * 新たなチケットを発行するコマンド
    *
    * @param sender コマンド送信者
    * @param args 発行に必要な引数
    * @return
    */
  def pushNewIssue(sender: CommandSender, args: Array[String]): Boolean = {
    // 引数を整形
    val projectId = Utils.stringToInt(args(1)).get
    val trackerId = Utils.stringToInt(args(2)).get
    val priorityId = Utils.stringToInt(args(3)).get
    val subject = args.slice(4, args.length + 1).mkString(" ")

    // XMLの組み立て
    val reqXML =
      s"""<?xml version="1.0"?>
         |<issue>
         |  <project_id>$projectId</project_id>
         |  <tracker_id>$trackerId</tracker_id>
         |  <subject>$subject</subject>
         |  <priority_id>$priorityId</priority_id>
         |</issue>
       """.stripMargin

    // リクエストの組み立て
    val headers = Map("Content-type" -> "text/xml; charset=UTF-8", "X-Redmine-API-Key" -> config.getApiKey(sender))
    val reqUrl = url(config.url + "issues.xml") << reqXML <:< headers
    val res = Http(reqUrl OK as.String)

    // 結果に応じてメッセージを表示
    res.onComplete {
      case Success(_) => sender.sendMessage("Success!")
      case Failure(_) => sender.sendMessage("Failed to add an issue.")
    }
    true
  }

  /**
    * time引数の妥当性をチェック
    * @param args
    * @return
    */
  def checkTimeCommandArgs(args: Array[String]): Boolean = {
    args match {
      case Array(_, issueId, _*) if Utils.stringToInt(issueId).isEmpty => false
      case Array(_, _, hours, _*) if Utils.stringToDouble(hours).isEmpty => false
      case Array(_, _, _, activityId, _*) if Utils.stringToInt(activityId).isEmpty => false
      case _ => true
    }
  }

  /**
    * 作業時間を記録するコマンド
    * @param sender 実行したプレイヤー
    * @param args コマンドの引数
    * @return
    */
  def createNewTimeEntry(sender: CommandSender, args: Array[String]): Boolean = {
    // 引数を整形
    val issueId = Utils.stringToInt(args(1)).get
    val hours = Utils.stringToDouble(args(2)).get
    val activityId = Utils.stringToInt(args(3)).get
    val comments = if (args.length >= 5) {
      args.slice(4, args.length + 1).mkString(" ")
    } else {
      ""
    }

    // XMLの組み立て
    val reqXML =
      s"""<?xml version="1.0"?>
         |<time_entry>
         |  <issue_id>$issueId</issue_id>
         |  <hours>$hours</hours>
         |  <activity_id>$activityId</activity_id>
         |  <comments>$comments</comments>
         |</time_entry>
       """.stripMargin

    // リクエストの組み立て
    val headers = Map("Content-type" -> "text/xml; charset=UTF-8", "X-Redmine-API-Key" -> config.getApiKey(sender))
    val reqUrl = url(config.url + "time_entries.xml") << reqXML <:< headers
    val res = Http(reqUrl OK as.String)

    // 結果に応じてメッセージを表示
    res.onComplete {
      case Success(_) => sender.sendMessage("Success!")
      case Failure(_) => sender.sendMessage("Failed to add a time entry.")
    }
    true
  }
}
