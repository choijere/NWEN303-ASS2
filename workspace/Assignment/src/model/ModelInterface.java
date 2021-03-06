package model;

import java.util.ArrayList;
import java.util.List;

import gui.runTests.ModelHolder;

/**
 * The ONLY thing this does is allow ModelParallel to use the particle's "interact(m)" method.
 * 
 * The "interact(Model m)" method in the Particle.java is also modified to "interact(ModelInterface m)"
 * 
 * @author Student#300474835
 *
 */
public interface ModelInterface {
	public List<Particle> getParticles(); //ALL particles in the simulation.

	public List<DrawableParticle> getPDraw();
	
	public ModelHolder getModelHolder();
}
