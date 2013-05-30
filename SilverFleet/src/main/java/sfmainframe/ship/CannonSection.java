package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sfmainframe.Commons;
import sfmainframe.ReusableElement;
import sfmainframe.ReusableMap;

public class CannonSection {

	private final Map<GunCompartment, ReusableMap<Gun>> cannons;

	public CannonSection() {
		cannons = new HashMap<GunCompartment, ReusableMap<Gun>>();

		for (GunCompartment location : GunCompartment.getValues()) {
			ReusableMap<Gun> gunMap = new ReusableMap<Gun>(Gun.class,
					Gun.getValues());
			cannons.put(location, gunMap);
		}
	}

	public CannonSection(CannonSection cannonSection) {
		this();

		for (Gun type : Gun.getValues())
			for (GunCompartment location : GunCompartment.getValues()) {
				ReusableElement elem = cannonSection.cannons.get(location)
						.getElement(type);
				cannons.get(location).getElement(type).copy(elem);
			}
	}

	public CannonSection(ShipClass shipClass) {
		this();

		for (GunCompartment location : GunCompartment.getValues()) {
			for (Gun type : Gun.getValues()) {
				int nCannons = shipClass.getCannonMax()[location.ordinal()][type
						.ordinal()];
				this.cannons.get(location).getElement(type).setReady(nCannons);
			}
		}
	}

	public void clear() {
		for (Gun type : Gun.getValues())
			for (GunCompartment location : GunCompartment.getValues())
				cannons.get(location).getElement(type).clear();
	}

	public void useCannon(GunCompartment compartment, Gun gunType) {
		cannons.get(compartment).use(gunType, 1);
	}

	public void modifyCannonsNumber(GunCompartment location, Gun type,
			int number) {
		cannons.get(location).getElement(type).modifyUsed(number);
	}

	public int getCannonsNumber(GunCompartment location, Gun _type, int _state) {
		switch (_state) {
		case Commons.READY:
			return cannons.get(location).getElement(_type).getReady();
		case Commons.USED:
			return cannons.get(location).getElement(_type).getUsed();
		case Commons.BOTH:
			return cannons.get(location).getElement(_type).getTotal();
		default:
			throw new IllegalArgumentException();
		}
	}

	public void destroyCannon(GunCompartment location, Gun type, int _state) {
		ReusableElement elem = cannons.get(location).getElement(type);
		if ((_state == Commons.READY || _state == Commons.BOTH)
				&& elem.getReady() > 0)
			elem.modifyReady(-1);
		else if (elem.getUsed() > 0)
			elem.modifyUsed(-1);
	}

	public int getTotalCannonNumber() {
		int cannons = 0;
		for (GunCompartment location : GunCompartment.getValues())
			for (Gun gt : Gun.getValues())
				cannons += getCannonsNumber(location, gt, Commons.BOTH);
		return cannons;
	}

	public void prepareForNewTurn() {
		for (GunCompartment location : GunCompartment.getValues())
			cannons.get(location).refresh();
	}

	public void writeToStream(DataOutputStream dos) throws IOException {
		for (GunCompartment gc : GunCompartment.getValues())
			for (Gun g : Gun.values()) {
				dos.writeInt(cannons.get(gc).getElement(g).getReady());
				dos.writeInt(cannons.get(gc).getElement(g).getUsed());
			}
	}

	public void readFromStream(DataInputStream dis) throws IOException {
		for (GunCompartment gc : GunCompartment.getValues())
			for (Gun g : Gun.values()) {
				cannons.get(gc).getElement(g).setReady(dis.readInt());
				cannons.get(gc).getElement(g).setUsed(dis.readInt());
			}
	}
}
