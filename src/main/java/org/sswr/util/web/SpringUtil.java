package org.sswr.util.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

public class SpringUtil {
	public static Pageable createPageable(HttpServletRequest req)
	{
		String s;
		int page = 0;
		int size = 20;
		s = req.getParameter("page");
		if (s != null)
		{
			try
			{
				page = Integer.parseInt(s);
			}
			catch (Exception ex)
			{
			}
		}
		s = req.getParameter("size");
		if (s != null)
		{
			try
			{
				size = Integer.parseInt(s);
			}
			catch (Exception ex)
			{
			}
		}
		String sarr[] = req.getParameterValues("sort");
		if (sarr != null)
		{
			List<Order> orders = new ArrayList<Order>();
			int i = 0;
			int j = sarr.length;
			int k;
			while (i < j)
			{
				if (sarr[i].length() > 0)
				{
					k = sarr[i].indexOf(",");
					if (k > 0)
					{
						if (sarr[i].toUpperCase().endsWith(",DESC"))
						{
							orders.add(Order.desc(sarr[i].substring(0, k)));
						}
						else
						{
							orders.add(Order.asc(sarr[i].substring(0, k)));
						}
					}
					else
					{
						orders.add(Order.asc(sarr[i]));
					}
				}
				i++;
			}
			return PageRequest.of(page, size, Sort.by(orders));
		}
		else
		{
			return PageRequest.of(page, size);
		}
	}
}
