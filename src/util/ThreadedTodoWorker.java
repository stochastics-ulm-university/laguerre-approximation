package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class for working on a to-do list of jobs (objects) in parallel.
 * <p>
 * Note that <code>ThreadedTodoWorker.ENABLED</code> has to be set to
 * <code>true</code> for parallel execution of code. The number of threads
 * is automatically determined and may be explicitly controlled by setting
 * the environment variable <code>NUMBER_OF_THREADS</code> or the class
 * variable <code>ThreadedTodoWorker.NUMBER_OF_THREADS</code>.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 */
public class ThreadedTodoWorker {
	
	/** Determines whether multi-threading is enabled. Disabled by default. */
	public static boolean ENABLED = false;
	
	/** Number of threads that may be used, has to be changed (if necessary) before the first call to <code>getThreadExecutor</code>. */
	public static int NUMBER_OF_THREADS = getDefaultNumberOfThreads();
	
	/**
	 * Worker class for <code>workOnTodoList</code>. A single object is used in several threads,
	 * so avoid e.g. instance variables unless you know what you are doing!
	 */
	public interface SimpleTodoWorker<E> {
		public void processTodoItem(E todo);
	}
	
	/** Marks whether we have currently running threads. */
	private static boolean threadsCurrentlyRunning = false;
	
	/**
	 * Starts threads that work on items of a to-do list.
	 * This method doesn't use threads if the number of threads is set
	 * to one, or, if we are already in a thread created by this class.
	 * 
	 * @param todos    the items to process
	 * @param worker   the worker object used to process an item
	 * @param <E> the class of the objects to work on
	 */
	public static <E> void workOnTodoList(Collection<E> todos, final SimpleTodoWorker<E> worker) {
		int todoCount  = todos.size();
		int maxThreads = Math.min(todoCount, ENABLED ? NUMBER_OF_THREADS : 1);
		
		// are we allowed to use threads (and there aren't already threads running...)?
		boolean lockObtained;
		if (maxThreads > 1) {
			lockObtained = getLockOnThreadsCurrentlyRunning();
		} else {
			lockObtained = false;
		}
		
		// now begin, either with threads or without
		if (lockObtained) {
			try {
				ExecutorService threadPool = Executors.newCachedThreadPool();
				try {
					// variable to hold the thread references
					ArrayList<Future<?>> futures = new ArrayList<Future<?>>(maxThreads);
					
					// synchronized copy of to-do list
					final LinkedList<E> todosLinked = new LinkedList<E>(todos);
					
					// now start all the threads
					for (int i = 0; i < maxThreads; i++) {
						Runnable thread = new Runnable() {
							public void run() {
								while (true) {
									E todo = null;
									try {
										synchronized (todosLinked) {
											todo = todosLinked.removeFirst();
										}
									} catch (NoSuchElementException e) {
										// another thread has fetched the last to-do entry, we are done
										break;
									}
									
									worker.processTodoItem(todo);
								}
							}
						};
						futures.add(threadPool.submit(thread));
					}
					
					try {
						// wait until all threads are finished
						for (Future<?> future : futures) {
							future.get();
						}
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (ExecutionException e) {
						if (e.getCause() != null) {
							// throw original exception created in the thread, wrap it with a
							// RuntimeException only if necessary
							if (e.getCause() instanceof RuntimeException) {
								throw (RuntimeException)e.getCause();
							} else {
								throw new RuntimeException(e.getCause());
							}
						} else {
							throw new RuntimeException(e);
						}
					}
				} finally {
					threadPool.shutdown();
				}
			} finally {
				releaseLockOnThreadsCurrentlyRunning();
			}
		} else {
			// no multi-threading or already in a thread: don't start a new thread
			for (E todo : todos) {
				worker.processTodoItem(todo);
			}
		}
	}
	
	/**
	 * Starts threads which work on a single index each, which may
	 * be e.g. a coordinate of a slice in a 3D image.
	 * This method doesn't use threads if the number of threads is set
	 * to one, or, if we are already in a thread created by this class.
	 * 
	 * @param startIndex  the first index
	 * @param endIndex    the last index
	 * @param increment   the increment for the indices (note that a
	 *                    negative increment may be used, this is useful
	 *                    if <code>endIndex &lt; startIndex</code>)
	 * @param worker      the worker object used to process a single index
	 */
	public static void workOnIndices(int startIndex, int endIndex, int increment, SimpleTodoWorker<Integer> worker) {
		if (increment == 0) {
			throw new IllegalArgumentException("The increment has to be non-zero!");
		}
		ArrayList<Integer> list = new ArrayList<Integer>(Math.max(0, (endIndex-startIndex)/increment) + 1);
		if (increment < 0) {
			for (int i = startIndex; i >= endIndex; i += increment) {
				list.add(i);
			}
		} else {
			for (int i = startIndex; i <= endIndex; i += increment) {
				list.add(i);
			}
		}
		workOnTodoList(list, worker);
	}
	
	/**
	 * Checks whether the lock for using threads is already assigned.
	 * 
	 * @return <code>true</code> if the lock exists,
	 *         <code>false</code> if the lock is available
	 */
	public static synchronized boolean hasLockOnThreadsCurrentlyRunning() {
		return threadsCurrentlyRunning;
	}
	
	/**
	 * Tries to get a lock on using threads.
	 * 
	 * @return <code>true</code> if the lock was obtained
	 */
	private static synchronized boolean getLockOnThreadsCurrentlyRunning() {
		if (threadsCurrentlyRunning) {
			return false;
		} else {
			threadsCurrentlyRunning = true;
			return true;
		}
	}
	
	/**
	 * Releases the lock on using threads.
	 * Call this method only if you obtained the lock before with <code>getLockOnThreadsCurrentlyRunning</code>!
	 */
	private static synchronized void releaseLockOnThreadsCurrentlyRunning() {
		threadsCurrentlyRunning = false;
	}
	
	/**
	 * Detects the number of threads that should be used by default, i.e.,
	 * it is the number of processors in the system, which can be overwritten
	 * by an environment variable <code>NUMBER_OF_THREADS</code>.
	 * 
	 * @return the default number of threads
	 */
	public static int getDefaultNumberOfThreads() {
		int result = Runtime.getRuntime().availableProcessors();
		
		String envNumberOfThreads = System.getenv("NUMBER_OF_THREADS");
		if ((envNumberOfThreads != null) && !envNumberOfThreads.isEmpty()) {
			try {
				result = Integer.parseInt(envNumberOfThreads);
			} catch (NumberFormatException e) {
				System.err.println("Could not parse the NUMBER_OF_THREADS environment variable, it will be ignored.");
			}
		}
		
		return result;
	}
	
}
