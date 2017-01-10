package org.icasa.temperature.regulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.example.temperature.configuration.TemperatureConfiguration;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

@Component
@Instantiate
@Provides(specifications=TemperatureConfiguration.class)
public class TemperatureControllerImpl implements DeviceListener, PeriodicRunnable, TemperatureConfiguration {

	/**
	 * The name of the LOCATION property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * BinaryLight The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";

	/** Field for heaters dependency */
	private Heater[] heaters;
	/** Field for thermometers dependency */
	private Thermometer[] thermometers;
	/** Field for coolers dependency */
	private Cooler[] coolers;

	private double tempKelvinBath = 296.15;
	private double tempKelvinBed = 293.15;
	private double tempKelvinLiving = 291.15;
	private double tempKelvinKitch = 288.15;

	private double precision = 2;

	/** Bind Method for heaters dependency */
	public void bindHeater(Heater heater, Map properties) {
		System.out.println("bind heater" + heater.getSerialNumber());
		heater.addListener(this);
	}

	/** Unbind Method for heaters dependency */
	public void unbindHeater(Heater heater, Map properties) {
		heater.removeListener(this);
	}

	/** Bind Method for thermometers dependency */
	public void bindThermometer(Thermometer thermometer, Map properties) {
		thermometer.addListener(this);
	}

	/** Unbind Method for thermometers dependency */
	public void unbindThermometer(Thermometer thermometer, Map properties) {
		thermometer.removeListener(this);
	}

	/** Bind Method for coolers dependency */
	public void bindCooler(Cooler cooler, Map properties) {
		cooler.addListener(this);
	}

	/** Unbind Method for coolers dependency */
	public void unbindCooler(Cooler cooler, Map properties) {
		cooler.removeListener(this);
	}

	/** Component Lifecycle Method */
	public void stop() {
		// TODO: Add your implementation code here
		System.out.println("stop temperature\n");
	}

	/** Component Lifecycle Method */
	public void start() {
		// TODO: Add your implementation code here
		System.out.println("start temperature\n");
	}

	@Override
	public void deviceAdded(GenericDevice arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceEvent(GenericDevice arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void devicePropertyAdded(GenericDevice arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		if (device instanceof Thermometer) {
			Thermometer thermometer = (Thermometer) device;
			if (propertyName.equals(Thermometer.LOCATION_PROPERTY_NAME)) {
				if (!propertyName.equals(LOCATION_UNKNOWN)) {
					List<Cooler> coolers = getCoolerFromLocation((String) newValue);
					List<Heater> heaters = getHeaterFromLocation((String) newValue);
					if (!coolers.isEmpty() && !heaters.isEmpty()) {

						switch ((String) newValue) {
						case "kitchen":
							modifyTemperature(thermometer, tempKelvinKitch, coolers, heaters);
							break;
						case "bedroom":
							modifyTemperature(thermometer, tempKelvinBed, coolers, heaters);
							break;
						case "bathroom":
							modifyTemperature(thermometer, tempKelvinBath, coolers, heaters);
							break;
						case "livingroom":
							modifyTemperature(thermometer, tempKelvinLiving, coolers, heaters);
							break;
						default:
							System.out.println("Location Unknown");
							break;
						}

					} else {
						for (Heater heater : getHeaterFromLocation((String) newValue)) {
							heater.setPowerLevel(0.0);
						}
						for (Cooler cooler : getCoolerFromLocation((String) newValue)) {
							cooler.setPowerLevel(0.0);
						}
					}
					if (getThermometerFromLocation((String) oldValue).isEmpty()) {
						for (Heater heater : getHeaterFromLocation((String) oldValue)) {
							heater.setPowerLevel(0.0);
						}
						for (Cooler cooler : getCoolerFromLocation((String) oldValue)) {
							cooler.setPowerLevel(0.0);
						}
					}
				}
			}
		} else if (device instanceof Cooler) {
			Cooler cooler = (Cooler) device;
			if (propertyName.equals(Cooler.LOCATION_PROPERTY_NAME)) {
				if (!propertyName.equals(LOCATION_UNKNOWN)) {
					List<Thermometer> thermometers = getThermometerFromLocation((String) newValue);
					List<Heater> heaters = getHeaterFromLocation((String) newValue);
					if (!thermometers.isEmpty() && !heaters.isEmpty()) {
						List<Cooler> coolers = new ArrayList<Cooler>(1);
						coolers.add(cooler);

						switch ((String) newValue) {
						case "kitchen":
							modifyTemperature(thermometers.get(0), tempKelvinKitch, coolers, heaters);
							break;
						case "bedroom":
							modifyTemperature(thermometers.get(0), tempKelvinBed, coolers, heaters);
							break;
						case "bathroom":
							modifyTemperature(thermometers.get(0), tempKelvinBath, coolers, heaters);
							break;
						case "livingroom":
							modifyTemperature(thermometers.get(0), tempKelvinLiving, coolers, heaters);
							break;
						default:
							System.out.println("Location Unknown");
							break;
						}

					} else {
						cooler.setPowerLevel(0.0);
					}
				}
			}
		} else if (device instanceof Heater) {
			Heater heater = (Heater) device;
			if (propertyName.equals(Heater.LOCATION_PROPERTY_NAME)) {
				if (!propertyName.equals(LOCATION_UNKNOWN)) {
					List<Cooler> coolers = getCoolerFromLocation((String) newValue);
					List<Thermometer> thermometers = getThermometerFromLocation((String) newValue);
					if (!coolers.isEmpty() && !thermometers.isEmpty()) {
						List<Heater> heaters = new ArrayList<Heater>(1);
						heaters.add(heater);

						switch ((String) newValue) {
						case "kitchen":
							modifyTemperature(thermometers.get(0), tempKelvinKitch, coolers, heaters);
							break;
						case "bedroom":
							modifyTemperature(thermometers.get(0), tempKelvinBed, coolers, heaters);
							break;
						case "bathroom":
							modifyTemperature(thermometers.get(0), tempKelvinBath, coolers, heaters);
							break;
						case "livingroom":
							modifyTemperature(thermometers.get(0), tempKelvinLiving, coolers, heaters);
							break;
						default:
							System.out.println("Location Unknown");
							break;
						}
					} else {
						heater.setPowerLevel(0.0);
					}
				}
			}
		}
	}

	@Override
	public void devicePropertyRemoved(GenericDevice arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceRemoved(GenericDevice arg0) {
		// TODO Auto-generated method stub

	}

	public void modifyTemperature(Thermometer thermometer, double tempKelvin, List<Cooler> coolers,
			List<Heater> heaters) {
		System.out.println(thermometer.getTemperature()
				+ "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
		for (Heater heater : heaters) {
			heater.setPowerLevel(0.0);
		}
		for (Cooler cooler : coolers) {
			cooler.setPowerLevel(0.0);
		}
		if (thermometer.getTemperature() < (tempKelvin - precision)) {
			System.out.println("\t active heater \n\t" + (tempKelvin - precision));
			heaters.get(0).setPowerLevel(0.01);
			coolers.get(0).setPowerLevel(0.0);
		} else if (thermometer.getTemperature() > (tempKelvin + precision)) {
			System.out.println("\t active cooler \n\t" + (tempKelvin - precision));
			heaters.get(0).setPowerLevel(0.0);
			coolers.get(0).setPowerLevel(0.01);
		}
	}

	private synchronized List<Cooler> getCoolerFromLocation(String location) {

		List<Cooler> coolersLocation = new ArrayList<Cooler>();
		for (Cooler cooler : coolers) {
			if (cooler.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				coolersLocation.add(cooler);
			}
		}
		return coolersLocation;
	}

	private synchronized List<Heater> getHeaterFromLocation(String location) {

		List<Heater> heatersLocation = new ArrayList<Heater>();
		for (Heater heater : heaters) {
			if (heater.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				heatersLocation.add(heater);
			}
		}
		return heatersLocation;
	}

	private synchronized List<Thermometer> getThermometerFromLocation(String location) {

		List<Thermometer> thermometersLocation = new ArrayList<Thermometer>();
		for (Thermometer thermometer : thermometers) {
			if (thermometer.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				thermometersLocation.add(thermometer);
			}
		}
		return thermometersLocation;
	}

	@Override
	public void run() {
		for (Thermometer thermometer : this.thermometers) {
			String location = (String) thermometer.getPropertyValue(LOCATION_PROPERTY_NAME);
			if (!location.equals(LOCATION_UNKNOWN) && !getCoolerFromLocation(location).isEmpty()
					&& !getHeaterFromLocation(location).isEmpty()) {
				System.out.println("Run Regulation de temperature dans la piece \n" + location + "\n");

				switch (location) {
				case "kitchen":
					modifyTemperature(thermometer, tempKelvinKitch, getCoolerFromLocation(location),
							getHeaterFromLocation(location));
					break;
				case "bedroom":
					modifyTemperature(thermometer, tempKelvinBed, getCoolerFromLocation(location),
							getHeaterFromLocation(location));
					break;
				case "bathroom":
					modifyTemperature(thermometer, tempKelvinBath, getCoolerFromLocation(location),
							getHeaterFromLocation(location));
					break;
				case "livingroom":
					modifyTemperature(thermometer, tempKelvinLiving, getCoolerFromLocation(location),
							getHeaterFromLocation(location));
					break;
				default:
					System.out.println("Location Unknown");
					break;
				}

			} else {
				for (Heater heater : getHeaterFromLocation((String) location)) {
					heater.setPowerLevel(0.0);
				}
				for (Cooler cooler : getCoolerFromLocation((String) location)) {
					cooler.setPowerLevel(0.0);
				}
			}
		}
	}

	@Override
	public long getPeriod() {
		return 5000;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.SECONDS;
	}

	@Override
	public void setTargetedTemperature(String targetedRoom, float temperature) {
		switch (targetedRoom) {
		case "kitchen":
			this.tempKelvinKitch = temperature + (float) 273.15;
			System.out.println("Hellooo " + temperature + (float) 273.15 + "\n");
			break;
		case "bathroom":
			this.tempKelvinBath = temperature + (float) 273.15;
			System.out.println("Hellooo " + temperature + (float) 273.15 + "\n");
			break;
		case "bedroom":
			this.tempKelvinBed = temperature + (float) 273.15;
			System.out.println("Hellooo " + temperature + (float) 273.15 + "\n");
			break;
		case "livingroom":
			this.tempKelvinLiving = temperature + (float) 273.15;
			System.out.println("Hellooo " + temperature + (float) 273.15 + "\n");
			break;
		default:
			System.out.println("Unknown location");
			break;
		}

	}

	@Override
	public float getTargetedTemperature(String room) {
		switch (room) {
		case "kitchen":
			return (float) this.tempKelvinKitch - (float) 273.15;
		case "bathroom":
			return (float) this.tempKelvinBath - (float) 273.15;
		case "bedroom":
			return (float) this.tempKelvinBed - (float) 273.15;
		case "livingroom":
			return (float) this.tempKelvinLiving - (float) 273.15;
		default:
			System.out.println("Unknown location");
			return 0;
		}
	}
}
