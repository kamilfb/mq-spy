var Thread = Java.type("java.lang.Thread");
var Date = Java.type("java.util.Date");
var SimpleDateFormat = Java.type("java.text.SimpleDateFormat");
	
var TIME_FORMAT_WITH_SECONDS = "HH:mm:ss";
var TIME_WITH_SECONDS_SDF = new SimpleDateFormat(TIME_FORMAT_WITH_SECONDS);

function publishTime()
{
	var currentTime = TIME_WITH_SECONDS_SDF.format(new Date());
	
	mqtt.publish("time", currentTime, 0, false);

	return "Published to topic time!";
}
