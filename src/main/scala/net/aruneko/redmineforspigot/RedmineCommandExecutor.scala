package net.aruneko.redmineforspigot

import org.bukkit.ChatColor
import org.bukkit.command.{Command, CommandExecutor, CommandSender, TabCompleter}

import collection.JavaConverters._

/**
  * Created by aruneko on 16/03/18.
  */
class RedmineCommandExecutor(config: Configuration) extends CommandExecutor with TabCompleter {
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
      case Array(arg, _) if arg.equalsIgnoreCase("setapikey") => config.setApiKey(sender, args(1))
      case Array(arg) if arg.equalsIgnoreCase("actid") => printActivityId(sender)
      case Array(arg) if arg.equalsIgnoreCase("trackid") => printTrackerId(sender)
      case Array(arg) if arg.equalsIgnoreCase("priorid") => printPriorityId(sender)
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
      case 1 if args(0).length == 0 => List("setapikey", "actid", "trackid", "priorid").asJava
      case 1 if "setapikey".startsWith(args(0)) => List("setapikey").asJava
      case 1 if "actid".startsWith(args(0)) => List("actid").asJava
      case 1 if "trackid".startsWith(args(0)) => List("trackid").asJava
      case 1 if "priorid".startsWith(args(0)) => List("priorid").asJava
      case _ => List("").asJava
    }
  }

  /**
    * Activity IDの一覧を表示するコマンド
    * @param sender
    * @return
    */
  def printActivityId(sender: CommandSender): Boolean = {
    val fetchedXML = Utils.fetchXML(config.url + "enumerations/TimeEntryActivities.xml?key=" + config.getApiKey(sender))

    fetchedXML match {
      case None =>
        // 取得に失敗した旨を表示
        sender.sendMessage("Failed to print activity_id")
      case Some(xml) =>
        // メッセージの送信
        sender.sendMessage(ChatColor.AQUA + "===== Activity ID List =====")
        sender.sendMessage("Activity ID : Activity Name")

        // Activity IDの一覧を表示
        val activities = xml \\ "time_entry_activity"
        activities foreach {
          act => {
            val actId = act \ "id"
            val actName = act \ "name"
            sender.sendMessage(actId.text + " : " + actName.text)
          }
        }
    }
    true
  }

  /**
    * Tracker IDの一覧を表示するコマンド
    * @param sender
    * @return
    */
  def printTrackerId(sender: CommandSender): Boolean = {
    val fetchedXML = Utils.fetchXML(config.url + "trackers.xml?key=" + config.getApiKey(sender))

    fetchedXML match {
      case None =>
        // 取得に失敗した旨を表示
        sender.sendMessage("Failed to print tracker_id")
      case Some(xml) =>
        // メッセージの送信
        sender.sendMessage(ChatColor.AQUA + "===== Tracker ID List =====")
        sender.sendMessage("Tracker ID : Tracker Name")

        // Tracker IDの一覧を表示
        val trackers = xml \\ "tracker"
        trackers foreach {
          tracker => {
            val trackerId = tracker \ "id"
            val trackerName = tracker \ "name"
            sender.sendMessage(trackerId.text + " : " + trackerName.text)
          }
        }
    }
    true
  }

  /**
    * Priority IDの一覧を表示するコマンド
    * @param sender
    * @return
    */
  def printPriorityId(sender: CommandSender): Boolean = {
    val fetchedXML = Utils.fetchXML(config.url + "enumerations/issue_priorities.xml?key=" + config.getApiKey(sender))

    fetchedXML match {
      case None =>
        // 取得に失敗した旨を表示
        sender.sendMessage("Failed to print priority_id")
      case Some(xml) =>
        // メッセージの送信
        sender.sendMessage(ChatColor.AQUA + "===== Priority ID List =====")
        sender.sendMessage("Priority ID : Priority Name")

        // Priority IDの一覧を表示
        val priorities = xml \\ "issue_priority"
        priorities foreach {
          priority => {
            val priorId = priority \ "id"
            val priorName = priority \ "name"
            sender.sendMessage(priorId.text + " : " + priorName.text)
          }
        }
    }
    true
  }
}
