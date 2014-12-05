package de.uni_hannover.spaceusagerules.core;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.sun.management.OperatingSystemMXBean;

/**
 * A Helper Class to efficiently schedule Threads and therefore have a maximal CPU usage.  
 * @author Fabian Pflug
 *
 */
public class ThreadScheduler {

	/**
	 * schedules a collection of Threads, by running maxparallel in parallel.
	 * returns if all threads finished running and joined.
	 * @param threads a collections of runnable threads
	 * @param maxparralel the maximal number of threads to run in parallel
	 */
	public static void schedule(Collection<? extends Thread> threads, int maxparallel) {
		if(maxparallel==1) {
			for(Thread t : threads) {
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return;
		}
		List<Thread> gens = new LinkedList<Thread>();
		for(Thread g : threads) {
			while(true) {
				if(gens.size()<maxparallel) {
					g.start();
					gens.add(g);
					break;
				} else
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
					}
				for(Thread r : gens)
					if(!r.isAlive()) {
						gens.remove(r);
						break;
					}
			}
		}
		for(Thread g : gens) {
			try {
				g.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * schedules a collection of threads, by starting one thread after another until minLoad is reached.
	 * an optimal value for minLoad would be: 1.0 - 1.0/CPUCORES
	 * so whenever one core is idle, another thread is started
	 * returns if all threads finished running and joined.
	 * @param threads a collections of runnable threads
	 * @param minLoad a minimum number of CPU Load which should be reached.
	 */
	public static void schedule(Collection<? extends Thread> threads, double minLoad, int maxparallel) {
		if(minLoad<=0.1) {
			minLoad = 0.1;
		}
		OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		List<Thread> gens = new LinkedList<Thread>();
		for(Thread g : threads) {
			while(true) {
				for(Thread r : gens)
					if(!r.isAlive()) {
						gens.remove(r);
						break;
					}
				if(gens.size()<maxparallel && bean.getSystemCpuLoad()<minLoad) {
					g.start();
					gens.add(g);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					break;
				} 
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
				}
			}
		}
		for(Thread g : gens) {
			try {
				g.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
