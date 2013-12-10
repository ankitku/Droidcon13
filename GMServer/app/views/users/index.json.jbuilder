json.array!(@users) do |user|
  json.extract! user, :id, :name, :pic, :userId
  json.url user_url(user, format: :json)
end
