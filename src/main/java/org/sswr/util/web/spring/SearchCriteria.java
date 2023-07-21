package org.sswr.util.web.spring;

import java.sql.Timestamp;
import java.util.List;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class SearchCriteria {

    private String key;
    private Object value;
	private Object value2;
    private SearchOperation operation;

    private SearchCriteria(String key, Object value, SearchOperation operation) {
        this.key = key;
        this.value = value;
        this.operation = operation;
    }

    private SearchCriteria(String key, Object value, Object value2, SearchOperation operation) {
        this.key = key;
        this.value = value;
		this.value2 = value2;
        this.operation = operation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue2() {
        return value2;
    }

    public void setValue2(Object value) {
        this.value2 = value;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public void setOperation(SearchOperation operation) {
        this.operation = operation;
    }

	public static SearchCriteria match(String fieldName, String value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.MATCH);
	}

	public static SearchCriteria equals(String fieldName, String value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.EQUAL);
	}

	public static SearchCriteria equals(String fieldName, Object value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.EQUAL);
	}

	public static <T extends Enum<T>> SearchCriteria equals(String fieldName, T value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.EQUAL);
	}

	public static SearchCriteria notEquals(String fieldName, String value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.NOT_EQUAL);
	}

	public static <T extends Enum<T>> SearchCriteria notEquals(String fieldName, T value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.NOT_EQUAL);
	}

	public static SearchCriteria after(String fieldName, Timestamp value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.DATE_AFTER);
	}

	public static SearchCriteria before(String fieldName, Timestamp value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.DATE_BEFORE);
	}

	public static <T> SearchCriteria in(String fieldName, List<T> valList)
	{
		return new SearchCriteria(fieldName, valList, SearchOperation.IN);
	}

	public static SearchCriteria isNull(String fieldName)
	{
		return new SearchCriteria(fieldName, null, SearchOperation.IS_NULL);
	}

	public static SearchCriteria greaterThan(String fieldName, int value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.GREATER_THAN);
	}

	public static SearchCriteria greaterThanEqual(String fieldName, int value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.GREATER_THAN_EQUAL);
	}

	public static SearchCriteria lessThan(String fieldName, int value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.LESS_THAN);
	}

	public static SearchCriteria lessThanEqual(String fieldName, int value)
	{
		return new SearchCriteria(fieldName, value, SearchOperation.LESS_THAN_EQUAL);
	}

	public static SearchCriteria joinTextMatches(String fieldName, String textLikes, String[] textColumns)
	{
		return new SearchCriteria(fieldName, textLikes, textColumns, SearchOperation.JOIN_MATCH);
	}

	public static SearchCriteria joinWithFilter(String fieldName, List<SearchCriteria> filters)
	{
		return new SearchCriteria(fieldName, filters, SearchOperation.JOIN_FILTER);
	}

	public static SearchCriteria dateIntersect(String fieldNames, Timestamp[] values)
	{
		return new SearchCriteria(fieldNames, values, SearchOperation.DATE_RANGE_INTERSECT);
	}

	public static SearchCriteria geometryInside(String fieldName, String wktString) throws ParseException
	{
		WKTReader wktReader = new WKTReader();
		if (wktReader.read(wktString) != null)
		{
			return new SearchCriteria(fieldName, wktString, SearchOperation.GEOMETRY_INSIDE);
		}
		throw new IllegalArgumentException("wktString is not valid");
	}

	public static SearchCriteria geometryIntersects(String fieldName, String wktString) throws ParseException
	{
		WKTReader wktReader = new WKTReader();
		if (wktReader.read(wktString) != null)
		{
			return new SearchCriteria(fieldName, wktString, SearchOperation.GEOMETRY_INTERSECTS);
		}
		throw new IllegalArgumentException("wktString is not valid");
	}

	public static SearchCriteria geometryDistanceLessThan(String fieldName, String wktString, double distance) throws ParseException
	{
		WKTReader wktReader = new WKTReader();
		if (wktReader.read(wktString) != null)
		{
			return new SearchCriteria(fieldName, wktString, distance, SearchOperation.GEOMETRY_DISTANCE_LESS_THAN);
		}
		throw new IllegalArgumentException("wktString is not valid");
	}
}
