package org.example.follow.me.regulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.example.follow.me.api.FollowMeConfiguration;
import org.example.follow.me.api.RoomEnum;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;

/**
 * Created by aygalinc on 28/10/16.
 */
@Component
@Instantiate
@Provides(specifications = FollowMeConfiguration.class)
public class LightFollowMeRegulatorImpl implements DeviceListener, FollowMeConfiguration {
	
	
	private static Logger log = Logger.getLogger(LightFollowMeRegulatorImpl.class.getName());
	
	private static final String BLIGHT="b-light";
	private static final String DLIGHT="d-light";
	private static final String SENSOR="sensor";
	/**
	 * The name of the LOCATION property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * BinaryLight The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";

	/**
	 * The maximum number of lights to turn on when a user enters the room :
	 **/
	private int maxLightsToTurnOnPerRoom = 2;

	/**
	 * The maximum energy consumption allowed in a room in Watt:
	 **/
	private double maximumEnergyConsumptionAllowedInARoom = 100.0d;

	/**
	 * The minimal energy consumption allowed in a room in Watt:
	 **/
	private double defaultBinaryLightEnergyConsumption = 100.0d;

	/** Field for presenceSensors dependency */
	@Requires(id = SENSOR, optional = true)
	private PresenceSensor[] presenceSensors;

	/** Field for binaryLights dependency */
	@Requires(id = BLIGHT, optional = true)
	private BinaryLight[] binaryLights;

	@Requires(id = DLIGHT, optional = true)
	private DimmerLight[] dimmerLights;

	/** Bind Method for binaryLights dependency */
	@Bind(id = BLIGHT)
	public void bindBinaryLight(BinaryLight binaryLight, Map properties) {
		log.info("bind binary light "+ binaryLight.getSerialNumber());
		binaryLight.addListener(this);
	}

	/** Unbind Method for binaryLights dependency */
	@Unbind(id = BLIGHT)
	public void unbindBinaryLight(BinaryLight binaryLight, Map properties) {
		log.info("unbind dimmer light " + binaryLight.getSerialNumber());
		binaryLight.removeListener(this);
	}

	/** Unbind Method for dependency */
	@Unbind(id = DLIGHT)
	public void unbindDimmerLight(DimmerLight dimmerLight, Map properties) {
		log.info("unbind dimmerlight " + dimmerLight.getSerialNumber());
		dimmerLight.removeListener(this);
	}

	@Bind(id = DLIGHT)
	public void bindDimmerLight(DimmerLight dimmerLight, Map properties) {
		log.info("bind dimmer light " + dimmerLight.getSerialNumber());
		dimmerLight.addListener(this);
	}

	/** Bind Method for presenceSensors dependency */
	@Bind(id = SENSOR)
	public synchronized void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		log.info("bind presence sensor " + presenceSensor.getSerialNumber());
		presenceSensor.addListener(this);
	}

	/** Unbind Method for presenceSensors dependency */
	@Unbind(id = SENSOR)
	public synchronized void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		log.info("Unbind presence sensor " + presenceSensor.getSerialNumber());
		presenceSensor.removeListener(this);
	}

	/** Component Lifecycle Method */
	public synchronized void stop() {
		log.info("Component is stopping...");
		for (PresenceSensor sensor : presenceSensors) {
			sensor.removeListener(this);
		}
	}

	/** Component Lifecycle Mef (changingSensor.getSensedPresence()) {thod */
	public void start() {
		log.info("Component is starting...");
	}

	@Override
	public void deviceAdded(GenericDevice arg0) {

	}

	@Override // Count number of lights on
	public void deviceEvent(GenericDevice arg0, Object arg1) {

	}

	@Override
	public void devicePropertyAdded(GenericDevice arg0, String arg1) {

	}

	@Override
	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {
		// we assume that we listen only to presence sensor events (otherwise
		// there is a bug)
		// assert device instanceof PresenceSensor : "device must be a presence
		// sensors only";

		// based on that assumption we can cast the generic device without
		// checking via instanceof
		if (device instanceof PresenceSensor) {
			PresenceSensor changingSensor = (PresenceSensor) device;
			// check the change is related to presence sensing
			if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {
				// get the location where the sensor is:
				String detectorLocation = (String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME);
				// if the location is known :
				if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
					controlLightsPerRoom(detectorLocation);
				}
			}
			if (propertyName.equals(PresenceSensor.LOCATION_PROPERTY_NAME)) {
				// get the location where the sensor is:
				String detectorLocation = (String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME);
				// if the location is known :
				if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
					controlLightsPerRoom(detectorLocation);
					controlLightsPerRoom((String) oldValue);
				}
			}
		} else if (device instanceof DimmerLight) {
			log.info("light changes\n\n\n");
			DimmerLight changingLight = (DimmerLight) device;
			if (propertyName.equals(DimmerLight.LOCATION_PROPERTY_NAME)) {
				// get the location where the light is:
				String detectorLocation = (String) changingLight.getPropertyValue(LOCATION_PROPERTY_NAME);
				// if the location is known :
				if (!detectorLocation.equals(LOCATION_UNKNOWN)) {

					controlLightsPerRoom(detectorLocation);
					controlLightsPerRoom((String) oldValue);
				}
			}
		} else if (device instanceof BinaryLight) {
			log.info("light changes\n\n\n");
			BinaryLight changingLight = (BinaryLight) device;
			if (propertyName.equals(BinaryLight.LOCATION_PROPERTY_NAME)) {
				// get the location where the light is:
				String detectorLocation = (String) changingLight.getPropertyValue(LOCATION_PROPERTY_NAME);
				// if the location is known :
				if (!detectorLocation.equals(LOCATION_UNKNOWN)) {

					controlLightsPerRoom(detectorLocation);
					controlLightsPerRoom((String) oldValue);
				}
			}
		}
	}

	@Override
	public void devicePropertyRemoved(GenericDevice arg0, String arg1) {

	}

	@Override
	public void deviceRemoved(GenericDevice arg0) {

	}

	private synchronized List<BinaryLight> getBinaryLightFromLocation(String location) {

		List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();
		for (BinaryLight binLight : binaryLights) {
			if (binLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				binaryLightsLocation.add(binLight);
			}
		}
		return binaryLightsLocation;
	}

	private synchronized List<DimmerLight> getDimmerLightFromLocation(String location) {

		List<DimmerLight> dimmerLightLocation = new ArrayList<DimmerLight>();
		for (DimmerLight dimLight : dimmerLights) {
			log.info("\n dimmerlight=" + dimmerLightLocation.size()
					+ "\n\n\n");
			if (dimLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				dimmerLightLocation.add(dimLight);
			}
		}
		return dimmerLightLocation;
	}

	/**
	 * Return nb of Lights on per location
	 * 
	 * @param location
	 * @return
	 */
	private List<BinaryLight> getBinaryLightONFromLocation(String location) {

		List<BinaryLight> binList = getBinaryLightFromLocation(location);
		List<BinaryLight> binLightsON = new ArrayList<>();
		for (BinaryLight lights : binList) {
			if (lights.getPowerStatus()) {
				binLightsON.add(lights);
			}
		}
		return binLightsON;
	}

	private synchronized List<PresenceSensor> getPresenceSensorFromLocation(String location) {

		List<PresenceSensor> presenceSensorsLocation = new ArrayList<PresenceSensor>();
		for (PresenceSensor sensor : presenceSensors) {
			if (sensor.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				presenceSensorsLocation.add(sensor);
			}
		}
		return presenceSensorsLocation;
	}

	private void controlBinaryLightsPerRoom(String location) {

		log.info("\n_\n on est dans " + location + "\n\n");
		// get the related binary lightsBinaryLightFollowMeImpl
		List<BinaryLight> lights = getBinaryLightFromLocation(location);
		List<PresenceSensor> sensors = getPresenceSensorFromLocation(location);
		for (BinaryLight binaryLight : lights) {
			binaryLight.turnOff();
		}
		boolean presence = false;
		for (PresenceSensor sensor : sensors) {
			if (sensor.getSensedPresence()) {
				presence = true;
			}
		}
		if (presence) {
			int sizeList = 0;
			while (sizeList < lights.size() && sizeList < maxLightsToTurnOnPerRoom) {
				lights.get(sizeList).turnOn();
				sizeList++;
			}
		}
	}

	private void controlDimmerLightsPerRoom(String location) {

		log.info("\n_\n on est dans " + location + "\n\n");
		// get the related binary lights
		List<DimmerLight> lights = getDimmerLightFromLocation(location);
		List<PresenceSensor> sensors = getPresenceSensorFromLocation(location);
		for (DimmerLight dimmerLight : lights) {
			dimmerLight.setPowerLevel(0.0);
		}
		boolean presence = false;
		for (PresenceSensor sensor : sensors) {
			if (sensor.getSensedPresence()) {
				presence = true;
			}
		}
		if (presence) {
			int sizeList = 0;
			while (sizeList < lights.size() && sizeList < maxLightsToTurnOnPerRoom) {
				lights.get(sizeList).setPowerLevel(1.0);
				sizeList++;
			}
		}
	}

	private void controlLightsPerRoom(String location) {

		log.info("\n_\n on est dans " + location + "\n\n");
		
		//set maximum lights per room
		this.maxLightsToTurnOnPerRoom=(int) (maximumEnergyConsumptionAllowedInARoom / defaultBinaryLightEnergyConsumption);
		
		System.out.println("\n_\n on est dans " + location + "\n\n"+maxLightsToTurnOnPerRoom+"\n\n\n\n");
		// get the related binary lights
		List<DimmerLight> lights = getDimmerLightFromLocation(location);
		List<BinaryLight> binLights = getBinaryLightFromLocation(location);
		List<PresenceSensor> sensors = getPresenceSensorFromLocation(location);
		for (DimmerLight dimmerLight : lights) {
			dimmerLight.setPowerLevel(0.0);
		}
		for (BinaryLight blight : binLights) {
			blight.turnOff();
		}

		boolean presence = false;
		for (PresenceSensor sensor : sensors) {
			if (sensor.getSensedPresence()) {
				presence = true;
			}
		}
		if (presence) {
			int sizebin = 0;
			int sizedim = 0;
			
			while ((sizedim < lights.size() || sizebin < binLights.size())
					&& sizebin + sizedim < maxLightsToTurnOnPerRoom) {
				if (sizedim < lights.size() && sizebin + sizedim < maxLightsToTurnOnPerRoom) {
					lights.get(sizedim).setPowerLevel(0.1d);
					sizedim++;
				}
				if (sizebin < binLights.size() && sizebin + sizedim < maxLightsToTurnOnPerRoom) {
					binLights.get(sizebin).turnOn();
					sizebin++;
				}
			}

		}
	}

	@Override
	public int getMaximumNumberOfLightsToTurnOn() {
		return maxLightsToTurnOnPerRoom;
	}

	@Override
	public void setMaximumNumberOfLightsToTurnOn(int maximumNumberOfLightsToTurnOn) {
		this.maxLightsToTurnOnPerRoom = maximumNumberOfLightsToTurnOn;
		controlLightsPerRoom(RoomEnum.KITCHEN.getName());
		controlLightsPerRoom(RoomEnum.LIVINGROOM.getName());
		controlLightsPerRoom(RoomEnum.BEDROOM.getName());
		controlLightsPerRoom(RoomEnum.BATHROOM.getName());
	}

	@Override
	public double getMaximumAllowedEnergyInRoom() {
		return maximumEnergyConsumptionAllowedInARoom;
	}

	@Override
	public void setMaximumAllowedEnergyInRoom(double maximumEnergy) {
		if (maximumEnergy >= defaultBinaryLightEnergyConsumption) {
			this.maximumEnergyConsumptionAllowedInARoom = maximumEnergy;
			setMaximumNumberOfLightsToTurnOn((int) (maximumEnergy / defaultBinaryLightEnergyConsumption));
		} else {
			log.info("\nexception : Energie inferieur au minimum\n");
		}
	}
}
