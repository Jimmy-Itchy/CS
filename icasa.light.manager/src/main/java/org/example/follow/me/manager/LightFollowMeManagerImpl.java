package org.example.follow.me.manager;

import java.util.Map;

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
import org.example.follow.me.api.TemperatureConfiguration;

/**
 * Created by aygalinc on 28/10/16.
 */
@Component
@Instantiate
@Provides(specifications = FollowMeAdministration.class)
public class LightFollowMeManagerImpl implements FollowMeAdministration {

	/** Field for followMeConfiguration dependency */
	@Requires(id = "conf", optional = true) // pas besoin de preciser il est
											// solo
	private FollowMeConfiguration[] followMeConfiguration;

	/** Bind Method for followMeConfiguration dependency */
	@Bind(id = "conf")
	public void bindFollowMeConfiguration(FollowMeConfiguration followMeConfiguration, Map properties) {
	}

	/** Unbind Method for followMeConfiguration dependency */
	@Unbind(id = "conf")
	public void unbindFollowMeConfiguration(FollowMeConfiguration followMeConfiguration, Map properties) {
		// TODO: Add your implementation code here
	}
	
	/** Field for followMeConfiguration dependency */
	@Requires(id = "confT", optional = true) // pas besoin de preciser il est
											// solo
	private TemperatureConfiguration[] temperatureConfigurations;

	/** Bind Method for followMeConfiguration dependency */
	@Bind(id = "confT")
	public void bindTemperatureConfiguration(TemperatureConfiguration temperatureConfiguration, Map properties) {
	}

	/** Unbind Method for followMeConfiguration dependency */
	@Unbind(id = "confT")
	public void unbindTemperatureConfiguration(TemperatureConfiguration temperatureConfiguration, Map properties) {
		// TODO: Add your implementation code here
	}

	/** Component Lifecycle Method */
	public void stop() {
		// TODO: Add your implementation code here
	}

	/** Component Lifecycle Method */
	public void start() {
		// TODO: Add your implementation code here
		System.out.println("start manager....\n");
	}

	@Override
	public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
		followMeConfiguration[0].setMaximumNumberOfLightsToTurnOn(illuminanceGoal.getNumberOfLightsToTurnOn());
	}

	@Override
	public IlluminanceGoal getIlluminancePreference() {
		switch (followMeConfiguration[0].getMaximumNumberOfLightsToTurnOn()) {
		case 1:
			return IlluminanceGoal.SOFT;
		case 2:
			return IlluminanceGoal.MEDIUM;
		case 3:
			return IlluminanceGoal.FULL;
		default:
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
	public void temperatureIsTooHigh(String roomName) {
		float currentTemp = temperatureConfigurations[0].getTargetedTemperature(roomName);
		temperatureConfigurations[0].setTargetedTemperature(roomName, currentTemp - 5);
		while (temperatureConfigurations[0].getTargetedTemperature(roomName) >= currentTemp - 5) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void temperatureIsTooLow(String roomName) {
		System.out.println("\n\n\n changement de température:"+ roomName+"\n\n\n");
		float currentTemp = temperatureConfigurations[0].getTargetedTemperature(roomName);
		temperatureConfigurations[0].setTargetedTemperature(roomName, currentTemp + 5);
		while (temperatureConfigurations[0].getTargetedTemperature(roomName) <= currentTemp + 5) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
