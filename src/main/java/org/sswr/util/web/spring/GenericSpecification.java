package org.sswr.util.web.spring;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.domain.Specification;
import org.sswr.util.db.DBUtil.DBType;

public class GenericSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 1900581010229669687L;

    private final List<SearchCriteria> andList;
    private final List<List<SearchCriteria>> andOrList;
    private final List<SearchCriteria> orList;
    private final List<String> errors;
    private boolean isDistinct;
	private DBType dbType;
	private int srid;

    public GenericSpecification(DBType dbType, int srid) {
        this.andList = new ArrayList<SearchCriteria>();
        this.orList = new ArrayList<SearchCriteria>();
        this.andOrList = new ArrayList<List<SearchCriteria>>();
        this.errors = new ArrayList<String>();
        this.isDistinct = false;
		this.dbType = dbType;
		this.srid = srid;
    }

    public void addAnd(SearchCriteria criteria) {
        andList.add(criteria);
    }

    public void addAndOrList(List<SearchCriteria> criteriaList) {
        andOrList.add(criteriaList);
    }
    public void addOr(SearchCriteria criteria) {
        orList.add(criteria);
    }

	private void parseSearchCriteria(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder, List<Predicate> predicates, SearchCriteria criteria)
	{
		try
		{
			switch (criteria.getOperation())
			{
			case IS_NULL:
				predicates.add(builder.isNull(root.get(criteria.getKey())));
				break;
			case NOT_NULL:
				predicates.add(builder.isNotNull(root.get(criteria.getKey())));
				break;
			case GREATER_THAN:
				predicates.add(builder.greaterThan(
						root.get(criteria.getKey()), criteria.getValue().toString()));
				break;
			case LESS_THAN:
				predicates.add(builder.lessThan(
						root.get(criteria.getKey()), criteria.getValue().toString()));
				break;
			case GREATER_THAN_EQUAL:
				predicates.add(builder.greaterThanOrEqualTo(
						root.get(criteria.getKey()), criteria.getValue().toString()));
				break;
			case LESS_THAN_EQUAL:
				predicates.add(builder.lessThanOrEqualTo(
						root.get(criteria.getKey()), criteria.getValue().toString()));
				break;
			case NOT_EQUAL:
				predicates.add(builder.notEqual(
						root.get(criteria.getKey()).as(criteria.getValue().getClass()), criteria.getValue()));
				break;
			case EQUAL:
				if (criteria.getValue() == null) {
					predicates.add(builder.isNull(
							root.get(criteria.getKey())));

				} else {
					predicates.add(builder.equal(
							root.get(criteria.getKey()).as(criteria.getValue().getClass()), criteria.getValue()));
				}
				break;
			case NOT_MATCH:
				predicates.add(builder.notLike(
						builder.lower(root.get(criteria.getKey())),
						"%" + criteria.getValue().toString().toLowerCase() + "%"));
				break;
			case MATCH:
				predicates.add(builder.like(
						builder.lower(root.get(criteria.getKey())),
						"%" + criteria.getValue().toString().toLowerCase() + "%"));
				break;
			case MATCH_START:
				predicates.add(builder.like(
						builder.lower(root.get(criteria.getKey())),
						"%" + criteria.getValue().toString().toLowerCase()));
				break;
			case MATCH_END:
				predicates.add(builder.like(
						builder.lower(root.get(criteria.getKey())),
						criteria.getValue().toString().toLowerCase() + "%"));
				break;
			case IN:
				predicates.add(
						builder.in(root.get(criteria.getKey())).value(criteria.getValue()));
				break;
			case NOT_IN:
				predicates.add(
						builder.not(root.get(criteria.getKey())).in(criteria.getValue()));
				break;
			case DATE_AFTER:
				predicates.add(
						builder.greaterThanOrEqualTo(root.get(criteria.getKey()).as(Timestamp.class), Timestamp.valueOf(criteria.getValue().toString())));
				break;
			case DATE_BEFORE:
				predicates.add(
						builder.lessThanOrEqualTo(root.get(criteria.getKey()).as(Timestamp.class), Timestamp.valueOf(criteria.getValue().toString())));
				break;
			case DATE_EQUAL:
				predicates.add(builder.equal(
						root.get(criteria.getKey()).as(Timestamp.class), Timestamp.valueOf(criteria.getValue().toString())));
				break;
			case DATE_RANGE:
				{
					Timestamp[] dateRange = (Timestamp[]) criteria.getValue();
					Timestamp dtFrom = dateRange[0];
					Timestamp dtTo = dateRange[1];
					if (dtFrom != null && dtTo != null) {
						predicates.add((builder.between(root.get(criteria.getKey()).as(Timestamp.class), dtFrom, dtTo)));
					}
				}
				break;
			//SubQuery: support textMatch and dateRange Only
			case EXIST:
				{
					@SuppressWarnings("unchecked")
					Map<String, Object> subQueryCriteria = (Map<String, Object>) criteria.getValue();
					@SuppressWarnings("unchecked")
					Subquery<T> subQuery = query.subquery((Class<T>) subQueryCriteria.get("class"));
					@SuppressWarnings("unchecked")
					Root<T> subRoot = subQuery.from((Class<T>) subQueryCriteria.get("class"));
	
					List<Predicate> subPredicate = new ArrayList<>();
					Predicate joinPredicate = builder.equal(subRoot.get((String) subQueryCriteria.get("parent")).get("id"), root.<Integer>get("id"));
					subPredicate.add(joinPredicate);
	
					@SuppressWarnings("unchecked")
					Map<String, Object[]> params = (Map<String, Object[]>) subQueryCriteria.get("values");
					for (Map.Entry<String, Object[]> entry : params.entrySet()) {
						switch ((SearchOperation) entry.getValue()[1]) {
							case DATE_AFTER:
								subPredicate.add(
										builder.greaterThanOrEqualTo(subRoot.get(entry.getKey()).as(Timestamp.class), Timestamp.valueOf(entry.getValue()[0].toString())));
	
								break;
							case DATE_BEFORE:
								subPredicate.add(
										builder.lessThanOrEqualTo(subRoot.get(entry.getKey()).as(Timestamp.class), Timestamp.valueOf(entry.getValue()[0].toString())));
								break;
							case MATCH:
	
								subPredicate.add(builder.like(
										builder.lower(subRoot.get(entry.getKey())),
										"%" + ((String) entry.getValue()[0]).toLowerCase() + "%"));
								break;
							case DATE_EQUAL:
								break;
							case DATE_RANGE:
								break;
							case EITHER_MATCH:
								break;
							case EQUAL:
								break;
							case EXIST:
								break;
							case GEOMETRY_INSIDE:
								break;
							case GEOMETRY_INTERSECTS:
								break;
							case GREATER_THAN:
								break;
							case GREATER_THAN_EQUAL:
								break;
							case IN:
								break;
							case IN_MATCH:
								break;
							case IS_NULL:
								break;
							case JOIN_MATCH:
								break;
							case LESS_THAN:
								break;
							case LESS_THAN_EQUAL:
								break;
							case MATCH_END:
								break;
							case MATCH_START:
								break;
							case NOT_EQUAL:
								break;
							case NOT_IN:
								break;
							case NOT_MATCH:
								break;
							case NOT_NULL:
								break;
							case SUBQFIELDMATCH:
								break;
							default:
								break;
						}
					}
	
					subQuery.select(subRoot).where(subPredicate.toArray(new Predicate[0]));
					predicates.add(builder.exists(subQuery));
				}
				break;
			case EITHER_MATCH:
				{
					@SuppressWarnings("unchecked")
					Map<String, String> eList = (Map<String, String>) criteria.getValue();
					List<Predicate> subPredicates = new ArrayList<>();
					for (Map.Entry<String, String> entry : eList.entrySet()) {
						subPredicates.add(builder.like(
								builder.lower(root.get(entry.getKey())),
								"%" + entry.getValue().toLowerCase() + "%"));
					}
					predicates.add(builder.or(subPredicates.toArray(new Predicate[0])));
				}
				break;
			//SubQuery: support textMatch and dateRange Only
			case IN_MATCH:
				{
					@SuppressWarnings("unchecked")
					Map<String, Object> subQueryCriteria = (Map<String, Object>) criteria.getValue();
					@SuppressWarnings("unchecked")
					Subquery<T> subQuery = query.subquery((Class<T>) subQueryCriteria.get("class"));
					@SuppressWarnings("unchecked")
					Root<T> subRoot = subQuery.from((Class<T>) subQueryCriteria.get("class"));

					List<Predicate> subPredicate = new ArrayList<>();

					@SuppressWarnings("unchecked")
					Map<String, Object[]> params = (Map<String, Object[]>) subQueryCriteria.get("values");
					for (Map.Entry<String, Object[]> entry : params.entrySet()) {
						switch ((SearchOperation) entry.getValue()[1]) {
							case DATE_AFTER:
								subPredicate.add(
										builder.greaterThanOrEqualTo(subRoot.get(entry.getKey()).as(Timestamp.class), Timestamp.valueOf(entry.getValue()[0].toString())));

								break;
							case DATE_BEFORE:
								subPredicate.add(
										builder.lessThanOrEqualTo(subRoot.get(entry.getKey()).as(Timestamp.class), Timestamp.valueOf(entry.getValue()[0].toString())));
								break;
							case MATCH:
								subPredicate.add(builder.like(
										builder.lower(subRoot.get(entry.getKey())),
										"%" + ((String) entry.getValue()[0]).toLowerCase() + "%"));
								break;
							case SUBQFIELDMATCH:
								subPredicate.add(builder.like(
										builder.lower(subRoot.get(entry.getKey()).get((String) entry.getValue()[2])),
										"%" + ((String) entry.getValue()[0]).toLowerCase() + "%"));
								break;
							case EQUAL:
								if(entry.getValue()[0] == null)
									subPredicate.add(builder.isNull(subRoot.get(entry.getKey())));
								else if(entry.getValue().length >=3 )
									subPredicate.add(builder.equal(
											subRoot.get(entry.getKey()).get((String) entry.getValue()[2]).as(entry.getValue()[0].getClass()),
											entry.getValue()[0]));
								else
									subPredicate.add(builder.equal(subRoot.get(entry.getKey()).as(entry.getValue()[0].getClass()), entry.getValue()[0]));
								break;
							case DATE_EQUAL:
								break;
							case DATE_RANGE:
								break;
							case EITHER_MATCH:
								break;
							case EXIST:
								break;
							case GEOMETRY_INSIDE:
								break;
							case GEOMETRY_INTERSECTS:
								break;
							case GREATER_THAN:
								break;
							case GREATER_THAN_EQUAL:
								break;
							case IN:
								break;
							case IN_MATCH:
								break;
							case IS_NULL:
								break;
							case JOIN_MATCH:
								break;
							case LESS_THAN:
								break;
							case LESS_THAN_EQUAL:
								break;
							case MATCH_END:
								break;
							case MATCH_START:
								break;
							case NOT_EQUAL:
								break;
							case NOT_IN:
								break;
							case NOT_MATCH:
								break;
							case NOT_NULL:
								break;
							default:
								break;
						}
					}

					subPredicate.add(builder.notEqual(subRoot.get("status"), 2)); //filter delete status
					subQuery.select(subRoot).where(builder.and(subPredicate.toArray(new Predicate[0])));
					predicates.add(builder.in(root.get((String) subQueryCriteria.get("parent"))).value(subQuery));
				}
				break;
			//SubQuery: support textMatch and dateRange Only
			case JOIN_MATCH:
				{
					@SuppressWarnings("unchecked")
					Map<String, String[]> joinCriteria = (Map<String, String[]>) criteria.getValue();

					String joinField = joinCriteria.get("joinField")[0];
					String joinValue = "%" + joinCriteria.get("joinValue")[0] + "%";
					String[] joinColumns = (String[]) joinCriteria.get("joinColumns");
					Join<Object, Object> landmarks = root.join(joinField, JoinType.LEFT);


					List<Predicate> joinOrPredicates = new ArrayList<>();
					for (String colName : joinColumns) {
						joinOrPredicates.add(builder.like(landmarks.get(colName), joinValue));
					}
					predicates.add(builder.or(joinOrPredicates.toArray(new Predicate[0])));

					//Join may cause duplicate reocrd, make distinct
					isDistinct = true;
				}
				break;
			case SUBQFIELDMATCH:
				System.out.println("SUBQFIELDMATCH not supported");
				errors.add("SUBQFIELDMATCH not supported");
				break;
			case GEOMETRY_INSIDE:
				if (dbType == DBType.MSSQL)
				{
					predicates.add(builder.equal(builder.function(criteria.getKey()+".STWithin", Integer.class, builder.function("geometry::STGeomFromText", Geometry.class, builder.literal((String)criteria.getValue()), builder.literal(srid))), 1));
				}
				else if (dbType == DBType.MySQL || dbType == DBType.PostgreSQL)
				{
					predicates.add(builder.equal(builder.function("ST_Within", Integer.class, root.get(criteria.getKey()).as(Geometry.class), builder.function("ST_GeomFromText", Geometry.class, builder.literal((String)criteria.getValue()), builder.literal(srid))), 1));
				}
				else
				{
					System.out.println("GEOMETRY_INSIDE is not supported for "+dbType);
					errors.add("GEOMETRY_INSIDE is not supported for "+dbType);
				}
				break;
			case GEOMETRY_INTERSECTS:
				if (dbType == DBType.MSSQL)
				{
					predicates.add(builder.equal(builder.function(criteria.getKey()+".STIntersects", Integer.class, builder.function("geometry::STGeomFromText", Geometry.class, builder.literal((String)criteria.getValue()), builder.literal(srid))), 1));
				}
				else if (dbType == DBType.MySQL || dbType == DBType.PostgreSQL)
				{
					predicates.add(builder.equal(builder.function("ST_Intersects", Integer.class, root.get(criteria.getKey()).as(Geometry.class), builder.function("ST_GeomFromText", Geometry.class, builder.literal((String)criteria.getValue()), builder.literal(srid))), 1));
				}
				else
				{
					System.out.println("GEOMETRY_INTERSECTS is not supported for "+dbType);
					errors.add("GEOMETRY_INTERSECTS is not supported for "+dbType);
				}
				break;
		}
		} catch (Exception ex) {
			ex.printStackTrace();
			errors.add(ex.getMessage());
		}
	}

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        //create a new predicate list
        List<Predicate> predicates = new ArrayList<>();
        //Or Criteria
        List<Predicate> predicatesOr = new ArrayList<>();
        //And Or Criteria
        List<Predicate> predicatesAndOr = new ArrayList<>();

        List<Predicate> finalPredicates = new ArrayList<>();

        //And Criteria
        for (SearchCriteria criteria : andList) {
			parseSearchCriteria(root, query, builder, predicates, criteria);
        }

        for (SearchCriteria criteria : orList) {
			parseSearchCriteria(root, query, builder, predicatesOr, criteria);
        }

        for (List<SearchCriteria> orList : andOrList) {
            for (SearchCriteria criteria : orList) {
				parseSearchCriteria(root, query, builder, predicatesAndOr, criteria);
            }
        }


        if(predicates.size() > 0)
            finalPredicates.add(builder.and(predicates.toArray(new Predicate[0])));

        if(predicatesOr.size() > 0)
            finalPredicates.add(builder.or(predicatesOr.toArray(new Predicate[0])));

        if(predicatesAndOr.size() > 0)
            finalPredicates.add(builder.or(predicatesAndOr.toArray(new Predicate[0])));

        if(isDistinct)
            query.distinct(true);
        return builder.and(finalPredicates.toArray(new Predicate[0]));
    }



    public List<String> getErrors() {
        return errors;
    }
}
