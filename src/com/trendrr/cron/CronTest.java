/**
 * 
 */
package com.trendrr.cron;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.Timeframe;
import com.trendrr.oss.IsoDateUtil;


/**
 * @author Dustin Norlander
 * @created May 19, 2011
 * 
 */
public class CronTest {

	protected Log log = LogFactory.getLog(CronTest.class);
	
//	public static void main(String ...strings) {
//		new CronTest().test();
//		
//	}
	
	@Test
	public void testGetTasksToExecute() throws Exception{
		CronTasks tasks = new CronTasks();

		tasks.register("33 * * * *", "33", TimeZone.getDefault());
		
		tasks.register("44 * * * *", "44", TimeZone.getDefault());
		Calendar start = Calendar.getInstance();
		start.add(Calendar.MINUTE, -60);
		
		List<CronTask> t = tasks.getTasksToExecute(start, Calendar.getInstance());
		Assert.assertEquals(t.size(), 2);
	}
	
	@Test
	public void test() {
		Date date = IsoDateUtil.parse("2011-05-19T17:21:07Z");
		CronTasks tasks = new CronTasks();
		try {
			tasks.register(this.getClass());
		} catch (InvalidPatternException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
		List<CronTask> t = new ArrayList<CronTask>();
		while (t.isEmpty()) {
			date = Timeframe.MINUTES.add(date, 1);
//			System.out.println(date);
			cal.setTime(date);
			t = tasks.getTasksToExecute(cal);
		}
		for (CronTask task : t) {
			try {
				task.execute(date);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Cron("daily")
	public void daily(Date date) {
		System.out.println("TO EXECUTE ON: " + date);
	}
	
	@Test
	public void testNextMatch() throws InvalidPatternException{
		Date date = IsoDateUtil.parse("2011-01-03T00:01:07Z");
		System.out.println("Start date is " + date.toString());

		Map<String, String> patternMap = new HashMap<String, String>();
		patternMap.put("hourly", "2011-01-03T01:00:07.00Z");
		patternMap.put("daily", "2011-01-03T05:00:07.00Z");
		patternMap.put("weekly", "2011-01-09T05:00:07.00Z");
		patternMap.put("monthly", "2011-02-01T05:00:07.00Z");
		patternMap.put("yearly", "2012-01-01T05:00:07.00Z");
		for(String pattern : patternMap.keySet()){
			SchedulingPattern sp = new SchedulingPattern(pattern);
			Date start = new Date();
			Date next = sp.getNextMatchingDate(date);
			long millis = new Date().getTime() - start.getTime();
			assertEquals(IsoDateUtil.getIsoDate(next), patternMap.get(pattern));
			System.out.println("Calculated " + pattern + " in " + millis + " millis. It was " + IsoDateUtil.getIsoDate(next));
		}
	}
	
	
	@Test
	public void testPrevMatch() throws InvalidPatternException{
		Date date = IsoDateUtil.parse("2011-01-03T00:01:00Z");
		System.out.println("Start date is " + date.toString());

		Map<String, String> patternMap = new HashMap<String, String>();
		patternMap.put("hourly", "2011-01-03T00:00:00.00Z");
//		patternMap.put("daily", "2011-01-03T05:00:07.00Z");
//		patternMap.put("weekly", "2011-01-09T05:00:07.00Z");
//		patternMap.put("monthly", "2011-02-01T05:00:07.00Z");
//		patternMap.put("yearly", "2012-01-01T05:00:07.00Z");
		for(String pattern : patternMap.keySet()){
			SchedulingPattern sp = new SchedulingPattern(pattern);
			Date start = new Date();
			Date next = sp.getPrevMatchingDate(date);
			long millis = new Date().getTime() - start.getTime();
			assertEquals(IsoDateUtil.getIsoDate(next), patternMap.get(pattern));
			System.out.println("Calculated " + pattern + " in " + millis + " millis. It was " + IsoDateUtil.getIsoDate(next));
		}
	}
	
	/**
	 * Parse date and go to the start of the minute (good enough for us)
	 * @param isoDate
	 * @return
	 */
	private Date normalize(String isoDate){
		Date date = IsoDateUtil.parse(isoDate);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}
}
