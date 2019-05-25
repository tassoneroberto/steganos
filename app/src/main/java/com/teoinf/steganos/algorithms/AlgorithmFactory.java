package com.teoinf.steganos.algorithms;

import com.teoinf.steganos.error.ErrorManager;
public class AlgorithmFactory {

	public static ISteganographyContainer getSteganographyContainerInstanceFromName(String name) {
		ISteganographyContainer algorithm = null;
		Class<?> algorithmClass;
		ErrorManager errorManager = ErrorManager.getInstance();
		
		try {
		    algorithmClass = Class.forName(name); 
		    algorithm = (ISteganographyContainer) algorithmClass.newInstance();
		} catch (ClassNotFoundException e) {
			errorManager.addErrorMessage("[Algorithm Factory]: Unable to find class " + name);
		} catch (InstantiationException e) {
			errorManager.addErrorMessage("[Algorithm Factory]: Failed to instantiate class " + name);
		} catch (IllegalAccessException e) {
			errorManager.addErrorMessage("[Algorithm Factory]: Illegal access to " + name);
		}
		return (algorithm);
	}
	
	public static ICryptographyAlgorithm getCryptographyAlgorithmInstanceFromName(String name) {
		ICryptographyAlgorithm algorithm = null;
		Class<?> algorithmClass;
		ErrorManager errorManager = ErrorManager.getInstance();
		
		try {
		    algorithmClass = Class.forName(name); 
		    algorithm = (ICryptographyAlgorithm) algorithmClass.newInstance();
		} catch (ClassNotFoundException e) {
			errorManager.addErrorMessage("[Algorithm Factory]: Unable to find class " + name);
		} catch (InstantiationException e) {
			errorManager.addErrorMessage("[Algorithm Factory]: Failed to instantiate class " + name);
		} catch (IllegalAccessException e) {
			errorManager.addErrorMessage("[Algorithm Factory]: Illegal access to " + name);
		}
		return (algorithm);
	}
}
