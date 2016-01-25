package spy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 */
public class Spy {
	private static final int DEFAULT_CHUNK_SIZE = 10000;

	/**
	 * 
	 */
	private static final class SpyCallable implements Callable<Boolean> {
		private final int end;
		private final int n;
		private final Secret secret;
		private final Spy spy;
		private final int start;

		/**
		 * @param spy
		 * @param secret
		 * @param n
		 * @param start
		 * @param end
		 */
		public SpyCallable(Spy spy, Secret secret, int n, int start, int end) {
			this.spy = spy;
			this.secret = secret;
			this.n = n;
			this.start = start;
			this.end = end;
		}

		@Override
		public Boolean call() throws Exception {
			boolean result = spy.isRangeAdditive(secret, n, start, end);

			spy.incrementCompletedCount();
			if (!result) {
				spy.setResultFalse();
			}

			return result;
		}
	}

	private static final String __ = "\r\n";

	private static final DateFormat DATE_TIME_INSTANCE = SimpleDateFormat.getDateTimeInstance();

	private static final String USAGE = "usage: spy N <secretclassname> [option]...\r\n"
			+ "Spy determines if Secret is an additive function such that secret(x+y) = secret(x) + secret(y)\r\n"
			+ "for all combinations of prime numbers less than N.\r\n"
			+ __
			+ __
			+ "Required arguments ---------------------------------------------------------------------\r\n"
			+ __
			+ "N                         the value of N used in the analysis\r\n"//
			+ "<secretclassname>         the class to be examined. Must implement the interface\r\n"
			+ "                          "
			+ Secret.class.getCanonicalName()
			+ __
			+ __
			+ __
			+ "Options --------------------------------------------------------------------------------\r\n"//
			+ __//
			+ "-chunksize <integer>      the number of primes each thread will check (default =\r\n"
			+ "                          "
			+ DEFAULT_CHUNK_SIZE
			+ "). Only used when the number of threads is > 1\r\n"//
			+ "-debug                    run a set of known Secrets as a self-diagnostic\r\n"
			+ "-help                     print this message\r\n"
			+ "-primesource <classname>  the class which provides the prime numbers used in the\r\n"
			+ "                          analysis. Must implement the interface "//
			+ PrimeSource.class.getCanonicalName()
			+ "\r\n"
			+ "-silent                   disable logging\r\n"
			+ "-threads <integer>        the maximum number of threads to run (default = 1)\r\n"
			+ "-verbose                  maximum logging\r\n";

	/**
	 * @param s
	 */
	private static void _log(String s) {
		System.out.println(DATE_TIME_INSTANCE.format(new Date()) + ": " + s);
	}

	/**
	 * @param args
	 * @param token
	 * @param isTuple
	 * @return
	 */
	private static String getArg(String[] args, String token, boolean isTuple) {
		String result = null;

		for (int i = 0, imax = args.length; i < imax && result == null; i++) {
			String s = args[i];
			if (token.equals(s)) {
				if (isTuple) {
					if (i + 1 < imax) {
						result = args[i + 1];
						i++;
					}
				} else {
					result = token;
				}
			}
		}

		return result;
	}

	/**
	 * @param args
	 * @return
	 */
	private static int getChunkSize(String[] args) {
		int result;

		String chunkSize = getArg(args, "-chunkSize", true);

		if (chunkSize != null && !chunkSize.isEmpty()) {
			result = Integer.parseInt(chunkSize);
		} else {
			result = DEFAULT_CHUNK_SIZE;
		}

		return result;
	}

	/**
	 * @param args
	 * @return
	 */
	private static LogLevel getLogLevel(String[] args) {
		LogLevel result;

		if (isArg(args, "-verbose", false)) {
			result = LogLevel.Verbose;
		} else if (isArg(args, "-silent", false)) {
			result = LogLevel.None;
		} else {
			result = LogLevel.Normal;
		}

		return result;
	}

	/**
	 * @param args
	 * @return
	 */
	private static int getN(String[] args) {
		return Integer.parseInt(args[0]);
	}

	/**
	 * @param args
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static PrimeSource getPrimeSource(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		PrimeSource result;

		String classname = getArg(args, "-primesource", true);

		if (classname != null) {
			@SuppressWarnings("rawtypes")
			Class c = Class.forName(classname);
			if (PrimeSource.class.isAssignableFrom(c)) {
				result = (PrimeSource) c.newInstance();
			} else {
				throw new IllegalArgumentException("Invalid value for "
						+ PrimeSource.class.getSimpleName() + ".  '" + classname
						+ "' is not an instance of " + PrimeSource.class.getCanonicalName());
			}
		} else {
			result = new DefaultPrimeSource();
		}

		return result;
	}

	/**
	 * @param args
	 * @return
	 */
	private static int getThreadCount(String[] args) {
		String threads = getArg(args, "-threads", true);

		int result = threads != null && threads.length() > 0 ? Integer.parseInt(threads) : 1;

		if (result > 1) {
			_log("ThreadCount " + result);
		}

		return result;
	}

	/**
	 * @param args
	 * @param token
	 * @param isTuple
	 * @return
	 */
	private static boolean isArg(String[] args, String token, boolean isTuple) {
		return getArg(args, token, isTuple) != null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if (args.length < 2 || isArg(args, "-help", false)) {
				System.out.println(USAGE);
			} else if (isArg(args, "-debug", false)) {
				int n = getN(args);

				for (Secret s : new Secret[] { new SecretSquare(), new SecretSwitch(),
						new SecretDouble() }) {

					_log("Analyzing " + s.getClass().getCanonicalName() + "...");

					Spy spy = newDebugSpy(args);

					boolean isAdditive = spy.isAdditive(s, n);

					_log(s.getClass().getCanonicalName() + (isAdditive ? " IS" : " is NOT")
							+ " an additive function for N = " + n + ".");
				}
			} else {
				Secret s = newSecret(args[1]);

				int n = getN(args);

				_log("Analyzing " + s.getClass().getCanonicalName() + "...");

				boolean isAdditive = newSpy(args).isAdditive(s, n);

				_log(s.getClass().getCanonicalName() + (isAdditive ? " IS" : " is NOT")
						+ " an additive function for N = " + n + ".");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static final Spy newDebugSpy(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		return new Spy(getPrimeSource(args), getThreadCount(args), getChunkSize(args),
				LogLevel.Verbose);
	}

	/**
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static Secret newSecret(String className) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Secret result;

		@SuppressWarnings("rawtypes")
		Class c = Class.forName(className);
		if (Secret.class.isAssignableFrom(c)) {
			result = (Secret) c.newInstance();
		} else {
			throw new IllegalArgumentException("Invalid value for " + Secret.class.getSimpleName()
					+ ".  '" + className + "' is not an instance of "
					+ Secret.class.getCanonicalName());
		}

		return result;
	}

	/**
	 * @param args
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static final Spy newSpy(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		return new Spy(getPrimeSource(args), getThreadCount(args), getChunkSize(args),
				getLogLevel(args));
	}

	private final int chunkSize;
	private int completedCount;
	private boolean concurrentResult = true;
	private final LogLevel logLevel;
	private int maxN;
	private int[] primes;
	private final PrimeSource primeSource;
	private int submittedCount;
	private final PrimeTest test;
	private final int threadCount;

	/**
	 * 
	 */
	public Spy() {
		this(new DefaultPrimeSource(), 1, DEFAULT_CHUNK_SIZE, LogLevel.None);
	}

	/**
	 * @param primeSource
	 * @param threadCount
	 * @param chunkSize
	 * @param logLevel
	 */
	public Spy(PrimeSource primeSource, int threadCount, int chunkSize, LogLevel logLevel) {
		this.primeSource = primeSource;
		this.threadCount = threadCount;
		this.chunkSize = chunkSize;
		this.logLevel = logLevel;
		this.test = new PrimeTest(primeSource, threadCount > 1 ? LogLevel.None : logLevel);
	}

	/**
	 * @return
	 */
	private synchronized int getQueuedSize() {
		return submittedCount - completedCount;
	}

	/**
	 * 
	 */
	private synchronized void incrementCompletedCount() {
		completedCount++;
	}

	/**
	 * @param n
	 */
	private void initializePrimes(int n) {
		long millis = System.currentTimeMillis();
		log("Initializing primes using " + primeSource.getClass().getCanonicalName() + "...");

		primes = primeSource.getPrimes(n);
		maxN = n;

		log("There are "
				+ primes.length
				+ " primes < "
				+ n
				+ "."
				+ (primes.length > 0 ? "  The largest of which is " + primes[primes.length - 1]
						: "") + ".");
		log("Initialized primes in " + (System.currentTimeMillis() - millis));
	}

	/**
	 * @param secret
	 * @param n
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public boolean isAdditive(Secret secret, int n) throws InterruptedException, ExecutionException {
		boolean result;

		if (n > 1) {
			if (primes == null || maxN < n) {
				initializePrimes(n);
			}

			if (threadCount > 1) {
				result = isAdditive(secret, n, chunkSize);
			} else {
				result = test.isAdditive(secret, n, primes);
			}
		} else {
			result = false;
		}

		return result;
	}

	/**
	 * Determines if Secret is an additive function [secret(x+y) = secret(x) + secret(y)] for all
	 * combinations of prime numbers less than <i>n</i>.
	 * 
	 * @param secret
	 * @param n
	 * @param chunkSize
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private boolean isAdditive(Secret secret, int n, int chunkSize) throws InterruptedException,
			ExecutionException {
		boolean result = true;

		ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);

		int minQueuedSize = Math.max(100, 100 * threadCount);

		int chunks = primes.length / chunkSize;
		if (primes.length % chunkSize > 0) {
			chunks++; // accounts for partial chunk
		}

		for (int c = 0, cmax = chunks, start = c; c < cmax && result && primes[start] < n; c++, start += chunkSize) {
			threadPool.submit(new SpyCallable(this, secret, n, start, start + chunkSize));
			submittedCount++;

			if (c > 0 && c % minQueuedSize == 0) {
				while (concurrentResult && getQueuedSize() > minQueuedSize) {
					Thread.sleep(1000);
					logStatus(chunkSize);
				}

				result = concurrentResult;
			}
		}

		return pollTasks(threadPool);
	}

	/**
	 * Determines if Secret is an additive function [secret(x+y) = secret(x) + secret(y)] for all
	 * combinations of prime numbers less than <i>n</i>.
	 * 
	 * @param secret
	 * @param n
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private boolean isRangeAdditive(Secret secret, int n, int start, int end)
			throws InterruptedException, ExecutionException {
		return test.isAdditiveRange(secret, n, primes, start, end);
	}

	/**
	 * @param s
	 */
	private void log(String s) {
		if (!logLevel.is(LogLevel.None)) {
			_log(s);
		}
	}

	/**
	 * @param chunkSize
	 */
	private void logStatus(int chunkSize) {
		if (logLevel.is(LogLevel.Verbose)) {
			log(completedCount * chunkSize / 1000 + "K primes completed. " + getQueuedSize()
					+ " queued.");
		}
	}

	/**
	 * @param threadPool
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private boolean pollTasks(ExecutorService threadPool) throws InterruptedException,
			ExecutionException {

		boolean isDone = !concurrentResult || getQueuedSize() < 1;
		while (!isDone) {
			Thread.sleep(1000);
			isDone = !concurrentResult || getQueuedSize() < 1;
			logStatus(chunkSize);
		}

		threadPool.shutdownNow();

		return concurrentResult;
	}

	/**
	 * 
	 */
	private void setResultFalse() {
		// no need to synchronize
		concurrentResult = false;
	}
}