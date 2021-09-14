package org.sswr.util.jts;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.sswr.util.jts.parsers.GenericGeometryParser;
import org.sswr.util.jts.parsers.GeometryCollectionParser;
import org.sswr.util.jts.parsers.LineStringParser;
import org.sswr.util.jts.parsers.MultiLineStringParser;
import org.sswr.util.jts.parsers.MultiPointParser;
import org.sswr.util.jts.parsers.MultiPolygonParser;
import org.sswr.util.jts.parsers.PointParser;
import org.sswr.util.jts.parsers.PolygonParser;
import org.sswr.util.jts.serialization.GeometryDeserializer;
import org.sswr.util.jts.serialization.GeometrySerializer;

public class JtsModule extends SimpleModule
{
    public JtsModule()
	{
        this(new GeometryFactory());
    }

    public JtsModule(GeometryFactory geometryFactory)
	{
        super("JtsModule", new Version(1, 0, 0, null, "org.sswr", "util"));

        addSerializer(Geometry.class, new GeometrySerializer());
        GenericGeometryParser genericGeometryParser = new GenericGeometryParser(geometryFactory);
        addDeserializer(Geometry.class, new GeometryDeserializer<Geometry>(genericGeometryParser));
        addDeserializer(Point.class, new GeometryDeserializer<Point>(new PointParser(geometryFactory)));
        addDeserializer(MultiPoint.class, new GeometryDeserializer<MultiPoint>(new MultiPointParser(geometryFactory)));
        addDeserializer(LineString.class, new GeometryDeserializer<LineString>(new LineStringParser(geometryFactory)));
        addDeserializer(MultiLineString.class, new GeometryDeserializer<MultiLineString>(new MultiLineStringParser(geometryFactory)));
        addDeserializer(Polygon.class, new GeometryDeserializer<Polygon>(new PolygonParser(geometryFactory)));
        addDeserializer(MultiPolygon.class, new GeometryDeserializer<MultiPolygon>(new MultiPolygonParser(geometryFactory)));
        addDeserializer(GeometryCollection.class, new GeometryDeserializer<GeometryCollection>(new GeometryCollectionParser(geometryFactory, genericGeometryParser)));
    }

    @Override
    public void setupModule(SetupContext context)
	{
        super.setupModule(context);
    }
}
