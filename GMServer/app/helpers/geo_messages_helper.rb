module GeoMessagesHelper
  # avg. radius of earth in meters
  R = 6378137
  
  def nearby_messages(userId, userLoc, radiusInMetres = 1000)
    messageList = []
    GeoMessage.geo_near(userLoc.reverse).max_distance(radiusInMetres/R).distance_multiplier(R).spherical().each do |gmsg|
      if gmsg[:toUserId] == userId
        oid = gmsg[:id]
        hash = {:id => oid.to_s}
        hash.merge!(:timestamp => oid.generation_time.to_i)
        hash.merge!(:loc => gmsg[:loc].reverse)
        hash.merge!(:message => gmsg[:message])

        fromUser = User.where(:userId => gmsg[:fromUserId]).first
        unless fromUser.nil?
          hash.merge!(:from_user_name => fromUser[:name])
          hash.merge!(:from_user_pic => fromUser[:pic])
        end
        
        messageList << hash
      end
    end
    
    Rails.logger.info "#{Time.now} | GeoMessagesHelper | nearby_messages : #{messageList.inspect}"
    messageList
  end
  
  
  def from_user_messages(userId)
    messageList = []
    GeoMessage.where(:fromUserId => userId).each do |gmsg|
        oid = gmsg[:id]
        hash = {:id => oid.to_s}
        hash.merge!(:timestamp => oid.generation_time.to_i)
        hash.merge!(:loc => gmsg[:loc].reverse)
        hash.merge!(:message => gmsg[:message])

        toUser = User.where(:userId => gmsg[:toUserId]).first
        unless toUser.nil?
          hash.merge!(:to_user_name => toUser[:name])
          hash.merge!(:to_user_pic => toUser[:pic])
        end
        
        messageList << hash
    end
    
    Rails.logger.info "#{Time.now} | GeoMessagesHelper | from_user_messages : #{messageList.inspect}"
    messageList
  end
  
end
