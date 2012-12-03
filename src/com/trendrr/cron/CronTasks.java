/**
 * 
 */
package com.trendrr.cron;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.Reflection;


/**
 * @author Dustin Norlander
 * @created Mar 3, 2011
 * 
 */
public class CronTasks {

	protected Log log = LogFactory.getLog(CronTasks.class);
	
	private List<CronTask> tasks = new ArrayList<CronTask>();
	
	/**
	 * registers an object that has Cron annotated methods 
	 * 
	 * if no methods are found, then no tasks are added.
	 * 
	 * CronTask.execute will be invoked on this specific instance. 
	 * 
	 * @param object
	 * @throws InvalidPatternException 
	 */
	public List<CronTask> register(Object object) throws InvalidPatternException {
		List<CronTask> tasks = register(object.getClass());
		for (CronTask t : tasks) {
			t.setObject(object);
		}
		return tasks;
	}
	
	/**
	 * registers an object that has Cron annotated methods 
	 * 
	 * if no methods are found, then no tasks are added.
	 * 
	 * @param object
	 * @throws InvalidPatternException 
	 */
	public List<CronTask> register(Class cls) throws InvalidPatternException {
		List<CronTask> tasks = new ArrayList<CronTask>();
		for (Method m : cls.getMethods()) {
			Cron c = m.getAnnotation(Cron.class);
			if (c == null)
				continue;
			for (String pattern : c.value()) {
				CronTask t = new CronTask(cls, m, pattern, TimeZone.getTimeZone(c.timezone()));
				this.tasks.add(t);
				tasks.add(t);
			}
		}
		return tasks;
	}
	
	/**
	 * Register an object with the specified pattern and timezone.  
	 * 
	 * note that task.execute will NOT work for this.
	 * 
	 * 
	 * @param pattern
	 * @param object
	 * @return
	 * @throws InvalidPatternException
	 */
	public List<CronTask> register(String pattern, Object object, TimeZone tz) throws InvalidPatternException {
		CronTask t = new CronTask(object.getClass(), null, pattern, tz);
		t.setObject(object);
		this.tasks.add(t);
		
		List<CronTask> tasks = new ArrayList<CronTask>();
		tasks.add(t);
		return tasks;
	}
	
	
	/**
	 * Searches the package for any Cron annotated methods.
	 * 
	 * will log.warn any exceptions and continue on.
	 * 
	 * @param packageName
	 * @param recure
	 * @return
	 */
	public List<CronTask> registerPackage(String packageName, boolean recure) {
		List<CronTask> tasks = new ArrayList<CronTask>();
		List<Class> clss;
		try {
			clss = Reflection.findClasses(this.getClass().getPackage().getName(), true);
		} catch (ClassNotFoundException e) {
			log.warn("Caught", e);
			return tasks;
		}
		for (Class c : clss) {
			try {
				tasks.addAll(this.register(c));
			} catch (InvalidPatternException e) {
				log.warn("Caught", e);
			}
		}
		return tasks;
	}
	
	/**
	 * returns all the tasks that are due to execute at the exact given time.
	 * 
	 * returns empty list on no tasks
	 * @return
	 */
	public List<CronTask> getTasksToExecute(Calendar date) {
		List<CronTask> tasks = new ArrayList<CronTask>();
		// This might be slightly slow if there are tons of possible tasks,
		// Good enough for now...
		for (CronTask t : this.tasks) {
			if (t.getPattern().match(date)) {
				t.setDate(date);
				tasks.add(t);
			}
		}
		return tasks;
	}
	
	/**
	 * returns all the tasks that are due to execute between 
	 * start(inclusive) and end(exclusive)
	 * 
	 * returns empty list on no tasks
	 * @return
	 */
	public List<CronTask> getTasksToExecute(Calendar start, Calendar end) {
		List<CronTask> tasks = new ArrayList<CronTask>();
		// This might be slightly slow if there are tons of possible tasks,
		// Good enough for now...
		for (CronTask t : this.tasks) {
			for (Calendar cal : t.getPattern().getMatchingDates(start, end)) {
				t.setDate(cal);
				tasks.add(t);
			}
			
		}
		return tasks;
	}
	
	/**
	 * convienince method to execute all due tasks.  if different methods have different args,
	 * then use the getTasksToExecute(date), and call execute individually.
	 * 
	 * 
	 * task exceptions are logged and swallowed
	 * @param date
	 * @param args
	 */
	public void executeTasks(Calendar date, Object ...args) {
		List<CronTask> tasks = this.getTasksToExecute(date);
		for (CronTask t : tasks) {
			try {
				t.execute(args);
			} catch (Exception x) {
				log.error("Caught", x);
			}
		}
	}
}
