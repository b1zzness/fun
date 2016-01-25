package spy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * 
 */
public class PrimeTest {
	private static final DateFormat DATE_TIME_INSTANCE = SimpleDateFormat.getDateTimeInstance();

	private final LogLevel log;

	/**
	 * @param primeSource
	 * @param isVerbose
	 * @param isLogging
	 */
	public PrimeTest(PrimeSource primeSource, LogLevel log) {
		this.log = log;
	}

	/**
	 * Determines if Secret is an additive function [secret(x+y) = secret(x) + secret(y)] for all
	 * combinations of prime numbers less than <i>n</i>.
	 * 
	 * @param secret
	 * @param n
	 * @param primes
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public boolean isAdditive(Secret secret, int n, int[] primes) {
		boolean result = n > 1;

		if (result) {
			result = isAdditiveRange(secret, n, primes, 0, primes.length);
		}

		return result;
	}

	/**
	 * @param secret
	 * @param n
	 * @param primes
	 * @param start
	 * @param end
	 * @return
	 */
	protected boolean isAdditiveRange(Secret secret, int n, int[] primes, int start, int end) {
		boolean result = n > 1;

		if (result) {
			long startMillis = System.currentTimeMillis();

			if (end > primes.length) {
				end = primes.length;
			}

			result = true;
			for (int i = start, imax = end; i < imax && primes[i] < n && result; i++) {
				for (int j = i, jmax = end, prime = primes[i]; j < jmax && primes[j] < n && result; j++) {
					result = secret.transform(prime + primes[j]) == secret.transform(prime)
							+ secret.transform(primes[j]);
				}

				if (i > 0 && i % 100 == 0) {
					if (LogLevel.Verbose == log) {
						logLoopStatus(startMillis, i);
					} else if (i == 1000) {
						logLoopStatus(startMillis, i);
					}
				}
			}
		}

		return result;
	}

	/**
	 * @param s
	 */
	private void log(String s) {
		if (LogLevel.None != log) {
			System.out.println(DATE_TIME_INSTANCE.format(new Date()) + ": " + s);
		}
	}

	/**
	 * @param startMillis
	 * @param count
	 */
	private void logLoopStatus(long startMillis, int count) {
		long millis = System.currentTimeMillis() - startMillis;
		if (millis > 0) {
			int i = count / 1000;
			log((i < 1 ? count : i + "K") + " primes checked at a rate of "
					+ (i < 1 ? (count * 60 * 1000 / millis) : (i * 60 * 1000 / millis) + "K")
					+ " primes per minute.");
		}
	}
}