package com.mpeers.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class RestClient {

	private static RestClient instance;
	
	private HttpClient client; 
	private CookieStore cookieStore;
	private HttpContext localContext;
	private String host = "http://54.254.216.45:3000";
	
	public static RestClient getInstance(){
		if(instance == null){
			instance = new RestClient();
		}
		return instance;
	}
	
	private RestClient(){
		client = new DefaultHttpClient();
		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}
	
	public void resetConnection(){		
		client = new DefaultHttpClient();
		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}
	
	public String getCookie(String name){
		for(Cookie ck : cookieStore.getCookies()){
			if(ck.getName().equals(name))
				return ck.getValue();
		}
		return "";
	}
	
	public String getHost(){
		return host;
	}
	
    public static enum RequestMethod{GET, POST, DELETE}
        
    public RestResponse execute(String url, RequestMethod method, HashMap<String, String> params, HashMap<String, String> headers){
    	if(url == null || url == "" || method == null)
    		return null;
    	
    	url = host + url;
    	ArrayList<NameValuePair> paramsList = new ArrayList<NameValuePair>();
    	ArrayList<NameValuePair> headersList = new ArrayList<NameValuePair>();
    	
    	if(params != null){
	    	for (Map.Entry<String, String> param : params.entrySet()) {
	    	    paramsList.add(new BasicNameValuePair(param.getKey(), param.getValue()));    	    
	    	}
    	}
    	
    	if(headers != null){
	    	for (Map.Entry<String, String> param : headers.entrySet()) {
	    		headersList.add(new BasicNameValuePair(param.getKey(), param.getValue()));    	    
	    	}
    	}
    	
    	return executeMethod(url, method, paramsList, headersList);
    }

    public RestResponse executeMethod(String url, RequestMethod method, ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers) 
    {
        switch(method) {
            case GET:
            {
                //add parameters
                String combinedParams = "";
                if(!params.isEmpty()){
                    combinedParams += "?";
                    for(NameValuePair p : params)
                    {                    	
                        String paramString = "";
						try {
							paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							RestResponse res = new RestResponse();
							res.responseCode = -1;
							res.message = "request error";							
							e.printStackTrace();
							return res;
						}
                        if(combinedParams.length() > 1)
                        {
                            combinedParams  +=  "&" + paramString;
                        }
                        else
                        {
                            combinedParams += paramString;
                        }
                    }
                }

                HttpGet request = new HttpGet(url + combinedParams);

                //add headers
                for(NameValuePair h : headers)
                {
                    request.addHeader(h.getName(), h.getValue());
                }

                return executeRequest(request, url);                
            }
            case POST:
            {
                HttpPost request = new HttpPost(url);

                //add headers
                for(NameValuePair h : headers)
                {
                    request.addHeader(h.getName(), h.getValue());
                }

                if(!params.isEmpty()){
                    try {
						request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						RestResponse res = new RestResponse();
						res.responseCode = -1;
						res.message = "request error";							
						e.printStackTrace();
						return res;
					}
                }

                return executeRequest(request, url);                
            }
            case DELETE:
            {
            	HttpDelete request = new HttpDelete(url);
            	 //add headers
                for(NameValuePair h : headers)
                {
                    request.addHeader(h.getName(), h.getValue());
                }
            	return executeRequest(request, url);
            }
        }
        
        return null;
    }

    private RestResponse executeRequest(HttpUriRequest request, String url)
    {
    	RestResponse res = new RestResponse();
    	HttpResponse httpResponse;

        try {
            httpResponse = client.execute(request, localContext);
            res.responseCode = httpResponse.getStatusLine().getStatusCode();
            res.message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {

                InputStream instream = entity.getContent();
                res.response = convertStreamToString(instream);

                // Closing the input stream will trigger connection release
                instream.close();                
            }

        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
            return null;
        }
        
        return res;
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
