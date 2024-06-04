import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Solution.java
 * 
 */

// ENUM for the Commands
enum Commands {
    START("start"),
    CANCEL("cancel"),
    GET("get"),
    RUNNING("running"),
    AFTER("after"),
    FINISH("finish"),
    ABORT("abort");

    public final String label;

    Commands(String label) {
        this.label = label;
    }

}

public class Solution implements CommandRunner {

    private static final Map<String, Thread> startedThreadsMap = new ConcurrentHashMap<>();
    private static final Map<String, SlowCalculator> runnablesMap = new ConcurrentHashMap<>();
    private static final Map<String, Thread> scheduledThreadsMap = new ConcurrentHashMap<>();
    private volatile String toRunCommand;
    private volatile long N;
    private volatile long M;

    /*
     * Constructor
     */
    public Solution() {
        this.N = 0;
        this.M = 0;
        this.toRunCommand = "";
    }

    @Override
    public String runCommand(String command) {
        // Parse User command
        this.parseUserCommand(command);

        // Execute the commands
        switch (this.toRunCommand) {
            case "start":
                return this.executeStartAction();
            case "cancel":
                return this.executeCancelAction();
            case "running":
                return this.executeRunAction();
            case "get":
                return this.executeGetAction();
            case "after":
                return this.executeAfterAction();
            case "finish":
                return this.executeFinishAction();
            case "abort":
                return this.executeAbortAction();
            default:
                return "Invalid Command";
        }
    }

    // Helper methods

    /**
     * Parses user command and sets the toRunCommand, N, and M.
     * 
     * @param command
     */
    private void parseUserCommand(String command) {
        try (Scanner scanner = new Scanner(command)) {
            String givenCommand = scanner.next();
            switch (givenCommand) {
                case "start":
                    this.toRunCommand = Commands.START.label;
                    if (scanner.hasNextLong()) {
                        this.N = scanner.nextLong();
                    } else {
                        this.toRunCommand = "";
                    }
                    break;
                case "cancel":
                    this.toRunCommand = Commands.CANCEL.label;
                    if (scanner.hasNextLong()) {
                        this.N = scanner.nextLong();
                    } else {
                        this.toRunCommand = "";
                    }
                    break;
                case "get":
                    this.toRunCommand = Commands.GET.label;
                    if (scanner.hasNextLong()) {
                        this.N = scanner.nextLong();
                    } else {
                        this.toRunCommand = "";
                    }
                    break;
                case "running":
                    this.toRunCommand = Commands.RUNNING.label;
                    break;
                case "after":
                    this.toRunCommand = Commands.AFTER.label;
                    if (scanner.hasNextLong()) {
                        this.N = scanner.nextLong();
                    } else {
                        this.toRunCommand = "";
                    }
                    if (scanner.hasNextLong()) {
                        this.M = scanner.nextLong();
                    } else {
                        this.toRunCommand = "";
                    }
                    break;
                case "finish":
                    this.toRunCommand = Commands.FINISH.label;
                    break;
                case "abort":
                    this.toRunCommand = Commands.ABORT.label;
                    break;
                default:
                    this.toRunCommand = "";
            }
        } catch (Exception e) {
            this.toRunCommand = "";
            e.printStackTrace();
        }
    }

    /**
     * executeStartAction()
     * 
     * Actions to perform when "start" command is given
     * 
     * @return String
     */
    private String executeStartAction() {
        startCalculation(this.N);
        return String.format("%s %d", "started", this.N);
    }

    /**
     * executeCancelAction()
     * 
     * Actions to perform when "cancel" command is given
     * 
     * @return String
     */
    private String executeCancelAction() {
        Thread toCancelThread = getThread(Long.toString(this.N));
        if (toCancelThread != null && toCancelThread.getState() == Thread.State.RUNNABLE) {
            // Interrupt the thread to cancel the operation
            toCancelThread.interrupt();
            try {
                // Wait for the interrupted thread to be stopped within 0.1s
                toCancelThread.join(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // If the running thread is interrupted after waiting for 0.1 s for
            // it to stop, return the String cancelled
            return String.format("%s %d", "cancelled", this.N);
        } else {
            // If the thread is not interrupted, which could be either if the thread
            // terminated already is not yet started - do nothing
            return String.format("%s %d", "cancelled", this.N);
        }
    }

    /**
     * executeRunAction()
     * 
     * Actions to perform when "run" command is given
     * 
     * @return String
     */
    private String executeRunAction() {
        // Local list to hold thread running calculations
        List<Thread> runningThreads = new CopyOnWriteArrayList<>();
        for (Thread thread : startedThreadsMap.values()) {
            // If the thread is running, add the thread to the list
            if (thread.getState() == Thread.State.RUNNABLE) {
                runningThreads.add(thread);
            }
        }
        if (runningThreads.isEmpty()) {
            return "no calculations running.";
        } else {
            String toReturnString = String.format("%d %s", runningThreads.size(),
                    "calculations running:");
            for (Thread thread : runningThreads) {
                toReturnString += " " + thread.getName();
            }
            return toReturnString;
        }
    }

    /**
     * executeGetAction()
     * 
     * Actions to perform when "get" command is given
     * 
     * @return String
     */
    private String executeGetAction() {
        String toReturnString = "";
        Thread resultThread = getThread(Long.toString(N));
        if ((resultThread != null && resultThread.isInterrupted()) || (resultThread == null)) {
            // If the thread was cancelled or was aborted
            toReturnString = "cancelled";
        } else if (resultThread != null && resultThread.getState() == Thread.State.TERMINATED) {
            SlowCalculator myRunnable = getRunnable(resultThread.getName());
            toReturnString = String.format("%s %d", "result is", myRunnable.getCalculationResult());
        } else if (resultThread != null && resultThread.getState() == Thread.State.RUNNABLE) {
            toReturnString = "calculating";
        }
        return toReturnString;
    }

    /**
     * executeAfterAction()
     * 
     * Actions to perform when "after" command is given
     * 
     * @return String
     */
    private String executeAfterAction() {
        long currentM = this.M;
        Thread threadN = getThread(Long.toString(N));
        if (threadN != null && threadN.getState() == Thread.State.RUNNABLE) {
            // If calculation for N is currently running
            // Create a new thread to wait for thread N to finish
            Thread waitForNThread = new Thread(() -> {
                try {
                    threadN.join(); // Wait for N to finish
                } catch (InterruptedException e) {
                    // If interrupted return
                    return;
                }
                if (threadN.isInterrupted()) {
                    return;
                }
                // Once N finishes, start the calculation for M
                startCalculation(currentM);
            });
            scheduledThreadsMap.put(Long.toString(currentM), waitForNThread);
            waitForNThread.start(); // Start the thread to wait for N to finish
            return String.format("%d %s %d", currentM, "will start after", N);
        } else {
            // Calculation for N has already completed or was cancelled or N is something
            // that is not present
            startCalculation(currentM);
            return String.format("%d %s %d", currentM, "will start after", N);
        }
    }

    /**
     * executeFinishAction()
     * 
     * Actions to perform when "finish" command is given
     * 
     * @return String
     */
    private String executeFinishAction() {
        // Wait for all active threads to finish
        for (Thread thread : startedThreadsMap.values()) {
            try {
                thread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace(); // Handle interruption if necessary
            }
        }

        // Wait for all scheduled threads to finish
        for (Thread thread : scheduledThreadsMap.values()) {
            try {
                thread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace(); // Handle interruption if necessary
            }
        }
        // After all calculations are completed, return "finished"
        return "finished";
    }

    /**
     * executeAbortAction()
     * 
     * Actions to perform when "abort" command is given
     * 
     * @return String
     */
    private String executeAbortAction() {
        // Interrupt and stop all running calculations
        for (Thread thread : startedThreadsMap.values()) {
            thread.interrupt();
        }

        // Wait for all running threads to stop within 0.1s
        for (Thread thread : startedThreadsMap.values()) {
            try {
                // Wait for the thread to stop within 0.1s
                thread.join(100);
            } catch (InterruptedException e) {
                e.printStackTrace(); // Handle interruption if necessary
            }
        }

        // Clear the startedThreadMap
        startedThreadsMap.clear();

        // Clear any scheduled threads, we need not wait for these
        // as they have not started yet
        for (Thread thread : scheduledThreadsMap.values()) {
            thread.interrupt();
        }
        // Clear the scheduledThreadsMap
        scheduledThreadsMap.clear();

        // After aborting all calculations, return
        return "aborted";
    }

    /**
     * startCalculation()
     * 
     * Start the calculation
     * 
     * @param val (long)
     */
    private void startCalculation(long val) {
        SlowCalculator mySlowCalculator = new SlowCalculator(val);
        Thread startCommandThread = new Thread(mySlowCalculator, Long.toString(val));
        startedThreadsMap.put(Long.toString(val), startCommandThread);
        runnablesMap.put(Long.toString(val), mySlowCalculator);
        startCommandThread.start();
    }

    /**
     * getThread()
     * 
     * Get the thread object given its name
     * 
     * @param name
     * @return Thread
     */
    private Thread getThread(String name) {
        return startedThreadsMap.get(name);
    }

    /**
     * getRunnable
     * 
     * Get the runnable object given the thread name
     * 
     * @param name
     * @return SlowCalculator
     */
    private SlowCalculator getRunnable(String name) {
        return runnablesMap.get(name);
    }

}
