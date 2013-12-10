require 'test_helper'

class GeoMessagesControllerTest < ActionController::TestCase
  setup do
    @geo_message = geo_messages(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:geo_messages)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create geo_message" do
    assert_difference('GeoMessage.count') do
      post :create, geo_message: { fromUserId: @geo_message.fromUserId, message: @geo_message.message, msgTime: @geo_message.msgTime, toUserId: @geo_message.toUserId }
    end

    assert_redirected_to geo_message_path(assigns(:geo_message))
  end

  test "should show geo_message" do
    get :show, id: @geo_message
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @geo_message
    assert_response :success
  end

  test "should update geo_message" do
    patch :update, id: @geo_message, geo_message: { fromUserId: @geo_message.fromUserId, message: @geo_message.message, msgTime: @geo_message.msgTime, toUserId: @geo_message.toUserId }
    assert_redirected_to geo_message_path(assigns(:geo_message))
  end

  test "should destroy geo_message" do
    assert_difference('GeoMessage.count', -1) do
      delete :destroy, id: @geo_message
    end

    assert_redirected_to geo_messages_path
  end
end
