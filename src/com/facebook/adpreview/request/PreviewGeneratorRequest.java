package com.facebook.adpreview.request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class PreviewGeneratorRequest {

	private final static String URL = "https://graph.facebook.com";
	private final static String VERSION = "v2.12";
	private final static String SESSION_TOKEN = "EAAWXmQeQZAmcBAJHkodkpmV1ZBX7S9t9eQDsmbBpr0KVFAoGYhV9vAw00a2sYkoM22lVp0s0RVxi0Uiw2cgxjBJyJ3AZAbwEP4MRZAFU7e0kq6ssOtUMMOsgZA2bYMypkyS8HDtJYHb3Wa8fPknN8Vd7cieifdE8j77qisyyw4wZDZD";
	
	public static JSONObject generatePreview(JSONObject queryObject) throws Exception {
	
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
		
			String creative_id = queryObject.getString("creative_id");

			JSONArray batch_array = new JSONArray();
			
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=DESKTOP_FEED_STANDARD\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=MOBILE_FEED_STANDARD\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=RIGHT_COLUMN_STANDARD\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=INSTAGRAM_STANDARD\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=INSTAGRAM_STORY\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=MESSENGER_MOBILE_INBOX_MEDIA\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=MOBILE_INTERSTITIAL\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=MOBILE_BANNER\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=MOBILE_MEDIUM_RECTANGLE\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=MOBILE_FULLWIDTH\"}"));
			batch_array.put(new JSONObject("{\"method\":\"GET\",\"relative_url\":\"" + creative_id + "/previews?ad_format=MOBILE_NATIVE\"}"));
			
			String custom_url = URL+"/"+VERSION;
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost postreq = new HttpPost(custom_url);
			
			ArrayList<NameValuePair> urlparams = new ArrayList<NameValuePair>();
			urlparams.add(new BasicNameValuePair("access_token", SESSION_TOKEN));
			urlparams.add(new BasicNameValuePair("batch", batch_array.toString()));
			
			postreq.setEntity(new UrlEncodedFormEntity(urlparams));
			
			HttpResponse responseFacebook = httpclient.execute(postreq);
	
			BufferedReader rd = new BufferedReader(new InputStreamReader(responseFacebook.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			
			if(responseFacebook.getStatusLine().getStatusCode() >= 200 && responseFacebook.getStatusLine().getStatusCode() < 300){
				
				JSONArray responseObj = new JSONArray(result.toString());
				
				if(null != responseObj && responseObj.length() > 0) {
					
					JSONObject response_bubble = new JSONObject();
					
					for(int arr_i = 0; arr_i < responseObj.length(); arr_i++) {
						if(!responseObj.isNull(arr_i)) {
							JSONObject format = responseObj.getJSONObject(arr_i);
							if(format.has("code") && format.getInt("code") == 200 && format.has("body") && !format.isNull("body")) {
								JSONObject body = new JSONObject(format.getString("body"));
								JSONArray data = body.getJSONArray("data");
								if(data.length() == 1) {
									JSONObject body_data = data.getJSONObject(0);
									if(body_data.has("body") && !body_data.isNull("body")) {
										String iframe = body_data.getString("body");
										Document doc = Jsoup.parse(URLDecoder.decode(iframe, "UTF-8"));
										Element element = doc.select("iframe").first();
										response_bubble.put(formats.get(arr_i) ,element.attr("src").replace("&amp;","&"));
									}
									else {
										response_bubble.put(formats.get(arr_i) , "");
										continue;
									}
								}
								else {
									response_bubble.put(formats.get(arr_i) , "");
									continue;
								}
							}
							else {
								response_bubble.put(formats.get(arr_i) , "");
								continue;
							}
						}
						else {
							response_bubble.put(formats.get(arr_i) , "");
							continue;
						}
					
					}
					
					response_bubble.put("success", true).put("message", "PLEASE FIND THE PREVIEW URLS.").put("error_code", "");
					
					return response_bubble;
					
				}
				else {
					
					JSONObject response_bubble = new JSONObject();
					response_bubble.put("success", false);
					response_bubble.put("error_code", 401);
					response_bubble.put("message", "COULD NOT FIND THE PREVIEW LINKS. MESSAGE : " + result.toString());
					
					for(String ad_format : formats) {
						response_bubble.put(ad_format, "");
					}
					
					return response_bubble;
				
				}
					
			}
			else{
				
				JSONObject response_bubble = new JSONObject();
				response_bubble.put("success", false);
				response_bubble.put("error_code", 402);
				response_bubble.put("message", "COULD NOT FIND THE PREVIEW LINKS. MESSAGE : " + result.toString());
				
				for(String ad_format : formats) {
					response_bubble.put(ad_format, "");
				}
				
				return response_bubble;
				
			}
	
	}
	
}