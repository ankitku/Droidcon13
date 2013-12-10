module ApplicationHelper
  def json_response(response_hash, code)
    render :json => response_hash.to_json, :status => code
  end

  def general_response(status = "SUCCESS", request_type= "UNKNWN", options = {})
    {:status => status, :request_type => request_type}.merge!(options)
  end
  
  def send_response(status = "SUCCESS", request_type = "UNKNWN", options = {}, code = 200)
    logger.info "#{Time.now} | ApplicationHelper | send_response | Status : #{status}, Request_type : #{request_type}, Options : #{options.inspect}, Code : #{code}"
    response_hash = general_response(status, request_type, options)
    json_response(response_hash, code)
  end

end