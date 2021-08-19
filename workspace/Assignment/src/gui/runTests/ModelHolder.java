package gui.runTests;

import java.util.ArrayList;
import java.util.List;

import model.Particle;

/**
 * Mostly just holds measurements that are given to it by the model.
 * Used for retrieving data from, by RunTests.
 * 
 * @author Student#300474835
 *
 */
public class ModelHolder {
	public int numberOfSteps = 0;
	
	//Code correctness
	public ArrayList<List<Particle>> logOfParticles = new ArrayList<>();
	
	//========================================================//
	
	//Code correctness
	public void logParticles(List<Particle> p) {
		logOfParticles.add(p);
		numberOfSteps = logOfParticles.size();
	}
	
}
