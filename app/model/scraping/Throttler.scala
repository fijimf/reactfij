package model.scraping

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.Future
import scala.reflect.ClassTag

class Throttler[S,T:ClassTag](val f: (S => Future[T])) {
  import akka.pattern.ask
  val ref: ActorRef = ActorSystem("my-system").actorOf(Props(classOf[ThrottlingActor[S, T]], f))
  implicit val timeout = Timeout(5,TimeUnit.MINUTES)
  def apply(s:S):Future[T] = {
    (ref ? s).mapTo[T]
  }

}
