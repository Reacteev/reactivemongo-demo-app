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
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import javax.inject.Inject

class Application @Inject() (
  components: ControllerComponents,
  val reactiveMongoApi: ReactiveMongoApi
)(implicit system: ActorSystem, ec: ExecutionContext, mat: Materializer)
  extends AbstractController(components) with MongoController with ReactiveMongoComponents {

  def watchCollection(author: String) = WebSocket.accept[JsValue, JsValue] { _ =>
    insert(Json.obj("content" -> s"$author has joined."))

    val in = Sink.foreach[JsValue] {
      case jsObj: JsObject => insert(jsObj)
      case js              => sys error s"unexpected JSON value: $js"
    }.mapMaterializedValue(_.map { done =>
      insert(Json.obj("content" -> s"$author has left."))
      done
    })

    val out = Source fromFutureSource futureCollection.map { collection =>
      collection
        .find(Json.obj())
        .options(QueryOpts().tailable.awaitData)
        .cursor[JsObject]()
        .documentSource()
    }

    Flow fromSinkAndSource (in, out)
  }

  def index = Action {
    Ok(views.html.index())
  }

  val futureCollection: Future[JSONCollection] = for {
    collection <- reactiveMongoApi.database.map(_.collection[JSONCollection]("acappedcollection"))
    _ <- collection.drop(false)
    _ <- collection.createCapped(1024 * 1024, None)
  } yield collection

  @inline
  def insert(jsObj: JsObject): Future[WriteResult] =
    futureCollection flatMap (_ insert jsObj)
}
