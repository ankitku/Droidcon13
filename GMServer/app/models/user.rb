class User
  include Mongoid::Document
  
  field :name, type: String
  field :pic, type: String
  field :userId, type: String
  
  index({ userId: 1 }, { unique: true, name: "unique_user_index" })
end
