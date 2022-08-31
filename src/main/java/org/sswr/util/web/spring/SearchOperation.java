package org.sswr.util.web.spring;

public enum SearchOperation {
    IS_NULL,
    NOT_NULL,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQUAL,
    LESS_THAN_EQUAL,
    NOT_EQUAL,
    EQUAL,
    NOT_MATCH,
    MATCH,
    MATCH_START,
    MATCH_END,
    IN,
    NOT_IN,
    DATE_AFTER,
    DATE_BEFORE,
    DATE_EQUAL,
    DATE_RANGE,
    EXIST,
    EITHER_MATCH,
    IN_MATCH,
    JOIN_MATCH,
    SUBQFIELDMATCH,
	GEOMETRY_INSIDE,
	GEOMETRY_INTERSECTS
}
