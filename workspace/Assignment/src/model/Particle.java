package model;

import java.util.HashSet;
import java.util.Set;

/**
 * Blueprint to build "Particle" objects
 * 
 * In addition to the variables set in the constructor,
 * Each particle remembers it's vector and speed. The move() function moves it accordingly.
 * Each particle also keeps track of any other particle overlapping it.
 * 
 * @author Student#300474835
 *
 */
public class Particle {
	public Particle(double mass, double speedX, double speedY, double x, double y) {
		this.mass = mass;
		this.speedX = speedX;
		this.speedY = speedY;
		this.x = x;
		this.y = y;
	}

	public Set<Particle> impacting = new HashSet<Particle>();
	public double mass;
	public double speedX;
	public double speedY;
	public double x;
	public double y;

	/**
	 * Move the particle according to it's set velocity from "interact(Model)".
	 */
	public void move() {
		x += speedX / (Model.timeFrame);
		y += speedY / (Model.timeFrame);
		// uncomment the following to have particle bouncing on the boundary
		// if(this.x<0){this.speedX*=-1;}
		// if(this.y<0){this.speedY*=-1;}
		// if(this.x>Model.size){this.speedX*=-1;}
		// if(this.y>Model.size){this.speedY*=-1;}
	}

	/**
	 * Process all interactions this particle will have with other particles in the model's universe.
	 * Determines and sets what velocity this particle will move in.
	 * @param m The model that contains the particles this particle will interact with.
	 */
	public void interact(ModelInterface m) {
		//check and process if this particle is interacting with any other particle in the simulation.
		for (Particle p : m.getParticles()) {
			//skip, if "this particle" is the particle that got drawn up from the simulation.
			if (p == this) {
				continue;
			}
			double dirX = -Math.signum(this.x - p.x);
			double dirY = -Math.signum(this.y - p.y);
			double dist = distance2(p);// this is already distance^2
			if (isImpact(dist, p.mass)) {
				this.impacting.add(p);
				continue;
			}
			dirX = p.mass * Model.gravitationalConstant * dirX / (dist * Model.timeFrame);
			dirY = p.mass * Model.gravitationalConstant * dirY / (dist * Model.timeFrame);
			assert this.speedX <= Model.lightSpeed : this.speedX;
			assert this.speedY <= Model.lightSpeed : this.speedY;
			double newSpeedX = this.speedX + dirX;
			newSpeedX /= (1 + (this.speedX * dirX) / Model.lightSpeed);
			double newSpeedY = this.speedY + dirY;
			newSpeedY /= (1 + (this.speedY * dirY) / Model.lightSpeed);
			if (!Double.isNaN(dirX)) {
				this.speedX = newSpeedX;
			}
			if (!Double.isNaN(dirY)) {
				this.speedY = newSpeedY;
			}
		}
	}

	//===========================================================================================//
	
	/**
	 * Check if this particle is overlapped with a target "other" particle.
	 * @param dist			The distance between the centre's of the two particles.
	 * @param otherMass 	The mass of the other particle.
	 * @return 				True if they're touching.
	 * 
	 * CHANGE: method was public, changed to private to avoid possible confusion later.
	 */
	private boolean isImpact(double dist, double otherMass) {
		if (Double.isNaN(dist)) {
			return true;
		}
		double distMass = Math.sqrt(mass) + Math.sqrt(otherMass);
		if (dist < distMass * distMass) {
			return true;
		}
		return false;
	}

	/**
	 * Check if this particle is overlapped with a set of other particles.
	 * @param ps 
	 * @return
	 * 
	 * CHANGE: method was public, changed to private to avoid possible confusion later.
	 */
	private boolean isImpact(Iterable<Particle> ps) {
		for (Particle p : ps) {
			if (this == p) {
				continue;
			}
			double dist = distance2(p);
			if (isImpact(dist, p.mass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the euclidean distance between this and the "target particle"
	 * @param p the target particle.
	 * @return the euclidean distance
	 * CHANGE: method was public, changed to private to avoid possible confusion later.
	 */
	private double distance2(Particle p) {
		double distX = this.x - p.x;
		double distY = this.y - p.y;
		return distX * distX + distY * distY;
	}
	
	/**
	 * Check if this particle is the exact same one as the other.
	 * 
	 * @param p The other particle.
	 * @return true if they're equal, false otherwise.
	 */
	public boolean equal(Particle p) {
		if(p.mass != mass) {
			return false;
		}
		//Compare positions with room for error
		else if(p.x-1 > x && p.x+1 < x) {
			return false;
		}
		else if(p.y-1 > y && p.y+1 < y) {
			return false;
		}
		return true;
	}
}