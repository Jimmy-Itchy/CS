package org.icasa.temperature.regulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.example.follow.me.api.TemperatureConfiguration;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

@Component
@Instantiate
@Provides(specifications = {TemperatureConfiguration.class,PeriodicRunnable.class})
public class TemperatureControllerImpl implements DeviceListener, PeriodicRunnable, TemperatureConfiguration {

	private static Logger log = Logger.getLogger(TemperatureControllerImpl.class.getName());
	private static final String COOL="cool";
	private static final String HEAT="heat";
	private static final String THERMO="thermo";
	
	/**
	 * The name of the LOCATION property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * BinaryLight The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";

	private static final String KITCHEN="kitchen";
	private static final String LIVINGROOM="livingroom";
	private static final String BATHROOM="bathroom";
	private static final String BEDROOM="bedroom";
	
	private double tempKelvinBath = 296.15;
	private double tempKelvinBed = 293.15;
	private double tempKelvinLiving = 291.15;
	private double tempKelvinKitch = 288.15;

	private double precision = 2;

	/** Field for heaters dependency */
	@Requires(id = HEAT, optional = true)
	private Heater[] heaters;

	/** Field for thermometers dependency */
	@Requires(id = THERMO, optional = true)
	private Thermometer[] thermometers;

	/** Field for coolers dependency */
	@Requires(id = COOL, optional = true)
	private Cooler[] coolers;

	/** Bind Method for heaters dependency */
	@Bind(id = HEAT)
	public void bindHeater(Heater heater, Map properties) {
		log.info("bind heater ");
		heater.addListener(this);
	}

	/** Unbind Method for heaters dependency */
	@Unbind(id = HEAT)
	public void unbindHeater(Heater heater, Map properties) {
		heater.removeListener(this);
	}

	/** Bind Method for thermometers dependency */
	@Bind(id = THERMO)
	public void bindThermometer(Thermometer thermometer, Map properties) {
		thermometer.addListener(this);
	}

	/** Unbind Method for thermometers dependency */
	@Unbind(id = THERMO)
	public void unbindThermometer(Thermometer thermometer, Map properties) {
		thermometer.removeListener(this);
	}

	/** Bind Method for coolers dependency */
	@Bind(id = COOL)
	public void bindCooler(Cooler cooler, Map properties) {
		cooler.addListener(this);
	}

	/** Unbind Method for coolers dependency */
	@Unbind(id = COOL)
	public void unbindCooler(Cooler cooler, Map properties) {
		cooler.removeListener(this);
	}

	/** Component Lifecycle Method */
	public void stop() {
		log.info("stop temperature\n");
	}

	/** Component Lifecycle Method */
	public void start() {
		log.info("start temperature\n");
	}

	@Override
	public void deviceAdded(GenericDevice arg0) {
		log.info("deviceadded");
	}

	@Override
	public void deviceEvent(GenericDevice arg0, Object arg1) {
		log.info("deviceEvent");
	}

	@Override
	public void devicePropertyAdded(GenericDevice arg0, String arg1) {
		log.info("deviceProperty");
	}

	@Override
	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {
		if (device instanceof Thermometer) {
			Thermometer thermometer = (Thermometer) device;
			if (propertyName.equals(Thermometer.LOCATION_PROPERTY_NAME)) {
				if (!propertyName.equals(LOCATION_UNKNOWN)) {
					List<Cooler> coolers = getCoolerFromLocation((String) newValue);
					List<Heater> heaters = getHeaterFromLocation((String) newValue);
					if (!coolers.isEmpty() && !heaters.isEmpty()) {

						switch ((String) newValue) {
						case KITCHEN:
							modifyTemperature(thermometer, tempKelvinKitch, coolers, heaters);
							break;
						case BEDROOM:
							modifyTemperature(thermometer, tempKelvinBed, coolers, heaters);
							break;
						case BATHROOM:
							modifyTemperature(thermometer, tempKelvinBath, coolers, heaters);
							break;
						case LIVINGROOM:
							modifyTemperature(thermometer, tempKelvinLiving, coolers, heaters);
							break;
						default:
							log.info("Location Unknown");
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
						case KITCHEN:
							modifyTemperature(thermometers.get(0), tempKelvinKitch, coolers, heaters);
							break;
						case BEDROOM:
							modifyTemperature(thermometers.get(0), tempKelvinBed, coolers, heaters);
							break;
						case BATHROOM:
							modifyTemperature(thermometers.get(0), tempKelvinBath, coolers, heaters);
							break;
						case LIVINGROOM:
							modifyTemperature(thermometers.get(0), tempKelvinLiving, coolers, heaters);
							break;
						default:
							log.info("Location Unknown");
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
						case KITCHEN:
							modifyTemperature(thermometers.get(0), tempKelvinKitch, coolers, heaters);
							break;
						case BEDROOM:
							modifyTemperature(thermometers.get(0), tempKelvinBed, coolers, heaters);
							break;
						case BATHROOM:
							modifyTemperature(thermometers.get(0), tempKelvinBath, coolers, heaters);
							break;
						case LIVINGROOM:
							modifyTemperature(thermometers.get(0), tempKelvinLiving, coolers, heaters);
							break;
						default:
							log.info("Location Unknown");
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

	}

	@Override
	public void deviceRemoved(GenericDevice arg0) {

	}

	public void modifyTemperature(Thermometer thermometer, double tempKelvin, List<Cooler> coolers,
			List<Heater> heaters) {
		log.info("\n Temperature= "+thermometer.getTemperature());
		for (Heater heater : heaters) {
			heater.setPowerLevel(0.0);
		}
		for (Cooler cooler : coolers) {
			cooler.setPowerLevel(0.0);
		}
		if (thermometer.getTemperature() < (tempKelvin - precision)) {
			log.info("\t active heater %s\n\t" + (tempKelvin - precision));
			heaters.get(0).setPowerLevel(0.01);
			coolers.get(0).setPowerLevel(0.0);
		} else if (thermometer.getTemperature() > (tempKelvin + precision)) {
			log.info("\t active heater %s\n\t" + (tempKelvin - precision));
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
		log.info("\n\n\nRun Regulation de temperature dans la piece \n\n");
		for (Thermometer thermometer : this.thermometers) {
			String location = (String) thermometer.getPropertyValue(LOCATION_PROPERTY_NAME);
			if (!location.equals(LOCATION_UNKNOWN) && !getCoolerFromLocation(location).isEmpty()
					&& !getHeaterFromLocation(location).isEmpty()) {
				log.info("\n\n\nRun Regulation de temperature dans la piece "+location);

				switch (location) {
				case KITCHEN:
					modifyTemperature(thermometer, tempKelvinKitch, getCoolerFromLocation(location),
							getHeaterFromLocation(location));
					break;
				case BEDROOM:
					modifyTemperature(thermometer, tempKelvinBed, getCoolerFromLocation(location),
							getHeaterFromLocation(location));
					break;
				case BATHROOM:
					modifyTemperature(thermometer, tempKelvinBath, getCoolerFromLocation(location),
							getHeaterFromLocation(location));
					break;
				case LIVINGROOM:
					modifyTemperature(thermometer, tempKelvinLiving, getCoolerFromLocation(location),
							getHeaterFromLocation(location));
					break;
				default:
					log.info("Location unknown");
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
		case KITCHEN:
			this.tempKelvinKitch = temperature + (float) 273.15;
			break;
		case BATHROOM:
			this.tempKelvinBath = temperature + (float) 273.15;
			break;
		case BEDROOM:
			this.tempKelvinBed = temperature + (float) 273.15;
			break;
		case LIVINGROOM:
			this.tempKelvinLiving = temperature + (float) 273.15;
			break;
		default:
			log.info("unknown location");
			break;
		}

	}

	@Override
	public float getTargetedTemperature(String room) {
		switch (room) {
		case KITCHEN:
			return (float) this.tempKelvinKitch - (float) 273.15;
		case BATHROOM:
			return (float) this.tempKelvinBath - (float) 273.15;
		case BEDROOM:
			return (float) this.tempKelvinBed - (float) 273.15;
		case LIVINGROOM:
			return (float) this.tempKelvinLiving - (float) 273.15;
		default:
			log.info("UnknOwn location");
			return 0;
		}
	}
}
