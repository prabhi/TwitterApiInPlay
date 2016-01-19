package models

import play.api.libs.json.Json

/**
  * Created by prabhitharajidi on 1/18/16.
  */
object PlayJsonFormat {

    implicit val seqOfTwitterUserFormat = Json.writes[TwitterUser]
    implicit val listOfTwitterHandles = Json.writes[TwitterScreenNames]
    implicit val readlistOfTwitterHandles = Json.reads[TwitterScreenNames]
}
