/*
 * Copyright 2012 Stephane Godbillon
 *
 * This sample is in the public domain.
 */
package controllers

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Sink, Source }

import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.{ MongoController, ReactiveMongoApi, ReactiveMongoComponents }

import reactivemongo.akkastream._
import reactivemongo.api.QueryOpts
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import javax.inject.Inject

class Application @Inject() (
  components: ControllerComponents,
  val reactiveMongoApi: ReactiveMongoApi
)(
  implicit
  system: ActorSystem,
  ec: ExecutionContext,
  mat: Materializer
)
  extends AbstractController(components)
  with MongoController with ReactiveMongoComponents {

  // let's be sure that the collections exists and is capped
  val futureCollection: Future[JSONCollection] = {
    val collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("acappedcollection"))

    (for {
      coll <- collection
      stats <- coll.stats()
    } yield {
      if (stats.capped) coll else {
        // the collection is not capped, so we convert it
        println("converting to capped")
        coll.convertToCapped(1024 * 1024, None)
      }
    }) recover {
      case _ =>
        println("creating capped collection...")
        collection.map(_.createCapped(1024 * 1024, None))
    } flatMap { _ =>
      println("the capped collection is available")
      collection
    }
  }

  def index = Action {
    Ok(views.html.index())
  }

  def watchCollection = WebSocket.accept[JsValue, JsValue] { request =>
    val in = Sink.foreach[JsValue] {
      case jsObj: JsObject => futureCollection.map(_.insert(jsObj))
      case js              => sys.error(s"unexpected JSON value: $js")
    }

    val out = Source.fromFutureSource(futureCollection.map { collection =>
      collection
        .find(Json.obj())
        .options(QueryOpts().tailable.awaitData)
        .cursor[JsObject]()
        .documentSource()
    })

    Flow.fromSinkAndSource(in, out)
  }
}
