package nl.knokko.usermanager;

import java.io.File;
import java.lang.reflect.Array;

class UpperUserGroup<D extends UserData> {
	
	private final File directory;
	
	private final Class<D> userDataClass;
	
	private final LowerUserGroup<D>[] lowerUserGroups;
	
	@SuppressWarnings("unchecked")
	UpperUserGroup(Class<D> userDataClass, File directory, int bufferSize) {
		this.userDataClass = userDataClass;
		this.directory = directory;
		this.lowerUserGroups = (LowerUserGroup<D>[]) Array.newInstance(LowerUserGroup.class, bufferSize);
	}
	
	void save() {
		directory.mkdirs();
		synchronized (lowerUserGroups) {
			for (int index = 0; index < lowerUserGroups.length; index++) {
				if (lowerUserGroups[index] != null) {
					lowerUserGroups[index].save();
				}
			}
		}
	}
	
	LowerUserGroup<D> getGroup(int index) {
		LowerUserGroup<D> lowerGroup = lowerUserGroups[index];
		if (lowerGroup == null) {
			
			// Synchronization is necessary here
			synchronized (lowerUserGroups) {
				if (lowerUserGroups[index] == null) {
					lowerGroup = new LowerUserGroup<D>(userDataClass, getGroupFile(index));
					lowerUserGroups[index] = lowerGroup;
				} else {
					lowerGroup = lowerUserGroups[index];
				}
			}
		}
		return lowerGroup;
	}
	
	private File getGroupFile(int index) {
		return new File(directory + "/group" + index + ".bin");
	}
}