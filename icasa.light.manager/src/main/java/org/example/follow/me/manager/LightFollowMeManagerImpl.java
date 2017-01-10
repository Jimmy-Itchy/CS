package org.example.follow.me.manager;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.example.follow.me.api.EnergyGoal;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.FollowMeConfiguration;
import org.example.follow.me.api.IlluminanceGoal;
import org.example.follow.me.api.ManagerException;
import org.example.follow.me.api.TemperatureConfiguration;

/**
 * Created by aygalinc on 28/10/16.
 */
@Component
@Instantiate
@Provides(specifications = FollowMeAdministration.class)
public class LightFollowMeManagerImpl implements FollowMeAdministration {
	
	private static Logger log = Logger.getLogger(LightFollowMeManagerImpl.class.getName());
	
	private static final String CONF="conf";
	private static final String CONFT="conft";

	/** Field for followMeConfiguration dependency */
	@Requires(id = CONF, optional = true) 
	private FollowMeConfiguration[] followMeConfiguration;

	/** Bind Method for followMeConfiguration dependency */
	@Bind(id = CONF)
	public void bindFollowMeConfiguration(FollowMeConfiguration followMeConfiguration, Map properties) {
	}

	/** Unbind Method for followMeConfiguration dependency */
	@Unbind(id = CONF)
	public void unbindFollowMeConfiguration(FollowMeConfiguration followMeConfiguration, Map properties) {
	}
	
	/** Field for followMeConfiguration dependency */
	@Requires(id = CONFT, optional = true)
	private TemperatureConfiguration[] temperatureConfigurations;

	/** Bind Method for followMeConfiguration dependency */
	@Bind(id = CONFT)
	public void bindTemperatureConfiguration(TemperatureConfiguration temperatureConfiguration, Map properties) {
	}

	/** Unbind Method for followMeConfiguration dependency */
	@Unbind(id = CONFT)
	public void unbindTemperatureConfiguration(TemperatureConfiguration temperatureConfiguration, Map properties) {
	}

	/** Component Lifecycle Method */
	public void stop() {
		log.info("stop manager....\n");
	}

	/** Component Lifecycle Method */
	public void start() {
		log.info("start manager....\n");
	}

	@Override
	public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
		followMeConfiguration[0].setMaximumNumberOfLightsToTurnOn(illuminanceGoal.getNumberOfLightsToTurnOn());
	}

	@Override
	public IlluminanceGoal getIlluminancePreference() {
		if(followMeConfiguration[0].getMaximumNumberOfLightsToTurnOn()<3) {
			return IlluminanceGoal.SOFT;
		}else if(followMeConfiguration[0].getMaximumNumberOfLightsToTurnOn()<6) {
			return IlluminanceGoal.MEDIUM;
		}else if(followMeConfiguration[0].getMaximumNumberOfLightsToTurnOn()>=6) {
			return IlluminanceGoal.FULL;
		}else{
			log.info("\n Mauviase illuminance goal\n");
			return null;
		}
	}

	@Override
	public void setEnergySavingGoal(EnergyGoal energyGoal) {
		followMeConfiguration[0].setMaximumAllowedEnergyInRoom(energyGoal.getMaximumEnergyInRoom());
	}

	@Override
	public EnergyGoal getEnergyGoal() {
		switch ((int) followMeConfiguration[0].getMaximumAllowedEnergyInRoom()) {
		case 100:
			return EnergyGoal.LOW;
		case 200:
			return EnergyGoal.MEDIUM;
		case 1000:
			return EnergyGoal.HIGH;
		default:
			return null;
		}
	}

	@Override
	public synchronized void temperatureIsTooHigh(String roomName) throws ManagerException{
		float currentTemp = temperatureConfigurations[0].getTargetedTemperature(roomName);
		temperatureConfigurations[0].setTargetedTemperature(roomName, currentTemp - 5);
		while (temperatureConfigurations[0].getTargetedTemperature(roomName) >= currentTemp - 5) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				log.log(Level.WARNING,"\nexception wait\n",e);
				throw new ManagerException("Exception");
			}
		}
	}

	@Override
	public synchronized void temperatureIsTooLow(String roomName) throws ManagerException{
		float currentTemp = temperatureConfigurations[0].getTargetedTemperature(roomName);
		temperatureConfigurations[0].setTargetedTemperature(roomName, currentTemp + 5);
		while (temperatureConfigurations[0].getTargetedTemperature(roomName) <= currentTemp + 5) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				log.log(Level.WARNING,"\nexception wait\n",e);
				throw new ManagerException("Exception");
			}
		}

	}
}
