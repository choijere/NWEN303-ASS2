package gui.runTests;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import datasets.DataSetLoaderWithHolders;
import gui.Canvas;
import model.Model;
import model.ModelInterface;
import model.ModelParallel;
import model.Particle;

/**
 * Run's everything, including all the models, whilst also testing correctness and time.
 * Prints a detailed report upon completion.
 * 
 * @author Student#300474835
 *
 */

@SuppressWarnings("serial")
public class RunTests extends JFrame implements Runnable {
	private static int minTime = 20;// use a bigger or smaller number for faster/slower simulation top speed
	// it will attempt to do a step every 20 milliseconds (less if the machine is
	// too slow)

	public static ScheduledThreadPoolExecutor schedulerRepaint = new ScheduledThreadPoolExecutor(1);
	public static ScheduledThreadPoolExecutor schedulerSimulation = new ScheduledThreadPoolExecutor(1);
	
	private final static boolean parallelSwitch = true;
	
	ModelInterface m;

	RunTests(ModelInterface m) {
		this.m = m;
	}

	/**
	 * This method handles the graphical representation of the simulation.
	 * It contains the schedulerRepaint instructions.
	 */
	public void run() {
		//GUI stuff
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		getRootPane().setLayout(new BorderLayout());
		JPanel p = new Canvas(m);
		p.setDoubleBuffered(true);
		getRootPane().add(p, BorderLayout.CENTER);
		pack();
		setVisible(true);
		//Sends "Execute the following" orders to a worker repeatedly at a consistent rate.
		schedulerRepaint.scheduleAtFixedRate(() -> {
			if (!schedulerRepaint.getQueue().isEmpty()) {
				System.out.println("Skipping a frame");
				setVisible(false);
				schedulerRepaint.shutdownNow();
				return;
			} // still repainting

			try {
				//The actual "draw" method.
				SwingUtilities.invokeAndWait(() -> repaint());
			} // You may want to explain why this is needed
			catch (InvocationTargetException | InterruptedException e) {// not a perfect solution, but
				e.printStackTrace();// makes sure you see the error and the program dies.
				System.exit(0);// the "right" solution is much more involved
			} // and would require storing and passing the exception between different threads
				// objects.
		}, 500, 5, TimeUnit.MILLISECONDS);
	}

	/**
	 * This is called from the main() at the bottom of this java file.
	 * It's passed the Model upon construction.
	 * It's run() function invokes the logic built within the Model (by calling "step()").
	 *
	 * There is no thread pool or scheduler executor within this class. It is called from the outside, Possible bug!
	 *
	 * @author Student#300474835
	 *
	 */
	private static final class MainLoop implements Runnable {
		Model m;
		private volatile boolean exit = false;

		MainLoop(Model m) {
			this.m = m;
		}

		public void run() {
			try {
				while (!exit) {
					long ut = System.currentTimeMillis();
					m.step();
					ut = System.currentTimeMillis() - ut;// used time
					// System.out.println("Particles: "+m.p.size()+" time:"+ut);//if you want to
					// have an idea of the time consumption
					long sleepTime = minTime - ut;
					if (sleepTime > 1) {
						Thread.sleep(sleepTime);
					}
					if(m.completed) {
						break;
					}
				} // if the step was short enough, it wait to make it at least minTime long.
				Thread.currentThread().interrupt();
			} catch (Throwable t) {// not a perfect solution, but
				t.printStackTrace();// makes sure you see the error and the program dies.
				System.exit(0);// the "right" solution is much more involved
			} // and would require storing and passing the exception between different threads
				// objects.
		}
		
		public void stop() {
			exit = true;
		}
	}

	/**
	 * "If MainLoop is so good, why isn't there a MainLoop 2???"
	 * This version uses ModelParallel.
	 *
	 * @author Student#300474835
	 *
	 */
	private static final class MainLoop2 implements Runnable {
		ModelParallel m;
		private volatile boolean exit = false;
		
		MainLoop2(ModelParallel m) {
			this.m = m;
		}

		public void run() {
			try {
				while (!exit) {
					long ut = System.currentTimeMillis();
					m.step();
					ut = System.currentTimeMillis() - ut;// used time
					// System.out.println("Particles: "+m.p.size()+" time:"+ut);//if you want to
					// have an idea of the time consumption
					long sleepTime = minTime - ut;
					if (sleepTime > 1) {
						Thread.sleep(sleepTime);
					}
					if(m.completed) {
						break;
					}
				} // if the step was short enough, it wait to make it at least minTime long.
				Thread.currentThread().interrupt();
			} catch (Throwable t) {// not a perfect solution, but
				t.printStackTrace();// makes sure you see the error and the program dies.
				System.exit(0);// the "right" solution is much more involved
			} // and would require storing and passing the exception between different threads
				// objects.
		}
		
		public void stop() {
			exit = true;
		}
	}

	/**
	 * Here is where the SchedulerSimulation is.
	 * Every 500 miliseconds, it creates a new MainLoop with it's own running loop, with it's own passed down model.
	 * Sounds a little inefficient, probably a bug.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		//Parallel Configuration
		ModelParallel p = DataSetLoaderWithHolders.getRegularGrid2(200, 700, 10);
		MainLoop2 parallelLoop = new MainLoop2(p);
		
		//Sequential Configuration
		Model s = DataSetLoaderWithHolders.getRegularGrid(200, 700, 10);
		MainLoop sequentialLoop = new MainLoop(s);
		
		//FIRST: Run the Sequential configuration
		long t0 = System.currentTimeMillis();
		schedulerSimulation.schedule(sequentialLoop, 500, TimeUnit.MILLISECONDS);
		SwingUtilities.invokeLater(new RunTests(s));
		
		//WAIT BLOCK:
		while(!s.completed) {
			//wait
			System.out.println("SEQUENTIAL IN PROGRESS");
		}
		sequentialLoop.stop();
		
		System.out.println("Sequential test completed.");
		long t1 = System.currentTimeMillis();
		
		final long sequentialTime = t1-t0;

		//------------------------------------------//
		schedulerRepaint = new ScheduledThreadPoolExecutor(1);
		schedulerSimulation = new ScheduledThreadPoolExecutor(1);
		//------------------------------------------//
		
		//SECOND: Run Parallel configuration.
		t0 = System.currentTimeMillis();
		schedulerSimulation.schedule(parallelLoop, 500, TimeUnit.MILLISECONDS);
		SwingUtilities.invokeLater(new RunTests(p));
		
		//WAIT BLOCK:
		while(!p.completed) {
			//wait
			System.out.println("PARALLEL IN PROGRESS");
		}
		parallelLoop.stop();
		
		schedulerSimulation.shutdown();
		System.out.println("Parallel test completed.");
		t1 = System.currentTimeMillis();
		
		final long parallelTime = t1-t0;
		
		//====================================================//
		//THIRD: Compare correctness
		System.out.println("Parallel Time Elapsed: " + parallelTime + "ms |Sequential Time Elapsed: " + sequentialTime + "ms");
		System.out.println("Parallel avg: " + parallelTime/p.getModelHolder().numberOfSteps + "ms |Sequential avg: " + sequentialTime/s.getModelHolder().numberOfSteps + "ms");
		
		System.out.println("Parallel steps: " + p.getModelHolder().numberOfSteps + "ms |Sequential steps: " + s.getModelHolder().numberOfSteps + "ms");

		ArrayList<List<Particle>> sLog = s.getModelHolder().logOfParticles;
		ArrayList<List<Particle>> pLog = p.getModelHolder().logOfParticles;
		boolean correct = true;
		for(int i = 0; i < s.getModelHolder().numberOfSteps-2; i++) {
			List<Particle> sList = sLog.get(i);
			List<Particle> pList = pLog.get(i);
			for(int j = 0; j < sList.size(); j++) {
				if(!sList.get(j).equal(pList.get(j))) {
					//Incorrect!
					System.out.println("INCORRECT PARTICLE!");
					System.out.println("step #" + i);
					System.out.println("Mass: " + sList.get(j).mass + "| " + pList.get(j).mass);
					System.out.println("X: " + sList.get(j).x + "| " + pList.get(j).x);
					System.out.println("Y: " + sList.get(j).x + "| " + pList.get(j).y);
					i = s.getModelHolder().numberOfSteps;
					correct = false;
					break;
				}
			}
		}
		if(correct) {
			System.out.println("All particles correct!");
		}
	}
}