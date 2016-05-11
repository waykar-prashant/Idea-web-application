package hello;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TemperatureController {

	//private MessageSendingOperations<String> messagingTemplate;

	@MessageMapping("/temperature/{sendTo}/deviceId/{deviceId}")
	@SendTo("/topic/temperature/{sendTo}")
	public String greeting(DeviceInfo message, @DestinationVariable String sendTo, @DestinationVariable String deviceId)
			throws Exception {
		// Parse the deviceId and the callbackId
		deviceId = String.valueOf(deviceId);
		String strArray[] = deviceId.split("\\?");
		deviceId = strArray[0];
		String callbackId = strArray[1].split("=")[1];
		System.out.println("Callback Id : " + callbackId);
		System.out.println("Device Id: " + deviceId);
		System.out.println("Send To : " + sendTo);
		System.out.println("Message : " + message);
		String returnValue = "{}";
		if (deviceId != null && callbackId != null) {
			if (sendTo.equals("latest")) {
				returnValue = getLatestTemperature(deviceId, callbackId);
			} else if (sendTo.equals("forecast")) {
				returnValue = getForecastTemperature(deviceId, callbackId);
			}else if(sendTo.equals("general")){
				returnValue = getGeneralTemperature(deviceId, callbackId);
			}else if(sendTo.equals("activetemperature")){
				returnValue = MongoDBConnection.getActiveTemperature().toString();
			}
		}
		return returnValue.toString();
	}

	private String getGeneralTemperature(String deviceId, String callbackId) {
		JSONObject query = new JSONObject();
		try{
			JSONArray metrics = new JSONArray();
			query.put("metrics",metrics);

			JSONObject metric = new JSONObject();
			metrics.put(metric);

			JSONObject tags = new JSONObject();
			JSONArray tagList = new JSONArray();
			deviceId = deviceId.replace(":", "-");
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

	/*
	{
	  "metrics": [
	    {
	      "tags": {},
	      "name": "environment.temp",
	      "aggregators": [
	        {
	          "name": "avg",
	          "align_sampling": true,
	          "sampling": {
	            "value": "1",
	            "unit": "minutes"
	          }
	        }
	      ]
	    }
	  ],
	  "cache_time": 0,
	  "start_absolute": 1448434800000,
	  "end_absolute": 1448521200000
	}
	
	{
  "metrics": [
    {
      "tags": {},
      "name": "28-db-b1-1f-06-00-00-d3",
      "aggregators": [
        {
          "name": "avg",
          "align_sampling": true,
          "sampling": {
            "value": "1",
            "unit": "minutes"
          }
        }
      ]
    }
  ],
  "cache_time": 0,
  "start_absolute": 1460876400000,
  "end_absolute": 1461567600000
}
	*/

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

			//out.println(query.toString(5));

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
			StringBuffer buffer = new StringBuffer();
			//angular.callbacks._0
			buffer.append(callbackId+"(");
			StringBuffer tempBuffer = new StringBuffer();

			String res = null;
			while((res=in.readLine())!=null){
				buffer.append(res);
				tempBuffer.append(res);
				buffer.append("\n");
			}
			in.close();

			buffer.append(");");
			//System.out.println(buffer.toString());
			String result = null;
			if(tempBuffer != null){
				JSONObject queryJSON = new JSONObject(tempBuffer.toString());
				JSONObject resultObject = queryJSON.getJSONArray("queries").getJSONObject(0).getJSONArray("results").getJSONObject(0);
				JSONArray valueArray = resultObject.getJSONArray("values");
				
				ArrayList<Double> arr = new ArrayList<Double>();
				
				for (int i = 0; i < valueArray.length(); i++) {
					JSONArray v = valueArray.getJSONArray(i);
					arr.add(v.getDouble(1));
				}
				double max = Collections.max(arr);
				double min  = Collections.min(arr);
				double avg = calculateAverage(arr);
				JSONObject aggregation  = new JSONObject();
				aggregation.put("max", max);
				aggregation.put("min", min);
				aggregation.put("avg", avg);
				
				resultObject.append("aggregation", aggregation);
				//result = callbackId+"("+queryJSON.toString()+");";
				result = queryJSON.toString();
			}
			return result;
			//return buffer.toString();
		}
		catch(Exception e){
			System.out.println("Error Building query:"+e.getMessage());
		}
		return "{}";
	}

	private String getLatestTemperature(String deviceId, String callbackId) {
		JSONObject query = new JSONObject();
		try {
			JSONArray metrics = new JSONArray();
			query.put("metrics", metrics);

			JSONObject metric = new JSONObject();
			metrics.put(metric);

			JSONObject tags = new JSONObject();
			//JSONArray tagList = new JSONArray();
			deviceId = deviceId.replace(":", "-");
			System.out.println("Received deviceId:" + deviceId);

			metric.put("tags", tags);
			metric.put("name", deviceId);

			query.put("cache_time", 0);
			JSONObject startRelative = new JSONObject();
			startRelative.put("value", 6);
			startRelative.put("unit", "seconds");
			query.put("start_relative", startRelative);
			JSONObject endRelative = new JSONObject();
			endRelative.put("value", 1);
			endRelative.put("unit", "seconds");
			query.put("end_relative", endRelative);

			/*
			 * { "metrics": [ { "tags": {}, "name": "28-4d-4a-60-07-00-00-9f" }
			 * ], "cache_time": 0, "start_relative": { "value": "10", "unit":
			 * "seconds" } }
			 */

			java.net.URL u = new java.net.URL("http://localhost:8888/api/v1/datapoints/query");
			java.net.HttpURLConnection uc = (java.net.HttpURLConnection) u.openConnection();

			uc.setRequestMethod("POST");
			uc.setDoOutput(true);
			uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			//String str = "";
			PrintWriter pw = new PrintWriter(uc.getOutputStream());
			pw.println(query.toString());
			pw.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			//buffer.append(callbackId + "(");

			String res = null;
			while ((res = in.readLine()) != null) {
				buffer.append(res);
				// tempBuffer.append(res);
				buffer.append("\n");
			}
			in.close();
			//buffer.append(");");
			return buffer.toString();
		} catch (Exception e) {
			System.out.println("Error Building query:" + e.getMessage());
		}
		return "{}";
	}

	private String getForecastTemperature(String deviceId, String callbackId) {
		JSONObject query = new JSONObject();
		try {
			JSONArray metrics = new JSONArray();
			query.put("metrics", metrics);

			JSONObject metric = new JSONObject();
			metrics.put(metric);

			JSONObject tags = new JSONObject();
			//JSONArray tagList = new JSONArray();
			deviceId = deviceId.replace(":", "-");
			System.out.println("Received deviceId:" + deviceId);

			metric.put("tags", tags);
			metric.put("name", deviceId);

			JSONArray aggregators = new JSONArray();
			metric.put("aggregators", aggregators);

			JSONObject agg = new JSONObject();
			aggregators.put(agg);

			agg.put("name", "avg");
			agg.put("align_sampling", true);

			JSONObject sampling = new JSONObject();
			sampling.put("value", 1);
			sampling.put("unit", "hours");
			agg.put("sampling", sampling);
			query.put("cache_time", 0);

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			long start = cal.getTimeInMillis();

			cal.add(Calendar.DAY_OF_MONTH, 5);
			long end = cal.getTimeInMillis();

			query.put("start_absolute", start);
			query.put("end_absolute", end);

			JSONObject relative = new JSONObject();
			relative.put("value", 1);
			relative.put("unit", "days");
			java.net.URL u = new java.net.URL("http://localhost:8888/api/v1/datapoints/query");
			java.net.HttpURLConnection uc = (java.net.HttpURLConnection) u.openConnection();

			uc.setRequestMethod("POST");
			uc.setDoOutput(true);
			uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			//String str = "";
			PrintWriter pw = new PrintWriter(uc.getOutputStream());
			pw.println(query.toString());
			pw.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			StringBuffer buffer = new StringBuffer();

			//buffer.append(callbackId + "(");
			StringBuffer tempBuffer = new StringBuffer();
			String res = null;
			while ((res = in.readLine()) != null) {
				buffer.append(res);
				tempBuffer.append(res);
				//buffer.append("\n");
			}
			in.close();

			//buffer.append(");");
			String result;
			if (tempBuffer != null) {
				JSONObject queryJSON = new JSONObject(tempBuffer.toString());
				JSONObject resultObject = queryJSON.getJSONArray("queries").getJSONObject(0).getJSONArray("results")
						.getJSONObject(0);
				JSONArray valueArray = resultObject.getJSONArray("values");

				ArrayList<Double> arr = new ArrayList<Double>();

				for (int i = 0; i < valueArray.length(); i++) {
					JSONArray v = valueArray.getJSONArray(i);
					arr.add(v.getDouble(1));
				}
				double max = Collections.max(arr);
				double min = Collections.min(arr);
				double avg = calculateAverage(arr);
				JSONObject aggregation = new JSONObject();
				aggregation.put("max", max);
				aggregation.put("min", min);
				aggregation.put("avg", avg);

				resultObject.append("aggregation", aggregation);
				result = callbackId + "(" + queryJSON.toString() + ");";

			}
			return buffer.toString();
		} catch (Exception e) {
			System.out.println("Error Building query:" + e.getMessage());
		}
		return "{}";
	}

	private double calculateAverage(List<Double> marks) {
		Double sum = 0.0;
		if (!marks.isEmpty()) {
			for (Double mark : marks) {
				sum += mark;
			}
			return sum.doubleValue() / marks.size();
		}
		return sum;
	}

}
