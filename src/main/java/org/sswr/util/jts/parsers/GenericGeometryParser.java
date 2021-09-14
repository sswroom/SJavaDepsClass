package org.sswr.util.jts.parsers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.sswr.util.jts.GeoJson;

import java.util.HashMap;
import java.util.Map;

public class GenericGeometryParser extends BaseParser implements GeometryParser<Geometry> {

    private Map<String, GeometryParser<? extends Geometry>> parsers;

    public GenericGeometryParser(GeometryFactory geometryFactory) {
        super(geometryFactory);
        parsers = new HashMap<String, GeometryParser<? extends Geometry>>();
        parsers.put(GeoJson.POINT, new PointParser(geometryFactory));
        parsers.put(GeoJson.MULTI_POINT, new MultiPointParser(geometryFactory));
        parsers.put(GeoJson.LINE_STRING, new LineStringParser(geometryFactory));
        parsers.put(GeoJson.MULTI_LINE_STRING, new MultiLineStringParser(geometryFactory));
        parsers.put(GeoJson.POLYGON, new PolygonParser(geometryFactory));
        parsers.put(GeoJson.MULTI_POLYGON, new MultiPolygonParser(geometryFactory));
        parsers.put(GeoJson.GEOMETRY_COLLECTION, new GeometryCollectionParser(geometryFactory, this));
    }

    @Override
    public Geometry geometryFromJson(JsonNode node) throws JsonMappingException {
        String typeName = node.get(GeoJson.TYPE).asText();
        GeometryParser<? extends Geometry> parser = parsers.get(typeName);
        if (parser != null) {
            return parser.geometryFromJson(node);
        } else {
            throw new JsonMappingException(null, "Invalid geometry type: " + typeName);
        }
    }
}
