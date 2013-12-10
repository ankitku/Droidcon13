json.array!(@geo_messages) do |geo_message|
  json.extract! geo_message, :id, :fromUserId, :toUserId, :message, :msgTime
  json.url geo_message_url(geo_message, format: :json)
end
