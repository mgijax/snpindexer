package org.jax.mgi.snpindexer.util;

import java.io.PrintStream;
import java.util.Date;

import org.apache.log4j.Logger;

public class Timer {
	
	private Date start;
	private PrintStream out;
	private Logger log;
	private Date lastTime = new Date();
	
	public Timer(PrintStream out) {
		this.out = out;
		this.start = new Date();
		StackTraceElement st = Thread.currentThread().getStackTrace()[2];
		out.println(st.getClassName() + ":" + st.getLineNumber() + ": Timer Started: " + 0);
	}

	public Timer(Logger log) {
		this.log = log;
		this.start = new Date();
		StackTraceElement st = Thread.currentThread().getStackTrace()[2];
		log.info(st.getClassName() + ":" + st.getLineNumber() + ": Timer Started: " + 0);
	}

	
	public void time() {
		StackTraceElement st = Thread.currentThread().getStackTrace()[2];
		Date end = new Date();
		if(log != null) {
			log.info(st.getClassName() + ":" + st.getLineNumber() + ": Start: " + (end.getTime() - start.getTime()) + " Last: " + (end.getTime() - lastTime.getTime()));
		}
		if(out != null) {
			out.println(st.getClassName() + ":" + st.getLineNumber() + ": Start: " + (end.getTime() - start.getTime()) + " Last: " + (end.getTime() - lastTime.getTime()));
		}
		lastTime = end;
	}
	

}
