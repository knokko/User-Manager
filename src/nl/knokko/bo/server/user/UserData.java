package nl.knokko.bo.server.user;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.BitOutput;

/**
 * Subclasses of this class should contain per-user data. They should have a single constructor that
 * takes the id and pass it along to the constructor of this class. Override the saveData and loadData
 * methods to save and load all necessary data.
 * @author knokko
 *
 */
public abstract class UserData {
	
	private final long id;
	
	public UserData(long id) {
		this.id = id;
	}
	
	/**
	 * @return The unique ID of the user that 'owns' this user data
	 */
	public long getID() {
		return id;
	}
	
	void save(BitOutput output) {
		output.addLong(id);
		saveData(output);
	}
	
	protected abstract void saveData(BitOutput output);
	
	void load(BitInput input) {
		loadData(input);
	}
	
	protected abstract void loadData(BitInput input);
}