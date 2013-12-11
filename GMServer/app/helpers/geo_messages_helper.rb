module GeoMessagesHelper
  # avg. radius of earth in meters
  R = 6378137
  
  def nearby_messages(userId, userLoc, radiusInMetres = 1000)
    messageList = []
    GeoMessage.geo_near(userLoc.reverse).max_distance(radiusInMetres/R).distance_multiplier(R).spherical().each do |gmsg|
      if gmsg[:toUserId] == userId
        messageList << gmsg
      end
    end
    
    messageList
  end
  
end
