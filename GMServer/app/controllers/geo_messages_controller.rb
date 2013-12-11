class GeoMessagesController < ApplicationController
  before_action :set_geo_message, only: [:show, :edit, :update, :destroy]

  # GET /geo_messages
  # GET /geo_messages.json
  def index
    @geo_messages = GeoMessage.all
  end

  # GET /geo_messages/1
  # GET /geo_messages/1.json
  def show
  end

  # GET /geo_messages/new
  def new
    @geo_message = GeoMessage.new
  end

  # GET /geo_messages/1/edit
  def edit
  end

  # POST /geo_messages
  # POST /geo_messages.json
  def create
    entries = 0
    
    params[:geo_messages].each do |gmsg|
      @geo_message = GeoMessage.new(
      :fromUserId => gmsg[:fromUserId],
      :fromUserId => gmsg[:toUserId],
      :message => gmsg[:message],
      :msgTime => gmsg[:msgTime],
      :loc => gmsg[:loc]
      )
      
      @user = User.new(
      :userId => gmsg[:fromUserId],
      :name => gmsg[:fromUserName],
      :pic => gmsg[:fromUserPic]
      )
      
      begin
        if @geo_message.save
          entries = entries.succ
        end
        @user.save
      rescue Exception => e
        if e.message =~ /11000/
          puts "------Duplicate key error handled------"
        else
          raise e
        end
      end
    
    end

    
    if entries > 0
      send_response("SUCCESS", "SAVE_GM", {:entries => entries.to_s}, 200)
    else
      send_response("FAILURE", "SAVE_GM", {:entries => entries.to_s}, 400)
    end
  end


  def update
    respond_to do |format|
      if @geo_message.update(geo_message_params)
        format.html { redirect_to @geo_message, notice: 'Geo message was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: 'edit' }
        format.json { render json: @geo_message.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /geo_messages/1
  # DELETE /geo_messages/1.json
  def destroy
    @geo_message.destroy
    respond_to do |format|
      format.html { redirect_to geo_messages_url }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_geo_message
      @geo_message = GeoMessage.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def geo_message_params
      params.require(:geo_message).permit(:fromUserId, :toUserId, :message, :msgTime)
    end
end
