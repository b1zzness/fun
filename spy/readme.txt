Download the contents of the github repository https://github.com/b1zzness/fun.git into a local directory of your choosing.  Using the command line go to that directory.  Run 'Spy' from the command line without arguments to see its usage, or look at the String constant Spy.USAGE in the source.  You can implement the interface spy.Secret to provide Spy with a Secret to test.  If you want to include a custom PrimeSource implement the interface spy.PrimeSource.


Example:


[a] Running from the package directory,

>java spy.Spy 100 spy.SecretDouble -debug



[b] running using multiple threads,

>java spy.Spy 1000000000 spy.SecretDouble -debug -threads 10



[c] the output of

>java spy.Spy

usage: spy N <secretclassname> [option]...
Spy determines if Secret is an additive function such that secret(x+y) = secret(x) + secret(y)
for all combinations of prime numbers less than N.


Required arguments ---------------------------------------------------------------------

N                         the value of N used in the analysis
<secretclassname>         the class to be examined. Must implement the interface
                          spy.Secret


Options --------------------------------------------------------------------------------

-chunksize <integer>      the number of primes each thread will check (default =
                          10000). Only used when the number of threads is > 1
-debug                    run a set of known Secrets as a self-diagnostic
-help                     print this message
-primesource <classname>  the class which provides the prime numbers used in the
                          analysis. Must implement the interface spy.PrimeSource
-silent                   disable logging
-threads <integer>        the maximum number of threads to run (default = 1)
-verbose                  maximum logging