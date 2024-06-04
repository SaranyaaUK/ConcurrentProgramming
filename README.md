# ConcurrentProgramming

## AIM

+ The aim of the project/coursework is to make some calculation run parallel to make the computation faster to understand the power of the concurrent programming using JAVA.
+ There is also some set of commands and the expected behaviour given to enforce an understanding of multi-threading and how interrupts work in multi-threading scenarios. 

(NOTE: The below commands are from coursework assignment sheet)
`start N` -  start calculating with input N, by calling
SlowCalculator.run on a new thread; immediately
return the message “started N”.

`cancel N` - immediately cancel the calculation with input N
that is currently running (do nothing if it already
completed or if it was never started); when it has
stopped (which should be within 0.1s) return message
“cancelled N” 

`running` -  return a message indicating the total number
of calculations currently running (i.e. excluding
those already completed/cancelled), and
their inputs N (in any order), in the form
“3 calculations running: 83476 1000 176544”.
If no calculations are running, return the string “no
calculations running”.

`get N` - if the calculation for N is finished, return message
“result is M ” where M is the integer result; if
the calculation is not yet finished, return message
“calculating”. If the calculation was started but
already cancelled, return message “cancelled”

`after N M` -  assuming the calculation for N is already running,
schedule the calculation for M to start when that for
N finishes (or is cancelled). Return the message “M
will start after N ” immediately (without waiting
for either calculation). The calculation for M
should not appear in running until it is actually running
(i.e. N has completed)

`finish` -  wait for all calculations previously requested by the
user (including those scheduled with after) to finish,
and then after they are all completed, return
message “finished”

`abort` - immediately stop all running calculations (and discard
any scheduled using after), and then when
they are stopped (which should be within 0.1s) return
message “aborted”

## Resource

A file named `SlowCalculator.java` was provided, this class actually has a constructor that takes in a value `N` and stores it. Also another function that very inefficiently calculates the unique prime factors of a given number. 

A inteface `CommandRunner.java` is provided that runs certain commands passed as strings. The user inputs the numbers as strings and outputs a string.
