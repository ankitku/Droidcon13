class GeoMessage
  include Mongoid::Document
  
  field :fromUserId, type: String
  field :toUserId, type: String
  field :message, type: String
  field :seen, type: Boolean
    
  field :loc, :type => Array
  
  index({ loc: "2d" }, { min: -200, max: 200, name: "geo_spatial_index"})
  index({ fromUserId: 1, toUserId: 1, message: 1}, { unique: true, drop_dups: true, name: "unique_messages_index" })
end
