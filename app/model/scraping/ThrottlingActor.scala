package model.scraping

import akka.actor.{ActorRef, Actor}
import play.api.Logger

import scala.concurrent.Future
import scala.util.Failure

class ThrottlingActor[S, T](val f: S => Future[T]) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  case object SubmitFromQueue

  private var outstandingRequests = 0
  private var pendingRequests = List.empty[(ActorRef, S)]

  override def receive: Actor.Receive = {
    case SubmitFromQueue =>
      outstandingRequests = outstandingRequests - 1
      pendingRequests match {
        case (s, u) :: rest =>
          pendingRequests = rest
          outstandingRequests = outstandingRequests + 1
          submitRequest(u, s)
        case Nil =>
      }
    case s: S =>
      if (outstandingRequests < 10) {
        outstandingRequests = outstandingRequests + 1
        submitRequest(s, sender())
      } else {
        queueRequest(s, sender())
      }
    case _ =>
  }

  def submitRequest(s: S, sender: ActorRef) {
    val future: Future[T] = f(s)
    future.onComplete({
      case scala.util.Success(t) =>
        sender ! t
        self ! SubmitFromQueue
      case Failure(exception) =>
        self ! SubmitFromQueue
        throw new RuntimeException(exception)
    })
  }

  def queueRequest(s: S, sender: ActorRef): Unit = {
    pendingRequests ::=(sender, s)
  }
}
