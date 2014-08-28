package model.scraping

case class Builder(keyField:String, data:Map[String, Any] ){
  assert(keyField.size>0 && data.containsKey(keyField) && data(keyField).isInstanceOf[String])
  def key = data(keyField)
  def update(updateData:Map[String, Any]): Builder = {
    if (updateData.containsKey(keyField)){
      if (updateData(keyField)){
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
}

case class BuilderList(builders:Map[String, Builder]) {
  def insert(keyField:String, data:Map[String, Any]):BuilderList = {
    val b = Builder(keyField, data)
    if (!data.contains(b.key)){
      BuilderList((b.key->b)+builders)
    } else {
      this
    }
  }
  def update(data:Map[String, Any]):BuilderList {
    
  }
  def upsert(data:Map[String, Any]):BuilderList
}


