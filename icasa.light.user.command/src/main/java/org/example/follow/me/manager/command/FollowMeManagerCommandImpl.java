package org.example.follow.me.manager.command;

import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.example.follow.me.api.EnergyGoal;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.IlluminanceGoal;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;

//Define this class as an implementation of a component :
@Component
// Create an instance of the component
@Instantiate
// Use the handler command and declare the command as a command provider. The
// namespace is used to prevent name collision.
@CommandProvider(namespace = "followme")
public class FollowMeManagerCommandImpl {

	/** Field for followMeCommand dependency */
	// Declare a dependency to a FollowMeAdministration service
	@Requires(id="admin", optional=true)
	private FollowMeAdministration m_administrationService;

	/** Bind Method for followMeCommand dependency */
	@Bind(id="admin")
	public void bindFollowMeCommand(FollowMeAdministration followMeAdministration, Map properties) {
		// TODO: Add your implementation code here
	}

	/** Unbind Method for followMeCommand dependency */
	@Unbind(id="admin")
	public void unbindFollowMeCommand(FollowMeAdministration followMeAdministration, Map properties) {
		// TODO: Add your implementation code here
	}

	/** Component Lifecycle Method */
	public void stop() {
		// TODO: Add your implementation code here
	}

	/** Component Lifecycle Method */
	public void start() {
		// TODO: Add your implementation code here
		System.out.println("start command\n");
	}

	/**
	 * Felix shell command implementation to sets the illuminance preference.
	 *
	 * @param goal
	 *            the new illuminance preference ("SOFT", "MEDIUM", "FULL")
	 */

	// Each command should start with a @Command annotation
	@Command
	public void setIlluminancePreference(String goal) {
		// The targeted goal
		// IlluminanceGoal illuminanceGoal;

		// TODO : Here you have to convert the goal string into an illuminance
		// goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
		String soft = new String("SOFT");
		if (new String("SOFT").equals(goal)) {
			m_administrationService.setIlluminancePreference(IlluminanceGoal.SOFT);
		} else if (new String("MEDIUM").equals(goal)) {
			m_administrationService.setIlluminancePreference(IlluminanceGoal.MEDIUM);
		} else if (new String("HIGH").equals(goal)) {
			m_administrationService.setIlluminancePreference(IlluminanceGoal.FULL);
		} else {
			System.out.println("Incorrect command please retry\n");
		}

	}

	@Command
	public void getIlluminancePreference() {
		// TODO : implement the command that print the current value of the goal
		System.out.println("The illuminance goal is " + m_administrationService.getIlluminancePreference().toString());
	}
	
	
	@Command
	public void setEnergyGoalPreference(String goal) {
		// The targeted goal
		// IlluminanceGoal illuminanceGoal;

		// TODO : Here you have to convert the goal string into an illuminance
		// goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
		String soft = new String("LOW");
		if (new String("LOW").equals(goal)) {
			m_administrationService.setEnergySavingGoal(EnergyGoal.LOW);
		} else if (new String("MEDIUM").equals(goal)) {
			m_administrationService.setEnergySavingGoal(EnergyGoal.MEDIUM);
		} else if (new String("HIGH").equals(goal)) {
			m_administrationService.setEnergySavingGoal(EnergyGoal.HIGH);
		} else {
			System.out.println("Incorrect command please retry\n");
		}

	}

	@Command
	public void getEnergyGoalPreference() {
		// TODO : implement the command that print the current value of the goal
		System.out.println("The energy goal is " + m_administrationService.getEnergyGoal().toString());
	}
	
	 // Each command should start with a @Command annotation
    @Command
    public void tempTooHigh(String room) {
        m_administrationService.temperatureIsTooHigh(room);
    }
 
    @Command
    public void tempTooLow(String room){
    	m_administrationService.temperatureIsTooLow(room);
    }
	

}