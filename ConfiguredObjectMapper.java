import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ConfiguredObjectMapper {
    public final static ObjectMapper objectMapper;
    public static TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<>() {};


    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static String writeValueAsString(Object value){
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readObjectFromJson(final String json,final Class<T> clazz){
        try {
            return objectMapper.readValue(json,clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> convertObjectToMap(Object value) {
        return objectMapper.convertValue(value, mapTypeReference);
    }

    public static JsonParser createParser(InputStream content) {
        try {
            return objectMapper.createParser(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
