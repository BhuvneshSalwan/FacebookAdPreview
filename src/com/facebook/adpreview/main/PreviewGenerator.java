package com.facebook.adpreview.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.facebook.adpreview.request.PreviewGeneratorRequest;

public class PreviewGenerator extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String requestParams = request.getQueryString();
		
		response.getWriter().print("PREVIEW GENERATOR VERSION 1 : " + requestParams);
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		
		ArrayList<String> formats = new ArrayList<String>();
		formats.add("DESKTOP_FEED_STANDARD");
		formats.add("MOBILE_FEED_STANDARD");
		formats.add("RIGHT_COLUMN_STANDARD");
		formats.add("INSTAGRAM_STANDARD");
		formats.add("INSTAGRAM_STORY");
		formats.add("MESSENGER_MOBILE_INBOX_MEDIA");
		formats.add("MOBILE_INTERSTITIAL");
		formats.add("MOBILE_BANNER");
		formats.add("MOBILE_MEDIUM_RECTANGLE");
		formats.add("MOBILE_FULLWIDTH");
		formats.add("MOBILE_NATIVE");
		
		try{
			
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = request.getReader();
			String line;
			while((line = reader.readLine()) != null){
				builder.append(line);
			}
			String query = builder.toString();
		
			JSONObject queryObject = new JSONObject(query);
			
			if(queryObject.has("creative_id") && !queryObject.isNull("creative_id") && !queryObject.getString("creative_id").equalsIgnoreCase("")){
				
				JSONObject requestObj = PreviewGeneratorRequest.generatePreview(queryObject);
				
				if(null != requestObj && requestObj.has("success") && requestObj.getBoolean("success")){
					
					out.println(requestObj.toString());
					
				}
				else{

					JSONObject response_bubble = new JSONObject();
					response_bubble.put("success", false);
					response_bubble.put("error_code", 501);
					response_bubble.put("message", "COULD NOT FIND THE PREVIEW LINKS. MESSAGE : " + requestObj.toString());
					
					for(String ad_format : formats) {
						response_bubble.put(ad_format, "");
					}
					
					out.println(response_bubble.toString());
				
				}
				
			}
			else{
				
				JSONObject response_bubble = new JSONObject();
				response_bubble.put("success", false);
				response_bubble.put("error_code", 502);
				response_bubble.put("message", "INVALID VALUE FOR THE CREATIVE ID FOUND.");
				
				for(String ad_format : formats) {
					response_bubble.put(ad_format, "");
				}
				
				out.println(response_bubble.toString());
				
			}
		
		}catch(Exception e){

			JSONObject response_bubble = new JSONObject();
			response_bubble.put("success", false);
			response_bubble.put("error_code", 503);
			response_bubble.put("message", "COULD NOT FIND THE PREVIEW LINKS. MESSAGE : " + e.getMessage());
			
			for(String ad_format : formats) {
				response_bubble.put(ad_format, "");
			}
			
			out.println(response_bubble.toString());
			
		}
		
		finally {
			out.close();
		}
			
	}

}