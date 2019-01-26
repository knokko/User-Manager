package nl.knokko.usermanager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.Arrays;

import nl.knokko.util.bits.BitInput;
import nl.knokko.util.bits.ByteArrayBitInput;
import nl.knokko.util.bits.ByteArrayBitOutput;

class LowerUserGroup<D extends UserData> {
	
	private final File dataFile;
	
	private final Class<D> userDataClass;
	
	private D[] users;
	
	@SuppressWarnings("unchecked")
	LowerUserGroup(Class<D> userDataClass, File dataFile){
		this.userDataClass = userDataClass;
		this.dataFile = dataFile;
		
		// Load data if available
		if (dataFile.exists()) {
			try {
				BitInput input = ByteArrayBitInput.fromFile(dataFile);
				int amount = input.readInt();
				this.users = (D[]) Array.newInstance(userDataClass, amount);
				for (int index = 0; index < amount; index++) {
					this.users[index] = createUserData(input.readLong());
					this.users[index].load(input);
				}
				input.terminate();
			} catch (IOException ioex) {
				throw new RuntimeException("Failed to read data file " + dataFile, ioex);
			}
		} else {
			this.users = (D[]) Array.newInstance(userDataClass, 0);
		}
	}
	
	void save() {
		
		// Avoid concurrency
		D[] users = this.users;
		
		// Store all bytes to save in the output array
		ByteArrayBitOutput output = new ByteArrayBitOutput();
		output.addInt(users.length);
		for (D user : users) {
			user.save(output);
		}
		output.terminate();
		
		// Save the actual data to the file
		synchronized (dataFile) {
			try {
				OutputStream outputStream = Files.newOutputStream(dataFile.toPath());
				outputStream.write(output.getBytes());
				outputStream.flush();
				outputStream.close();
			} catch (IOException ioex) {
				throw new RuntimeException("Failed to save data to datafile", ioex);
			}
		}
	}
	
	D getUserData(long id) {
		
		// Avoid annoying concurrency
		D[] users = this.users;
		
		D user = searchUserData(users, id);
		if (user == null) {
			
			// Make sure it is impossible that 2 users are added at the same time
			synchronized (this) {
				
				// Possibly, the user was added in the meantime
				user = searchUserData(users, id);
				if (user == null) {
					user = createUserData(id);
					D[] newUsers = Arrays.copyOf(users, users.length + 1);
					newUsers[users.length] = user;
					this.users = newUsers;
				}
			}
		}
		
		return user;
	}
	
	private D createUserData(long id) {
		try {
			return userDataClass.getConstructor(long.class).newInstance(id);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to initialise new user data: (UserData class must have public constructor with a single long parameter)", ex);
		}
	}
	
	private D searchUserData(D[] users, long id) {
		for (D user : users) {
			if (user.getID() == id) {
				return user;
			}
		}
		return null;
	}
}