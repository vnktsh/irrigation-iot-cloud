package com.google.devrel.training.conference.domain;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.io.IOException;
import java.net.MalformedURLException;
import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.CurrentWeather.Rain;
import net.aksingh.owmjapis.OpenWeatherMap;
import org.json.JSONException;


class FarmAnalytics {
    boolean b;
	String str;
    public static OpenWeatherMap owm = new OpenWeatherMap("ac99eb4c7588a151f420a57224f14a88");

	public FarmAnalytics(boolean b,String str){

		b=this.b;
		str=this.str;
	}


    public static FarmAnalytics computeSense(Conference ref, String sensor_data_str) throws IOException, MalformedURLException, JSONException {
        int sensor_data = Integer.parseInt(sensor_data_str);
        // declaring object of "OpenWeatherMap" class
        //OpenWeatherMap owm = new OpenWeatherMap("ac99eb4c7588a151f420a57224f14a88");

        // getting current weather data for the "London" city
        CurrentWeather cwd = owm.currentWeatherByCityName(ref.getCity());

        //printing city name from the retrieved data
        System.out.println("City: " + cwd.getCityName());

        int rainDuration=0;
        if (cwd.hasRainInstance()==false)
        {
        	rainDuration=0;
        }
        else
        {
        	rainDuration=1;
        }
        System.out.println("Rain instance: "+cwd.hasRainInstance());
        // printing the max./min. temperature
        //System.out.println("Temperature: " + cwd.getMainInstance().getMaxTemperature()
        //                    + "/" + cwd.getMainInstance().getMinTemperature() + "\'F");
        int temperature=(int) (cwd.getMainInstance().getMaxTemperature()+cwd.getMainInstance().getMinTemperature());
        temperature=temperature/2;
        System.out.println("Temperature: "+temperature);
    	FarmAnalytics a = Logic(sensor_data, rainDuration, ref.getCrop(), temperature);
    	System.out.println(a.b);
    	System.out.println(a.str);
        return a;
    }


public static FarmAnalytics Logic(int sensorData,int rainDuration,String cropType,int temperature){
	boolean value = false;
	String str = "No Need to water now";

    FarmAnalytics anal=new FarmAnalytics(value,str);

	switch (cropType){



	case "Rice":
		if(rainDuration==1)
		{
			value=false;
		    str=" It will rain today. No need to water.";
		    anal.b=value;
		    anal.str=str;
			return anal;
			}
		else{
			if(sensorData<95 && temperature>68)
			{
				value=true;
				str="Hey! The moisture level of soil is slightly below the optimum level and temperature is high.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else if (sensorData<70 && temperature<50)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry and temperature is also quite low.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}

			else if (sensorData<70 && 55<temperature && temperature<65)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else{
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
		}




	case "Wheat":
		if(rainDuration==1)
		{
			value=false;
		    str=" It will rain today. No need to water.";
		    anal.b=value;
		    anal.str=str;
			return anal;
			}
		else{
			if(sensorData<80 && temperature>85)
			{
				value=true;
				str="Hey! The moisture level of soil is slightly below the optimum level and temperature is high.Its better to water the crops.";
				 anal.b=value;
				 anal.str=str;
				return anal;
			}
			else if (sensorData<51 && temperature<50)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry and temperature is quite low.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}

			else if (sensorData<50 && 75<temperature && temperature<85)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else{

				 anal.b=value;
				    anal.str=str;
				return anal;
			}
		}


	case "Corn":
		if(rainDuration==1)
		{
		    value=false;
		    str=" It will rain today. No need to water.";
		    anal.b=value;
		    anal.str=str;
			return anal;
			}
		else{
			if(sensorData<80 && temperature>50)
			{
				value=true;
				str="Hey! The moisture level of soil is slightly below the optimum level and temperature is high.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else if (sensorData<50 && temperature<25)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry and temperature is quite low.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}

			else if (sensorData<50 && 26<=temperature && temperature<50)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else{

				 anal.b=value;
				    anal.str=str;
				return anal;
			}
		}



	case "Potato":
		if(rainDuration==1)
		{
			value=false;
		    str=" It will rain today. No need to water.";

		    anal.b=value;
		    anal.str=str;
			return anal;
			}
		else{
			if(sensorData<90 && temperature>85)
			{
				value=true;
				str="Hey! The moisture level of soil is slightly below the optimum level and temperature is high.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else if (sensorData<60 && temperature<55)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry and temperature is quite low.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}

			else if (sensorData<60 && 55<temperature && temperature<75)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else{
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
		}



	case "Soyabean":
		if(rainDuration==1)
		{

		    anal.b=value;
		    anal.str=str;
			return anal;
			}
		else{
			if(sensorData<90 && temperature>85)
			{
				value=true;
				str="Hey! The moisture level of soil is slightly below the optimum level and temperature is high.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else if (sensorData<60 && temperature<50)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry and temperature is quite low.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}

			else if (sensorData<60 && 75<temperature && temperature<85)
			{
				value=true;
				str="Hey! The moisture level of soil is quite dry.Its better to water the crops.";
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
			else{
				 anal.b=value;
				    anal.str=str;
				return anal;
			}
		}


	}
	//anal = new Analytics(value,str);
	return anal;
}

} // close of Farm Analytics
