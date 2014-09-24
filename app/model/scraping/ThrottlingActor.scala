package model.scraping

import akka.actor.{ActorRef, Actor}
import play.api.Logger

import scala.concurrent.Future
import scala.util.Failure

class ThrottlingActor[S, T](val f: S => Future[T], maxConcurrent:Int=10, retryAttempts:Int=5) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  case object SubmitFromQueue

  private var outstandingRequests = 0
  private var pendingRequests = List.empty[(ActorRef, S, Int)]

  override def receive: Actor.Receive = {
    case SubmitFromQueue =>
      outstandingRequests = outstandingRequests - 1
      pendingRequests match {
        case (s, u, n) :: rest =>
          pendingRequests = rest
          outstandingRequests = outstandingRequests + 1
          submitRequest(u, s, n)
        case Nil =>
      }
    case s: S =>
      if (outstandingRequests < 10) {
        outstandingRequests = outstandingRequests + 1
        submitRequest(s, sender(), 5)
      } else {
        queueRequest(s, sender(), 5)
      }
    case _ =>
  }

  def submitRequest(s: S, sender: ActorRef, n:Int) {
    val future: Future[T] = f(s)
    future.onComplete({
      case scala.util.Success(t) =>
        sender ! t
        self ! SubmitFromQueue
      case Failure(exception) =>
        self ! SubmitFromQueue
        if (n==0) {
          throw new RuntimeException(exception)
        } else {
          Logger.warn("Task failed with exception "+exception.getMessage+". Retries left: "+n)
          queueRequest(s, sender, n-1)
        }

    })
  }

  def queueRequest(s: S, sender: ActorRef, n:Int): Unit = {
    pendingRequests ::=(sender, s, n)
  }
}
