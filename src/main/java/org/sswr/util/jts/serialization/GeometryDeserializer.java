package org.sswr.util.jts.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.jts.parsers.GeometryParser;

import java.io.IOException;

public class GeometryDeserializer<T extends Geometry> extends JsonDeserializer<T>
{
    private GeometryParser<T> geometryParser;

    public GeometryDeserializer(GeometryParser<T> geometryParser)
	{
        this.geometryParser = geometryParser;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode root = oc.readTree(jsonParser);
        return geometryParser.geometryFromJson(root);
    }
}
