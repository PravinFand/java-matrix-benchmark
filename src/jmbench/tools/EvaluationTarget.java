/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of JMatrixBenchmark.
 *
 * JMatrixBenchmark is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * JMatrixBenchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JMatrixBenchmark.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmbench.tools;

import jmbench.impl.MatrixLibrary;
import jmbench.interfaces.LibraryFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;


/**
 * Provides a description of the library that is being processed and a factory for the functions
 * that are being evaluated.
 *
 * @author Peter Abeles
 */
public class EvaluationTarget implements Serializable {

    private String libName;
    private String factoryPath;

    // this is determined at runtime
    private String version;
    private String modificationData;

    private transient List<String> jarFiles;

    public EvaluationTarget(){

    }

    public EvaluationTarget( MatrixLibrary lib,
                             String factoryPath )
    {
        this.libName = lib.getPlotName();
        this.factoryPath = factoryPath;
    }

    public EvaluationTarget( String libName,
                             String factoryPath )
    {
        this.libName = libName;
        this.factoryPath = factoryPath;
    }

    /**
     * Examines the library's directory to get the list of jar files
     */
    private void loadJarNames() {

        MatrixLibrary lib = MatrixLibrary.lookup(libName);

        if( lib.libraryDirName == null )
            return;

        List<String> jarNames = lib.listOfJarFilePaths();

        if (jarNames == null) return;

        this.jarFiles = jarNames;
    }


    public List<String> getJarFiles() {
        return jarFiles;
    }

    /**
     * Creates a new instance of the algorithm factory for this library
     *
     * @return The RuntimePerformanceFactory for this library.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends LibraryFactory> T loadAlgorithmFactory() {
        loadJarNames();

        loadJars(jarFiles);

        try {
            return (T)Class.forName(factoryPath).newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadJars( List<String> listJars ) {
        if( listJars != null ) {
            for (String name : listJars) {
                try {
                    addURL(new URL("file:" + name));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    /**
     * This is a complete hack I found on the internets.  Seems to be the only way I can
     * do what I want to do.
     *
     * @param u What is to be added to the classpath
     * @throws IOException
     */
    public static void addURL(URL u) throws IOException
    {
        Class[] parameters = new Class[]{URL.class};

        if( !(ClassLoader.getSystemClassLoader() instanceof URLClassLoader )) {
            // see http://forums.sun.com/thread.jspa?threadID=300557&start=0&tstart=0
            throw new RuntimeException("NOOOO so they broke the class loader hack, this is bad.");
        }

        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, u);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    public String getLibName() {
        return libName;
    }

    public void setLibName(String libName) {
        this.libName = libName;
    }

    public String getFactoryPath() {
        return factoryPath;
    }

    public void setFactoryPath(String factoryPath) {
        this.factoryPath = factoryPath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getModificationData() {
        return modificationData;
    }

    public void setModificationData(String modificationData) {
        this.modificationData = modificationData;
    }
}



