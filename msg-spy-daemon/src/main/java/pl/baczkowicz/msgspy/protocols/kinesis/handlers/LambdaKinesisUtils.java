package pl.baczkowicz.msgspy.protocols.kinesis.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.tz.FixedDateTimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Helper utilities for testing Lambda functions.
 */
public class LambdaKinesisUtils
{

	private static final ObjectMapper mapper = new ObjectMapper();

	static
	{
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.setPropertyNamingStrategy(new UpperCaseRecordsPropertyNamingStrategy());
		mapper.registerModule(new TestJacksonMapperModule());
	}

	private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime()
			.withZone(new FixedDateTimeZone("GMT", "GMT", 0, 0));

	/**
	 * Helper method that parses a JSON object from a resource on the classpath
	 * as an instance of the provided type.
	 *
	 * @param resource
	 *            the path to the resource (relative to this class)
	 * @param clazz
	 *            the type to parse the JSON into
	 */
	public static <T> T parse(String resource, Class<T> clazz) throws IOException
	{
		InputStream stream = LambdaKinesisUtils.class.getResourceAsStream(resource);
		try
		{
			return mapper.readValue(stream, clazz);
		} 
		finally
		{
			stream.close();
		}
	}

	private static class TestJacksonMapperModule extends SimpleModule
	{
		private static final long serialVersionUID = 1L;

		public TestJacksonMapperModule()
		{
			super("TestJacksonMapperModule");

			super.addSerializer(DateTime.class, new DateTimeSerializer());
			super.addDeserializer(DateTime.class, new DateTimeDeserializer());
		}
	}

	private static class DateTimeSerializer extends JsonSerializer<DateTime>
	{

		@Override
		public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException
		{

			gen.writeString(dateTimeFormatter.print(value));
		}
	}

	private static class DateTimeDeserializer extends JsonDeserializer<DateTime>
	{

		@Override
		public DateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException
		{

			return dateTimeFormatter.parseDateTime(parser.getText());
		}
	}

	private static class UpperCaseRecordsPropertyNamingStrategy
			extends PropertyNamingStrategy.PropertyNamingStrategyBase
	{

		private static final long serialVersionUID = 1L;

		@Override
		public String translate(String propertyName)
		{
			if (propertyName.equals("records"))
			{
				return "Records";
			}
			return propertyName;
		}
	}
}
