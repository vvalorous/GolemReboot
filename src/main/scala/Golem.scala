package cc.hypo.golem

import cc.hypo.golem.bot.Bot
import cc.hypo.golem.bot.hipchat.Hipchat
import com.typesafe.config._
import scala.collection.JavaConverters._

import akka.actor._

object Golem extends App {
  val conf = ConfigFactory.load()
  
  val system = ActorSystem("Golem")

  if (conf.getString("node-type") == "master") {
    println("flowbot setup")
    val powerups = conf.getObjectList("powerups").asScala.map((powerupConfig: ConfigObject) => {
      val clazz = Class.forName(powerupConfig.get("class").unwrapped().asInstanceOf[String])
      val powerupName = powerupConfig.get("actorName").unwrapped().asInstanceOf[String]
      println(powerupName)
      system.actorOf(Props(clazz), powerupName)
    })
    
    println("hipchat settings")
    val hipchatUsername = conf.getString("hipchat-username")
    val hipchatPassword = conf.getString("hipchat-password")
    val hipchatNickname = conf.getString("hipchat-nickname")
    val hipchatAuthToken = conf.getString("hipchat-auth-token")
    val hipchatRooms = conf.getObjectList("hipchat-rooms").asScala
    
    println("bot setup")
    val bot = new Bot(hipchatUsername, hipchatPassword, hipchatNickname, Hipchat(hipchatAuthToken), system)
    
    println("hipchat room set")
    hipchatRooms.foreach((roomConfig: ConfigObject) =>
      bot.joinRoom(roomConfig.get("room").unwrapped.asInstanceOf[String],
        system.actorSelection(roomConfig.get("handler").unwrapped.asInstanceOf[String]))
    )

    println("last line Golem")
  }
}

