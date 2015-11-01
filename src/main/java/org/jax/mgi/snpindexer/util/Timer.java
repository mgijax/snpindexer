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
		out.println("Timer Started: " + 0);
	}

	public Timer(Logger log) {
		this.log = log;
		this.start = new Date();
		log.info("Timer Started: " + 0);
	}

	public void time() {
		Date end = new Date();
		if(log != null) {
			log.info("Timer from start: " + (end.getTime() - start.getTime()));
			log.info("Timer from last: " + (end.getTime() - lastTime.getTime()));
		}
		if(out != null) {
			out.println("Timer from start: " + (end.getTime() - start.getTime()));
			out.println("Timer from last: " + (end.getTime() - lastTime.getTime()));
		}
		lastTime = end;
	}
	

}
