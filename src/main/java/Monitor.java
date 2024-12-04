import java.util.ArrayList;
import java.util.List;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	boolean isSomeoneSpeaking = false;
	static int  numberOfPhilosphers;

	// Technically only necessary if the user passes 1 as a commandline argument,
	// otherwise it's equal to the number of philosophers
	static int numberOfChopsticks;

	// Will be used to keep track of which chopsticks are free: 0 = free, 1 =
	// Used to prevent deadlocks
	static int[] chopstickArray;

	// Priority queue to solve starvation problem
	static List<Integer> priorityQueue = new ArrayList<Integer>();


	/**
	 * Constructor
	 */
	// We initialize the array of chopsticks here to all 0s, to indicate that they start all available
	public Monitor(int piNumberOfPhilosophers)
	{
		if (piNumberOfPhilosophers == 1){
			numberOfPhilosphers = 1;
			numberOfChopsticks = 2;
		} else {
			numberOfPhilosphers = piNumberOfPhilosophers;
			numberOfChopsticks = piNumberOfPhilosophers;
		}
		chopstickArray = new int[numberOfPhilosphers];

		for (int i = 0; i < numberOfChopsticks; i++){
			chopstickArray[i] = 0;
		}
		priorityQueue.clear();
    }

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID) {

		// We add the current Philosopher to the back of the queue
		priorityQueue.add(piTID);

		// We wait until both chopsticks next to a philosopher are free, and for that philosopher to be at the front of the queue
		while(chopstickArray[piTID%numberOfPhilosphers] == 1 && chopstickArray[(piTID-1)%numberOfPhilosphers] == 1 && priorityQueue.getFirst() != piTID) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

		// Once we're done waiting, we mark the chopsticks as taken in the array, and pop from the queue
		chopstickArray[piTID%numberOfPhilosphers] = 1;
		chopstickArray[(piTID-1)%numberOfPhilosphers] = 1;
		priorityQueue.removeFirst();
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		// After we're done eating, we make the chopsticks available again and notifyAll()
		chopstickArray[piTID%numberOfPhilosphers] = 0;
		chopstickArray[(piTID-1)%numberOfPhilosphers] = 0;
		notifyAll();
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk() throws InterruptedException {

		// Only one philosopher can speak at a time, so we wait() until no one is speaking before requesting to talk
		while(isSomeoneSpeaking){
			wait();
		}

		// When we're speaking, we  notifyAll()
		isSomeoneSpeaking = true;
		notifyAll();
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk() throws InterruptedException {

		// When we're done speaking, we notifyAll()
		isSomeoneSpeaking = false;
		notifyAll();
	}
}

// EOF
