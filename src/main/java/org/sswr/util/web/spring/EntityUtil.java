package org.sswr.util.web.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public class EntityUtil
{
	public static <T> List<Order> toOrderList(CriteriaBuilder cb, Root<T> rootEntry, Sort sort)
	{
		List<Order> orders = new ArrayList<Order>();
		Iterator<Sort.Order> itOrders = sort.iterator();
		while (itOrders.hasNext())
		{
			Sort.Order order = itOrders.next();
			if (order.isAscending())
			{
				orders.add(cb.asc(rootEntry.get(order.getProperty())));
			}
			else
			{
				orders.add(cb.desc(rootEntry.get(order.getProperty())));
			}
		}
		return orders;
	}

	public static <T> Long getCount(EntityManager entityManager, Specification<T> spec, Class<T> cls)
	{
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> rootEntry = cq.from(cls);
		CriteriaQuery<Long> select = cq.select(cb.count(rootEntry));
		Predicate pred = spec.toPredicate(rootEntry, select, cb);
		select.where(pred);
		return entityManager.createQuery(select).getSingleResult();
	}

	public static <T> Page<T> findAll(EntityManager entityManager, Specification<T> spec, Class<T> cls, Pageable pageable)
	{
		Long cnt = getCount(entityManager, spec, cls);
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(cls);
        Root<T> rootEntry = cq.from(cls);
		Predicate pred = spec.toPredicate(rootEntry, cq, cb);
		cq.where(pred);
		cq.orderBy(toOrderList(cb, rootEntry, pageable.getSort()));
        TypedQuery<T> query = entityManager.createQuery(cq);
		query.setFirstResult((int)pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());
		List<T> results = query.getResultList();
		return new PageImpl<T>(results, pageable, cnt);
	}
}
