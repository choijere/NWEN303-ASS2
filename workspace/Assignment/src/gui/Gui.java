package gui;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import datasets.DataSetLoader;
import model.Model;
import model.ModelInterface;
import model.ModelParallel;

@SuppressWarnings("serial")
public class Gui extends JFrame implements Runnable {
	private static int minTime = 20;// use a bigger or smaller number for faster/slower simulation top speed
	// it will attempt to do a step every 20 milliseconds (less if the machine is
	// too slow)

	public static final ScheduledThreadPoolExecutor schedulerRepaint = new ScheduledThreadPoolExecutor(1);
	public static final ScheduledThreadPoolExecutor schedulerSimulation = new ScheduledThreadPoolExecutor(1);
	
	private final static boolean parallelSwitch = true;
	
	ModelInterface m;

	Gui(ModelInterface m) {
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

		MainLoop(Model m) {
			this.m = m;
		}

		public void run() {
			try {
				while (true) {
					long ut = System.currentTimeMillis();
					m.step();
					ut = System.currentTimeMillis() - ut;// used time
					
					long sleepTime = minTime - ut;
					if (sleepTime > 1) {
						Thread.sleep(sleepTime);
					}
				} // if the step was short enough, it wait to make it at least minTime long.
			} catch (Throwable t) {// not a perfect solution, but
				t.printStackTrace();// makes sure you see the error and the program dies.
				System.exit(0);// the "right" solution is much more involved
			} // and would require storing and passing the exception between different threads
				// objects.
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

		MainLoop2(ModelParallel m) {
			this.m = m;
		}

		public void run() {
			try {
				while (true) {
					long ut = System.currentTimeMillis();
					m.step();
					ut = System.currentTimeMillis() - ut;// used time
					
					long sleepTime = minTime - ut;
					if (sleepTime > 1) {
						Thread.sleep(sleepTime);
					}
				} // if the step was short enough, it wait to make it at least minTime long.
			} catch (Throwable t) {// not a perfect solution, but
				t.printStackTrace();// makes sure you see the error and the program dies.
				System.exit(0);// the "right" solution is much more involved
			} // and would require storing and passing the exception between different threads
				// objects.
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
		//Sequential Configurations
		Model s=DataSetLoader.getRegularGrid(100, 800, 10);
//		Model s=DataSetLoader.getRandomRotatingGrid(0.02d,100, 800, 40);
//		Model s=DataSetLoader.getRandomRotatingGrid(0.02d,100, 800, 30);
//		Model s = DataSetLoader.getElaborate(200, 700, 2, 0.99);
//		Model s=DataSetLoader.getElaborate(200, 700, 2,0.99005);
//		Model s=DataSetLoader.getElaborate(200, 700, 2,0.99008);
//		Model s=DataSetLoader.getRandomSet(100, 800, 1000);
//		Model s=DataSetLoader.getRandomSet(100, 800, 100);
//		Model s=DataSetLoader.getRandomGrid(100, 800, 30);

		//Parallel Configurations
		ModelParallel p=DataSetLoader.getRegularGrid2(100, 800, 10);
		//ModelParallel p=DataSetLoader.getElaborate2(200, 700, 2, 0.99);

		if(parallelSwitch) {
			schedulerSimulation.schedule(new MainLoop2(p), 500, TimeUnit.MILLISECONDS);
			SwingUtilities.invokeLater(new Gui(p));
		} else {
			schedulerSimulation.schedule(new MainLoop(s), 500, TimeUnit.MILLISECONDS);
			SwingUtilities.invokeLater(new Gui(s));
		}
	}
}