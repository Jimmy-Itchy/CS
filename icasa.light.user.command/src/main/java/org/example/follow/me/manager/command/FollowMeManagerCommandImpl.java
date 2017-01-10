package org.example.follow.me.manager.command;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.example.follow.me.api.EnergyGoal;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.IlluminanceGoal;
import org.example.follow.me.api.ManagerException;

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

	private static Logger log = Logger.getLogger(FollowMeManagerCommandImpl.class.getName());
	
	private static final String ADMIN="admin";
	private static final String SOFT="SOFT";
	private static final String MEDIUM="MEDIUM";
	private static final String HIGH="HIGH";
	private static final String LOW="LOW";
	
	/** Field for followMeCommand dependency */
	// Declare a dependency to a FollowMeAdministration service
	@Requires(id=ADMIN, optional=true)
	private FollowMeAdministration m_administrationService;

	/** Bind Method for followMeCommand dependency */
	@Bind(id=ADMIN)
	public void bindFollowMeCommand(FollowMeAdministration followMeAdministration, Map properties) {
		log.info("bind follow command");
	}

	/** Unbind Method for followMeCommand dependency */
	@Unbind(id=ADMIN)
	public void unbindFollowMeCommand(FollowMeAdministration followMeAdministration, Map properties) {
		log.info("unbind follow command");
	}

	/** Component Lifecycle Method */
	public void stop() {
		log.info("stop command\n");
	}

	/** Component Lifecycle Method */
	public void start() {
		log.info("start command\n");
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

		// goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
		if (SOFT.equals(goal)) {
			m_administrationService.setIlluminancePreference(IlluminanceGoal.SOFT);
		} else if (MEDIUM.equals(goal)) {
			m_administrationService.setIlluminancePreference(IlluminanceGoal.MEDIUM);
		} else if (HIGH.equals(goal)) {
			m_administrationService.setIlluminancePreference(IlluminanceGoal.FULL);
		} else {
			log.info("Incorrect command please retry\n");
		}

	}

	@Command
	public void getIlluminancePreference() {
		log.info("\nThe illuminance goal is :"+m_administrationService.getIlluminancePreference().toString()+"\n");
	}
	
	
	@Command
	public void setEnergyGoalPreference(String goal) {
		// The targeted goal
		// IlluminanceGoal illuminanceGoal;

		// goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
		if (LOW.equals(goal)) {
			m_administrationService.setEnergySavingGoal(EnergyGoal.LOW);
		} else if (MEDIUM.equals(goal)) {
			m_administrationService.setEnergySavingGoal(EnergyGoal.MEDIUM);
		} else if (HIGH.equals(goal)) {
			m_administrationService.setEnergySavingGoal(EnergyGoal.HIGH);
		} else {
			log.info("\nIncorrect command please retry\n");
		}

	}

	@Command
	public void getEnergyGoalPreference() {
		log.info("\nThe energy goal is :"+m_administrationService.getEnergyGoal().toString()+"\n");
	}
	
	 // Each command should start with a @Command annotation
    @Command
    public void tempTooHigh(String room) throws CommandException {
        try {
			m_administrationService.temperatureIsTooHigh(room);
		} catch (ManagerException e) {
			log.log(Level.WARNING,"\nexception manager\n",e);
			throw new CommandException("Exception Commande");
		}
    }
 
    @Command
    public void tempTooLow(String room) throws CommandException{
    	try {
			m_administrationService.temperatureIsTooLow(room);
		} catch (ManagerException e) {
			log.log(Level.WARNING,"\nexception manager\n",e);
			throw new CommandException("Exception Commande");
		}
    }
	

}