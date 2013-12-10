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
    @msg = GeoMessage.new(post_params)
    
    @msg.save
    redirect_to @msg
  end
  
  private
  def post_params
    params.require(:geo_message).permit(:fromUserId, :toUserId, :message, :msgTime, :loc)
  end
  
      # @geo_message = GeoMessage.new(geo_message_params)
# 
    # respond_to do |format|
      # if @geo_message.save
        # format.html { redirect_to @geo_message, notice: 'Geo message was successfully created.' }
        # format.json { render action: 'show', status: :created, location: @geo_message }
      # else
        # format.html { render action: 'new' }
        # format.json { render json: @geo_message.errors, status: :unprocessable_entity }
      # end
    # end

  # PATCH/PUT /geo_messages/1
  # PATCH/PUT /geo_messages/1.json
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
