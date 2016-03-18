package net.aruneko.redmineforspigot

import java.io.FileNotFoundException
import java.net.URL

import org.bukkit.configuration.file.FileConfiguration

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
  def stringToInt(str: String) = {
    catching(classOf[NumberFormatException]) opt str.toInt
  }

  def fetchXML(url: String) = {
    catching(classOf[FileNotFoundException]) opt XML.load(new URL(url))
  }

  def canPingRedmine(redmineUrl: String) : Boolean = {
    val ping = Http(url(redmineUrl) OK as.String)
    Await.ready(ping, Duration.Inf)
    ping.value.get match {
      case Success(_) => true
      case Failure(_) => false
    }
  }
}
