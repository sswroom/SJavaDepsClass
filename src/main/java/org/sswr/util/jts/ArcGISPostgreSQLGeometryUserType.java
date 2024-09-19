package org.sswr.util.jts;

import java.util.function.BiConsumer;

import org.hibernate.type.descriptor.java.BasicJavaType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.usertype.BaseUserTypeSupport;
import org.locationtech.jts.geom.Geometry;
import org.sswr.util.jts.ArcGISPostgreSQLGeometryType.GeometryJavaType;

public class ArcGISPostgreSQLGeometryUserType extends BaseUserTypeSupport<Geometry> {
	
	@Override
	protected void resolve(BiConsumer<BasicJavaType<Geometry>, JdbcType> resolutionConsumer) {
		resolutionConsumer.accept(GeometryJavaType.INSTANCE, BinaryJdbcType.INSTANCE);
	}
}
