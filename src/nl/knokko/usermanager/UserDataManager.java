package nl.knokko.usermanager;

import java.io.File;
import java.lang.reflect.Array;

public class UserDataManager<D extends UserData> {
	
	private final File userDirectory;
	private final Class<D> userDataClass;
	
	private final UpperUserGroup<D>[] upperUserGroups;
	
	private final int lowerBufferSize;
	
	@SuppressWarnings("unchecked")
	public UserDataManager(Class<D> userDataClass, File userDirectory, int upperBufferSize, int lowerBufferSize) {
		this.userDataClass = userDataClass;
		this.userDirectory = userDirectory;
		this.upperUserGroups = (UpperUserGroup<D>[]) Array.newInstance(UpperUserGroup.class, upperBufferSize);
		this.lowerBufferSize = lowerBufferSize;
	}
	
	public void save() {
		synchronized (upperUserGroups) {
			userDirectory.mkdirs();
			for (int index = 0; index < upperUserGroups.length; index++) {
				if (upperUserGroups[index] != null) {
					upperUserGroups[index].save();
				}
			}
		}
	}
	
	public D getUserData(long id) {
		/*
		 * Let u be upperBufferSize (which is upperUserGroups.length)
		 * let l be lowerBufferSize
		 * The first l ids should go in the first upper buffer
		 * The next l ids should go in second upper buffer...
		 * 
		 * after u * l ids, we should start over
		 */
		int bigIndex = (int) (id % (upperUserGroups.length * lowerBufferSize));
		int upperIndex = bigIndex / lowerBufferSize;
		return getUpperGroup(upperIndex).getGroup(bigIndex - upperIndex * lowerBufferSize).getUserData(id);
	}
	
	private UpperUserGroup<D> getUpperGroup(int index){
		UpperUserGroup<D> upperGroup = upperUserGroups[index];
		if (upperGroup == null) {
			
			// Synchronized operations are expensive, so only use if necessary
			synchronized (upperUserGroups) {
				
				// This check is for the case the group has been put in the meantime.
				if (upperUserGroups[index] == null) {
					upperGroup = new UpperUserGroup<D>(userDataClass, getUserGroupFolder(index), lowerBufferSize);
					upperUserGroups[index] = upperGroup;
				} else {
					upperGroup = upperUserGroups[index];
				}
			}
		}
		return upperGroup;
	}
	
	private File getUserGroupFolder(int index) {
		return new File(userDirectory + "/group" + index);
	}
}