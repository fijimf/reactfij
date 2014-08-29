package model.scraping

case class Builder(keyField:String, data:Map[String, Any] ){
  assert(keyField.size>0 && data.contains(keyField) && data(keyField).isInstanceOf[String])
  def key = data(keyField)
  def update(updateData:Map[String, Any]): Builder = {
    if (updateData.contains(keyField)){
      if (updateData(keyField)==key){
        Builder(keyField, data++updateData)
      } else {
        //key field in upsdate data does not match our keyField -- cannot update
        this
      }
    } else {
      Builder(keyField, data++updateData)
    }
  }

}

