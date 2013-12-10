class User
  include Mongoid::Document
  
  field :name, type: String
  field :pic, type: String
  field :userId, type: String
end
