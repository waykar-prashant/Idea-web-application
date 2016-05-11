package hello;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class LightController {

	//Get the sunrise/sunset points from location colorado - Mark's home address
	//10 API's for each set of light + 10 recommendation API's
	@MessageMapping("/light/{sendTo}/deviceId/{deviceId}")
	@SendTo("/topic/light/{sendTo}")
	public String getLightInformation(@DestinationVariable String sendTo, @DestinationVariable String deviceId) throws Exception{
		String returnValue = "{}";
		if(sendTo != null && deviceId != null){
			if(sendTo.equals("general")){
				//get the general data for all the devices available
				returnValue = getGeneralLightInformation(deviceId);
			}
		}
		
		return returnValue;
	}

	private String getGeneralLightInformation(String deviceId) {
		JSONObject query = new JSONObject();
		try{
			JSONArray metrics = new JSONArray();
			query.put("metrics",metrics);

			JSONObject metric = new JSONObject();
			metrics.put(metric);

			JSONObject tags = new JSONObject();
			JSONArray tagList = new JSONArray();
			//deviceId = deviceId.replace(":", "-");
			System.out.println("Received deviceId:"+deviceId);

			metric.put("tags", tags);
			metric.put("name", deviceId);

			JSONArray aggregators = new JSONArray();
			metric.put("aggregators", aggregators);

			JSONObject agg = new JSONObject();
			aggregators.put(agg);

			agg.put("name","avg");
			agg.put("align_sampling",true);

			JSONObject sampling = new JSONObject();
			sampling.put("value",1);
			sampling.put("unit","minutes");
			//sampling.put("value",1);
			//sampling.put("unit","seconds");
			agg.put("sampling",sampling);
			query.put("cache_time",0);



			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);

			long start = cal.getTimeInMillis();

			cal.add(Calendar.DAY_OF_MONTH,1);
			long end = cal.getTimeInMillis();

			query.put("start_absolute",start);
			query.put("end_absolute",end);

			JSONObject relative = new JSONObject();
			relative.put("value",1);
			relative.put("unit","days");
			//query.put("start_relative",relative);

			System.out.println(query.toString(5));

			java.net.URL u = new java.net.URL("http://localhost:8888/api/v1/datapoints/query");
			java.net.HttpURLConnection uc = (java.net.HttpURLConnection)u.openConnection();

			uc.setRequestMethod("POST");
			uc.setDoOutput(true);
			uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

			//String str = "";
			PrintWriter pw = new PrintWriter(uc.getOutputStream());
			pw.println(query.toString());
			pw.close();
			//System.out.println(str);

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			StringBuffer tempBuffer = new StringBuffer();

			String res = null;
			while((res=in.readLine())!=null){
				tempBuffer.append(res);
			}
			in.close();
			
			if(tempBuffer != null){
				JSONObject queryJSON = new JSONObject(tempBuffer.toString());
				JSONObject resultObject = queryJSON.getJSONArray("queries").getJSONObject(0).getJSONArray("results").getJSONObject(0);
				//append the chartId and the deviceId to the result json
				resultObject.put("deviceId", deviceId);
				resultObject.put("chartId", deviceId.replaceAll("_", "").replace("_", ""));
				return resultObject.toString();
			}
			return tempBuffer.toString();
		}
		catch(Exception e){
			System.out.println("Error Building query:"+e.getMessage());
		}
		return "{}";
	}
	
	
}
