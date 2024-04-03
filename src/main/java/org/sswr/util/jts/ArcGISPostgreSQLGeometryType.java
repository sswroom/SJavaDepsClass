package org.sswr.util.jts;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class ArcGISPostgreSQLGeometryType extends AbstractSingleColumnStandardBasicType<Geometry> {
   public ArcGISPostgreSQLGeometryType() {
        super(BinaryTypeDescriptor.INSTANCE, GeometryJavaTypeDescriptior.INSTANCE);
    }

    @Override
    public String getName() {
        return "st_geometry.WKB";
    }

    public static class GeometryJavaTypeDescriptior extends AbstractTypeDescriptor<Geometry> {
        public static final GeometryJavaTypeDescriptior INSTANCE = new GeometryJavaTypeDescriptior(Geometry.class);

        protected GeometryJavaTypeDescriptior(Class<Geometry> type) {
            super(type);
        }

        @Override
        public String toString(Geometry value) {
            return new WKTWriter().write(value);
        }

        @Override
        public Geometry fromString(String string) {
			try
			{
				return new WKTReader().read(string);
			}
			catch (ParseException ex)
			{
				ex.printStackTrace();
				return null;
			}
        }

        @Override
        public <X> X unwrap(Geometry value, Class<X> type, WrapperOptions options) {
            if (value == null) {
                return null;
            }

            if (Geometry.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				X ret = (X) value;
                return ret;
            }
			if (byte[].class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				X ret = (X) new WKBWriter().write(value);
				return ret;
			}
            if (String.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				X ret = (X) toString(value);
                return ret;
            }
			System.out.println(type.toString());
            throw unknownUnwrap(type);
        }

        @Override
        public <X> Geometry wrap(X value, WrapperOptions options) {
            if (value == null) {
                return null;
            }
            if (Geometry.class.isInstance(value)) {
                return (Geometry) value;
            }
            if (String.class.isInstance(value)) {
                return fromString((String) value);
            }
			if (byte[].class.isInstance(value))
			{
				byte[] v = (byte[])value;
				try
				{
					return new WKBReader().read(v);
				}
				catch (ParseException ex)
				{
					ex.printStackTrace();
					return null;
				}
			}
            throw unknownWrap(value.getClass());
        }
    }
}
