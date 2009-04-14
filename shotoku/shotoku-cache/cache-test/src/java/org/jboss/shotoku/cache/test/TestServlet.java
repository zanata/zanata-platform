package org.jboss.shotoku.cache.test;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.shotoku.cache.CacheItem;
import org.jboss.shotoku.cache.CacheItemUser;

public class TestServlet extends HttpServlet {
	private CacheItemUser<String,Integer> tci;
	private CacheItemUser<String,Integer> tci2;
	private CacheItemUser<String,Integer> tci3;
	private CacheItemUser<String,Integer> tci4;
	private CacheItemUser<String,Integer> tci5;
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(tci.get("A"));
		System.out.println(tci.get("B"));
		System.out.println(tci2.get("C"));
		System.out.println(tci2.get("D"));
		System.out.println(tci3.get("E"));
		System.out.println(tci3.get("F"));
		System.out.println(tci4.get("G"));
		System.out.println(tci4.get("H"));
		System.out.println(tci5.get("I"));
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		tci = CacheItem.create(new TestCacheItem());
		tci2 = CacheItem.create(new TestCacheItem2());
		tci3 = CacheItem.create(new TestCacheItem());
		tci4 = CacheItem.create(new TestCacheItem2());
		tci5 = CacheItem.create(new TestCacheItem3());
		
		super.init(config);
	}
}
