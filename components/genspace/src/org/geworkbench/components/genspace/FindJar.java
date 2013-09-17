package org.geworkbench.components.genspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FindJar {
	private static int globalClassConflicts = 0;
	private static int genspaceClassConflicts = 0;
	
	private static int globalJarConflicts = 0;
	private static int genspaceJarConflicts = 0;
	
	private static HashSet<String> globalComponentJarsConflicting = new HashSet<String>();
	private static HashSet<String> genspaceComponentJarsConflicting = new HashSet<String>();
	private static void traverseFoldersClassesAndCheck(File folder)
			throws FileNotFoundException, IOException {

		if (folder.exists()) {
			File[] libFiles = folder.listFiles();
			for (int i = 0; i < libFiles.length; i++) {
				File file = libFiles[i];
				if (!file.isDirectory()) {
					String name = file.getName().toLowerCase();
					if (name.endsWith(".jar")) {
						// file.toURL() is obsolete
						/* see http://www.jguru.com/faq/view.jsp?EID=1280051 */
						JarInputStream jarFile = new JarInputStream(
								new FileInputStream(file));
						JarEntry jarEntry;

						while (true) {
							jarEntry = jarFile.getNextJarEntry();
							if (jarEntry == null) {
								break;
							}
							if (classesToJar.containsKey(jarEntry.getName())) {
								if(classesToJar.get(jarEntry.getName()).contains("genspace"))
								{
									genspaceClassConflicts++;
									genspaceComponentJarsConflicting.add(folder.getPath() + "/" + name);
								}
								else
								{
									globalClassConflicts++;
									globalComponentJarsConflicting.add(folder.getPath() + "/" + name);
								}
								if(!conflictsMap.containsKey(classesToJar.get(jarEntry.getName())))
									conflictsMap.put(classesToJar.get(jarEntry.getName()), new HashSet<String>());
								conflictsMap.get(classesToJar.get(jarEntry.getName())).add(folder.getPath() + "/" + name);
							}
						}

					}
				}
			}
		}
//		if(conflicts.size() > 0)
//			for(String s : conflicts.keySet())
//			{
//				if(conflicts.get(s).contains("genspace"))
//					genspaceJarConflicts++;
//				else
//					globalJarConflicts++;
//				System.out.println(folder.getPath() + "/" + s + " conflicts with " + conflicts.get(s));
//			}
	}
	public static HashMap<String, String> classesToJar = new HashMap<String, String>();
	public static HashMap<String, HashSet<String>> conflictsMap = new HashMap<String, HashSet<String>>();
	public static void main(String[] args) throws Exception {
		String s = "SAAJ";
		findJar("lib/",s);
System.out.println("genspace now:");
		
		findJar("components/genspace/lib/",s);
		
	}
	public static void findJar(String f, String s) throws Exception
	{
		File folder = new File(f);
		File[] libFiles = folder.listFiles();
		for (int i = 0; i < libFiles.length; i++) {
			File file = libFiles[i];
			if (!file.isDirectory()) {
				String name = file.getName().toLowerCase();
//				System.out.println(name);
				if (name.endsWith(".jar")) {
					// file.toURL() is obsolete
					/* see http://www.jguru.com/faq/view.jsp?EID=1280051 */
					JarInputStream jarFile = new JarInputStream(
							new FileInputStream(file));
					JarEntry jarEntry;

					while (true) {
						jarEntry = jarFile.getNextJarEntry();
						if (jarEntry == null) {
							break;
						}
						if(jarEntry.getName().contains(s))
						{
							System.out.println("Found " + jarEntry.getName()+ " in " + name);
						}
					}
				}
			}
		}

	}
	public static void conflictCheck() throws Exception
	{

		File libdir;
//		
		libdir = new File("lib");
		if (libdir.exists()) {
			File[] libFiles = libdir.listFiles();
			for (int i = 0; i < libFiles.length; i++) {
				File file = libFiles[i];
				if (!file.isDirectory()) {
					String name = file.getName().toLowerCase();
					if (name.endsWith(".jar")) {
						// file.toURL() is obsolete
						/* see http://www.jguru.com/faq/view.jsp?EID=1280051 */
						JarInputStream jarFile = new JarInputStream(
								new FileInputStream(file));
						JarEntry jarEntry;

						while (true) {
							jarEntry = jarFile.getNextJarEntry();
							if (jarEntry == null) {
								break;
							}
							if ((jarEntry.getName().contains(".class"))) {
								classesToJar.put(jarEntry.getName(),"lib/" + name);
							}
						}

					}
				}
			}
		}
		
		libdir = new File("lib/genspace");
		if (libdir.exists()) {
			File[] libFiles = libdir.listFiles();
			for (int i = 0; i < libFiles.length; i++) {
				File file = libFiles[i];
				if (!file.isDirectory()) {
					String name = file.getName().toLowerCase();
					if (name.endsWith(".jar")) {
						// file.toURL() is obsolete
						/* see http://www.jguru.com/faq/view.jsp?EID=1280051 */
						JarInputStream jarFile = new JarInputStream(
								new FileInputStream(file));
						JarEntry jarEntry;

						while (true) {
							jarEntry = jarFile.getNextJarEntry();
							if (jarEntry == null) {
								break;
							}
							if ((jarEntry.getName().contains(".class"))) {
								classesToJar.put(jarEntry.getName(),"lib/genspace/" + name);
							}
						}

					}
				}
			}
		}


		
		System.out.println("-----CONFLICTS REPORT------");
		File components = new File("components");
		for(File f : components.listFiles())
		{
			libdir = new File(f.getPath() + "/lib");
			if(libdir.exists())
			{
				traverseFoldersClassesAndCheck(libdir);
			}
		}
		ArrayList<String> keys = new ArrayList<String>(conflictsMap.keySet());
		Collections.sort(keys);
		int numConflictingCoreJars= 0;
		int numConflictingGenSpaceJars= 0;
		for(String s : keys)
		{
			if(s.contains("genspace"))
				numConflictingGenSpaceJars++;
			else
				numConflictingCoreJars++;
			System.out.println(s);
			for(String b : conflictsMap.get(s))
			{
				if(s.contains("genspace"))
					genspaceJarConflicts++;
				else
					globalJarConflicts++;
				System.out.println("\t" + b);
			}
		}
		System.out.println("-------SUMMARY-------");
		System.out.println("Total # of class conflicts from lib/: " + globalClassConflicts);
		System.out.println("Total # of class conflicts from lib/genspace: " + genspaceClassConflicts);
		System.out.println("Total # of jar conflicts from lib/: " + globalJarConflicts);
		System.out.println("Total # of jar conflicts from lib/genspace/: " + genspaceJarConflicts);
		System.out.println("# of lib/ jars in conflict with components: " + numConflictingCoreJars);
		System.out.println("# of lib/genspace/ jars in conflict with components: " + numConflictingGenSpaceJars);
		System.out.println("# of component jars in conflict with lib/: " + globalComponentJarsConflicting.size());
		System.out.println("# of component jars in conflict with lib/genspace/: " + genspaceComponentJarsConflicting.size());
		genspaceComponentJarsConflicting.removeAll(globalComponentJarsConflicting);
		System.out.println("# of component jars in conflict with lib/genspace/ but NOT lib/: " + genspaceComponentJarsConflicting.size() + " (" + genspaceComponentJarsConflicting.iterator().next() + ")");

	}
}
