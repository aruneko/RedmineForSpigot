package net.aruneko.redmineforspigot

import java.net.URL

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import scala.util.control.Exception._
import scala.xml.{Elem, XML}
import dispatch._
import dispatch.Defaults._
import org.bukkit.ChatColor

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

  private def fetchXml(url: String): MyEither[String, Elem] = {
    Try{XML.load(new URL(url))} match {
      case Success(v) => Right(v)
      case Failure(e) => Left(ChatColor.RED + "Failed to fetch data.")
    }
  }

  def fetchXmlByApiKey(sender: CommandSender, config: Configuration, url: String): MyEither[String, Elem] = {
    val keyParam = if (url.contains("?")) "&key=" else "?key="
    config.getApiKey(sender) match {
      case Right(key) =>
        fetchXml(config.url + url + keyParam + key)
      case Left(e) =>
        sender.sendMessage(e)
        fetchXml(config.url + url)
    }
  }

  def canExecCommand(sender: CommandSender, config: Configuration): MyEither[String, Boolean] = {
    // コマンドを実行できるかどうかを判定
    val isPlayer = sender.isInstanceOf[Player]
    val canConnectRedmine = canPingRedmine(config.url)

    if (!isPlayer) {
      Left(ChatColor.RED + "Only player can execute this command.")
    } else if (!canConnectRedmine) {
      Left(ChatColor.RED + "Can't connect Redmine.")
    } else {
      Right(true)
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
