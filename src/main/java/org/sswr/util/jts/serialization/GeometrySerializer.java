package org.sswr.util.jts.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.sswr.util.jts.GeoJson;

import java.io.IOException;
import java.util.Arrays;

public class GeometrySerializer extends JsonSerializer<Geometry> {

    @Override
    public void serialize(Geometry value, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {

        writeGeometry(jgen, value);
    }

    public void writeGeometry(JsonGenerator jgen, Geometry value)
            throws IOException {
        if (value instanceof Polygon) {
            writePolygon(jgen, (Polygon) value);

        } else if (value instanceof Point) {
            writePoint(jgen, (Point) value);

        } else if (value instanceof MultiPoint) {
            writeMultiPoint(jgen, (MultiPoint) value);

        } else if (value instanceof MultiPolygon) {
            writeMultiPolygon(jgen, (MultiPolygon) value);

        } else if (value instanceof LineString) {
            writeLineString(jgen, (LineString) value);

        } else if (value instanceof MultiLineString) {
            writeMultiLineString(jgen, (MultiLineString) value);

        } else if (value instanceof GeometryCollection) {
            writeGeometryCollection(jgen, (GeometryCollection) value);

        } else {
            throw new JsonMappingException(jgen, "Geometry type "
                    + value.getClass().getName() + " cannot be serialized as GeoJSON." +
                    "Supported types are: " + Arrays.asList(
                    Point.class.getName(),
                    LineString.class.getName(),
                    Polygon.class.getName(),
                    MultiPoint.class.getName(),
                    MultiLineString.class.getName(),
                    MultiPolygon.class.getName(),
                    GeometryCollection.class.getName()));
        }
    }

    private void writeGeometryCollection(JsonGenerator jgen, GeometryCollection value) throws
            IOException {
        jgen.writeStartObject();
        jgen.writeStringField(GeoJson.TYPE, GeoJson.GEOMETRY_COLLECTION);
        jgen.writeArrayFieldStart(GeoJson.GEOMETRIES);

        for (int i = 0; i != value.getNumGeometries(); ++i) {
            writeGeometry(jgen, value.getGeometryN(i));
        }

        jgen.writeEndArray();
        jgen.writeEndObject();
    }

    private void writeMultiPoint(JsonGenerator jgen, MultiPoint value)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(GeoJson.TYPE, GeoJson.MULTI_POINT);
        jgen.writeArrayFieldStart(GeoJson.COORDINATES);

        for (int i = 0; i != value.getNumGeometries(); ++i) {
            writePointCoords(jgen, (Point) value.getGeometryN(i));
        }

        jgen.writeEndArray();
        jgen.writeEndObject();
    }

    private void writeMultiLineString(JsonGenerator jgen, MultiLineString value)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(GeoJson.TYPE, GeoJson.MULTI_LINE_STRING);
        jgen.writeArrayFieldStart(GeoJson.COORDINATES);

        for (int i = 0; i != value.getNumGeometries(); ++i) {
            writeLineStringCoords(jgen, (LineString) value.getGeometryN(i));
        }

        jgen.writeEndArray();
        jgen.writeEndObject();
    }

    @Override
    public Class<Geometry> handledType() {
        return Geometry.class;
    }

    private void writeMultiPolygon(JsonGenerator jgen, MultiPolygon value)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(GeoJson.TYPE, GeoJson.MULTI_POLYGON);
        jgen.writeArrayFieldStart(GeoJson.COORDINATES);

        for (int i = 0; i != value.getNumGeometries(); ++i) {
            writePolygonCoordinates(jgen, (Polygon) value.getGeometryN(i));
        }

        jgen.writeEndArray();
        jgen.writeEndObject();
    }

    private void writePolygon(JsonGenerator jgen, Polygon value)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(GeoJson.TYPE, GeoJson.POLYGON);
        jgen.writeFieldName(GeoJson.COORDINATES);
        writePolygonCoordinates(jgen, value);

        jgen.writeEndObject();
    }

    private void writePolygonCoordinates(JsonGenerator jgen, Polygon value)
            throws IOException {
        jgen.writeStartArray();
        writeLineStringCoords(jgen, value.getExteriorRing());

        for (int i = 0; i < value.getNumInteriorRing(); ++i) {
            writeLineStringCoords(jgen, value.getInteriorRingN(i));
        }
        jgen.writeEndArray();
    }

    private void writeLineStringCoords(JsonGenerator jgen, LineString ring)
            throws IOException {
        jgen.writeStartArray();
        for (int i = 0; i != ring.getNumPoints(); ++i) {
            Point p = ring.getPointN(i);
            writePointCoords(jgen, p);
        }
        jgen.writeEndArray();
    }

    private void writeLineString(JsonGenerator jgen, LineString lineString)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(GeoJson.TYPE, GeoJson.LINE_STRING);
        jgen.writeFieldName(GeoJson.COORDINATES);
        writeLineStringCoords(jgen, lineString);
        jgen.writeEndObject();
    }

    private void writePoint(JsonGenerator jgen, Point p)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField(GeoJson.TYPE, GeoJson.POINT);
        jgen.writeFieldName(GeoJson.COORDINATES);
        writePointCoords(jgen, p);
        jgen.writeEndObject();
    }

    private void writePointCoords(JsonGenerator jgen, Point p)
            throws IOException {
        jgen.writeStartArray();

        jgen.writeNumber(p.getCoordinate().x);
        jgen.writeNumber(p.getCoordinate().y);

        if (!Double.isNaN(p.getCoordinate().z)) {
            jgen.writeNumber(p.getCoordinate().z);
        }
        jgen.writeEndArray();
    }

}
