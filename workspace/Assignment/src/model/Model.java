package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import gui.runTests.ModelHolder;

/**
 * The Model class manages the particles.
 * 
 * First, the logic such as the speed and direction.
 * Second, merging the colliding particles.
 * Third, drawing all the particles.
 * 
 * @author Student#300474835
 *
 */
public class Model implements ModelInterface {
	public static final double size = 900;// just for the canvas
	public static final double gravitationalConstant = 0.001;
	public static final double lightSpeed = 10;// the smaller, the larger is the chunk of universe we simulate
	public static final double timeFrame = 0.1;// the bigger, the shorter is the time of a step, the slower but more
												// precise the simulation
	public List<Particle> p = new ArrayList<Particle>(); //ALL particles in the simulation.
	public volatile List<DrawableParticle> pDraw = new ArrayList<DrawableParticle>();

	//For testing:
	private final ModelHolder modelHolder;
	public boolean completed = false;
	
	public Model() {
		this.modelHolder = null;
	}
	
	public Model(ModelHolder modelHolder) {
		this.modelHolder = modelHolder;
	}

	/**
	 * Update through time itself.
	 * Display the updated Graphical Representation.
	 * This method is called from the GUI.
	 */
	public void step() {
		//FIRST: Cycle through all particles and...
		for (Particle p : this.p) {
			//... Process any interactions it has with any other particles in this model.
			p.interact(this);
		}
		
		//SECOND: Cycle through all particles and update movement.
		for (Particle p : this.p) {
			p.move();
		}
		
		//THIRD: Merge any colliding particles while deleting dead ones.
		mergeParticles();
		
		//FOURTH: Draw everything
		updateGraphicalRepresentation();

		if(modelHolder != null) {
			modelHolder.logParticles(p);
			if(p.size() <= 2) {
				
				completed = true;
			}
		}
	}

	//===========================================================================================//
	/**
	 * The draw method for this model.
	 * Call this last in the step method.
	 */
	private void updateGraphicalRepresentation() {
		ArrayList<DrawableParticle> d = new ArrayList<DrawableParticle>();
		Color c = Color.ORANGE;
		for (Particle p : this.p) {
			d.add(new DrawableParticle((int) p.x, (int) p.y, (int) Math.sqrt(p.mass), c));
		}
		this.pDraw = d;// atomic update
	}

	//===========================================================================================//
	/**
	 * Merge all colliding particles whilst removing dead ones.
	 * Dead ones are particles that're about to be merged into another one.
	 * 
	 * CHANGE #1: method was public, changed to private to avoid possible confusion later.
	 */
	private void mergeParticles() {
		//Find and remove all the dead particles from the simulation.
		Stack<Particle> deadPs = new Stack<Particle>();
		for (Particle p : this.p) {
			if (!p.impacting.isEmpty()) {
				deadPs.add(p);
			}
			//;
		}
		this.p.removeAll(deadPs);
		
		//For all the dead particles, (whilst in limbo) merge their stats into the particles they collide with.
		while (!deadPs.isEmpty()) {
			Particle current = deadPs.pop();
			Set<Particle> ps = getSingleChunck(current);
			deadPs.removeAll(ps);
			this.p.add(merge(ps));
		}
	}

	/**
	 * Starting from a single particle, grab every touching particle,
	 * including all particles touching the particle, that's touching the current particle.
	 * @param current
	 * @return
	 */
	private Set<Particle> getSingleChunck(Particle current) {
		Set<Particle> impacting = new HashSet<Particle>();
		impacting.add(current);
		while (true) {
			//Get all the impacting particles
			Set<Particle> tmp = new HashSet<Particle>();
			for (Particle pi : impacting) {
				tmp.addAll(pi.impacting);
			}
			//If the while loop has finished adding new particles.
			boolean changed = impacting.addAll(tmp); 
			if (!changed) {
				break;
			}
		}
		// now impacting have all the chunk of collapsing particles
		return impacting;
	}

	/**
	 * Merges a set of particles into singular one.
	 * Achieves this by combining all their stats together and returns the complete result
	 * @param ps 	Set of all the particles to be merged.
	 * @return 	Particle
	 * 
	 * CHANGE #1: method was public, changed to private to avoid possible confusion later.
	 * CHANGE #2: changed method name, was causing confusion since it's the exact same as it's parent.
	 */
	private Particle merge(Set<Particle> ps) {
		double speedX = 0;
		double speedY = 0;
		double x = 0;
		double y = 0;
		double mass = 0;
		for (Particle p : ps) {
			mass += p.mass;
			x += p.x * p.mass;
			y += p.y * p.mass;
			speedX += p.speedX * p.mass;
			speedY += p.speedY * p.mass;
		}
		x /= mass;
		y /= mass;
		speedX /= mass;
		speedY /= mass;
		return new Particle(mass, speedX, speedY, x, y);
	}

	//===========================================================================================//
	
	@Override
	public List<Particle> getParticles() {
		return p;
	}
	
	@Override
	public List<DrawableParticle> getPDraw() {
		return pDraw;
	}

	@Override
	public ModelHolder getModelHolder() {
		return modelHolder;
	}

}
