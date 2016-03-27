package net.aruneko.redmineforspigot

import java.net.URL

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Success, Failure}
import scala.util.control.Exception._
import scala.xml.XML

import dispatch._
import dispatch.Defaults._

/**
  * Created by aruneko on 16/03/12.
  */
object Utils {
  def stringToInt(str: String): Option[Int] = {
    catching(classOf[NumberFormatException]) opt str.toInt
  }

  def stringToDouble(str: String): Option[Double] = {
    catching(classOf[NumberFormatException]) opt str.toDouble
  }

  def fetchXML(url: String) = {
    catching(classOf[Exception]) opt XML.load(new URL(url))
  }

  def canExecCommand(sender: CommandSender, config: Configuration): Boolean = {
    // コマンドを実行できるかどうかを判定
    val isPlayer = sender.isInstanceOf[Player]
    val canConnectRedmine = canPingRedmine(config.url)

    if (!isPlayer) {
      sender.sendMessage("Only player can execute this command.")
      false
    } else if (!canConnectRedmine) {
      sender.sendMessage("Can't connect Redmine.")
      false
    } else {
      true
    }
  }

  def canPingRedmine(redmineUrl: String): Boolean = {
    val ping = Http(url(redmineUrl) OK as.String)
    Await.ready(ping, Duration.Inf)
    ping.value.get match {
      case Success(_) => true
      case Failure(_) => false
    }
  }
}
