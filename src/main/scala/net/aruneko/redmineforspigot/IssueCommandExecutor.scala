package net.aruneko.redmineforspigot

import dispatch._
import dispatch.Defaults._
import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import org.bukkit.entity.Player

/**
  * Redmineチケットに関するコマンドを実行するクラス
  */
class IssueCommandExecutor(config: Configuration) extends CommandExecutor {
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

    if (Utils.canPingRedmine(config.url) && isPlayer && args.length >= 0) {
      if (args.length == 1 && args(0).equalsIgnoreCase("list")) {
        // チケット一覧を引っ張ってくるコマンド
        issueList(sender)
      } else if (args.length == 1 && Utils.stringToInt(args(0)).isDefined) {
        // チケットの詳細を引っ張ってくるコマンド
        issueDetails(sender, Utils.stringToInt(args(0)).get)
      } else if (args.length == 2 && args(0).equalsIgnoreCase("list") && Utils.stringToInt(args(1)).isDefined) {
        // プロジェクトIDごとのチケット一覧を表示するコマンド
        issueListByProjectId(sender, Utils.stringToInt(args(1)).get)
      } else if (args.length >= 5 && args(0).equalsIgnoreCase("new") && checkNewCommandArgs(sender, args)) {
        // 新規チケットを発行するコマンド
        pushNewIssue(sender, args)
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
    * チケット一覧を取得するコマンド
    *
    * @param sender コマンド送信者
    * @return
    */
  def issueList(sender: CommandSender): Boolean = {
    // XMLを取得
    val fetchedXML = Utils.fetchXML(config.url + "issues.xml?key=" + config.getApiKey(sender))

    if (fetchedXML.isEmpty) {
      // 取得に失敗した旨を表示して終わる
      sender.sendMessage("Issues not found.")
    } else {
      // チケット一覧の取得
      val issues = fetchedXML.get \\ "issue"

      // メッセージの送信
      sender.sendMessage("===== Issues List =====")
      sender.sendMessage("Project ID : Issue ID : Issue Subject")

      // すべてのチケットを表示
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

    if (fetchedXML.isEmpty) {
      // 取得に失敗した旨を表示して終わる
      sender.sendMessage("Issues not found.")
    } else {
      // チケット一覧の取得
      val issues = fetchedXML.get \\ "issue"

      // メッセージの送信
      sender.sendMessage("===== Issues List =====")
      sender.sendMessage("Issue ID : Issue Subject")

      // 該当プロジェクトのすべてのチケットを表示
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

    if (fetchedXML.isEmpty) {
      // 取得に失敗した旨を表示して終わる
      sender.sendMessage("Issue ID " + issueId + " is not found.")
    } else {
      // チケットの取得
      val issue = fetchedXML.get \\ "issue"

      // パーツの分解
      val project = issue \ "project" \ "@name"
      val tracker = issue \ "tracker" \ "@name"
      val status = issue \ "status" \ "@name"
      val priority = issue \ "priority" \ "@name"
      val subject = issue \ "subject"
      val description = issue \ "description"
      val startDate = issue \ "start_date"
      val dueDate = issue \ "due_date"
      val doneRatio = issue \ "done_ratio"

      // メッセージの送信
      sender.sendMessage("===== Details of Issue \"" + subject.text + "\" =====")
      sender.sendMessage("Issue ID: " + issueId)
      sender.sendMessage("Project name: " + project.text)
      sender.sendMessage("Tracker: " + tracker.text)
      sender.sendMessage("Status: " + status.text)
      sender.sendMessage("Priority: " + priority.text)
      sender.sendMessage("Description: " + description.text)
      sender.sendMessage("Start date: " + startDate.text)
      sender.sendMessage("Due date: " + dueDate.text)
      sender.sendMessage("Done ratio: " + doneRatio.text + "%")
    }
    true
  }

  /**
    * 新規作成モードの引数の妥当性をチェック
    *
    * @param args チェックする引数
    * @return
    */
  def checkNewCommandArgs(sender: CommandSender, args: Array[String]): Boolean = {
    // 各種引数を整形
    val projectId = Utils.stringToInt(args(1))
    val trackerId = Utils.stringToInt(args(2))
    val priorityId = Utils.stringToInt(args(3))

    val hasEmptyArgs = projectId.isEmpty || trackerId.isEmpty || priorityId.isEmpty

    if (hasEmptyArgs) {
      // 数値以外が混入したら蹴る
      false
    } else {
      // projectIdのはみ出しチェック
      // API叩いて一覧を取得
      val fetchedXML = Utils.fetchXML(config.url + "projects.xml?key=" + config.getApiKey(sender))

      if (fetchedXML.isEmpty) {
        // 取得に失敗したら落とす
        false
      } else {
        // プロジェクトID一覧を生成する
        val projectIDs = (fetchedXML.get \\ "id").map(i => i.text)

        // 範囲外のIDをチェック
        val hasWrongArgs = !projectIDs.contains(projectId.toString) || (1 to 3).contains(trackerId.get) || (1 to 5).contains(priorityId.get)

        if (!hasWrongArgs) {
          // 範囲外のIDが指定されていた場合も終了
          false
        } else {
          // 検査を通ったときだけ実行
          true
        }
      }

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
    // 各種引数を整形
    val projectId = Utils.stringToInt(args(1)).get
    val trackerId = Utils.stringToInt(args(2)).get
    val priorityId = Utils.stringToInt(args(3)).get
    val subject = args.slice(4, args.length + 1).mkString(" ")

    val reqXML =
      s"""<?xml version="1.0"?>
          |<issue>
          |  <project_id>$projectId</project_id>
          |  <tracker_id>$trackerId</tracker_id>
          |  <subject>$subject</subject>
          |  <priority_id>$priorityId</priority_id>
          |</issue>
        """.stripMargin

    val headers = Map("Content-type" -> "text/xml", "X-Redmine-API-Key" -> config.getApiKey(sender))
    val reqUrl = url(config.url + "issues.xml") << reqXML <:< headers
    val res = Http(reqUrl OK as.String).apply()

    if (res.isEmpty) {
      sender.sendMessage("Failed to add new issue.")
      true
    } else {
      true
    }
  }
}
