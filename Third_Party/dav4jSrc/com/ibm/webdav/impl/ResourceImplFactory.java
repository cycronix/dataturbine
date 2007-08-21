package com.ibm.webdav.impl;

/*
 * (C) Copyright IBM Corp. 2000  All rights reserved.
 *
 * The program is provided "AS IS" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 * 
 * Portions Copyright (C) Simulacra Media Ltd, 2004.
 */
import java.util.*;

import com.ibm.webdav.*;


/** ResourceImplFactory creates implementations of the namespace,
 * properties, and lock manager for a resource based in its URL.
 * This allows methods to be dispatch to different underlying repository
 * managers based on the resource it is operating on.
 * @author Jim Amsden &lt;jamsden@us.ibm.com&gt;
 * @see ResourceImpl
 * @see CollectionImpl
 * @see NamespaceManager
 * @see PropertiesManager
 * @see LockManager
 */
public class ResourceImplFactory extends Object {
    /*private static String defaultRepository = "fileSystem";
    private static String lockmanagerClass = "com.ibm.webdav.fileSystem.LockManager";
    private static String namespacemanagerClass = "com.ibm.webdav.fileSystem.NamespaceManager";
    private static String propertiesmanagerClass = "com.ibm.webdav.fileSystem.PropertiesManager";*/
    private static String defaultRepository = "harmonise";
    private static String searchmanagerClass = "org.openharmonise.dav.server.managers.HarmoniseSearchManager";
    private static String lockmanagerClass = "org.openharmonise.dav.server.managers.HarmoniseLockManager";
    private static String namespacemanagerClass = "org.openharmonise.dav.server.managers.VersionedNamespaceManager";
    private static String propertiesmanagerClass = "org.openharmonise.dav.server.managers.VersionedPropertiesManager";
    private static String authenticatorClass = "org.openharmonise.dav.server.managers.HarmoniseSessionManager";
	private static Hashtable namespaceManagers = new Hashtable();
    private static Hashtable propertiesManagers = new Hashtable();
    private static Hashtable lockManagers = new Hashtable();
    private static Hashtable searchManagers = new Hashtable();
    private static Hashtable authenticators = new Hashtable();
	
    /** Create the lock manager for a resource that is specific to a particular
    * repository manager based on the URL of the resource.
    * @param resource the client resource
    * @param namespaceManager its namespace manager
    * @param propertiesManager its properties manager
    * @return a LockManager for this resource
    * @exception com.ibm.webdav.WebDAVException
    */
    public static LockManager createLockManager(ResourceImpl resource,
                                                NamespaceManager namespaceManager,
                                                PropertiesManager propertiesManager)
                                         throws WebDAVException {
        String repositoryManager = getRepositoryManagerFor(resource);
        boolean usingDefault = false;

        if (repositoryManager == null) {
            repositoryManager = defaultRepository;

            usingDefault = true;
        }

        Class theClass = (Class) lockManagers.get(repositoryManager);

        if (theClass == null) {
            String classname = ResourceImpl.webdavProperties.getProperty(repositoryManager +
                                                                         ".LockManager");

            if (classname == null) {
                if (!usingDefault) {
                    System.err.println(
                            "No LockManager for Repository Manager: " +
                            repositoryManager);
                    System.err.println("Using the fileSystem by default");
                }

                classname = lockmanagerClass;
            }

            try {
                theClass = Class.forName(classname);
            } catch (Exception exc) {
                System.err.println("Cannot create lock manager: " +
                                   classname);
                System.err.println(exc);
            }

            lockManagers.put(repositoryManager, theClass);
        }

        LockManager lockManager = null;

        try {
            lockManager = (LockManager) theClass.newInstance();
            lockManager.initialize(resource, namespaceManager,
                                   propertiesManager);
        } catch (Exception exc) {
            System.err.println("Cannot create lock manager: " +
                               theClass.getName());
            System.err.println(exc);
        }

        return lockManager;
    }

    /** Create the namespace manager for a resource specific to a particular
    * repository manager, based on the resource URL.
    * @param resource the client resource
    * @return a NamespaceManager for this resource
    * @exception com.ibm.webdav.WebDAVException
    */
    public static NamespaceManager createNamespaceManager(ResourceImpl resource)
                                                   throws WebDAVException {
        String repositoryManager = getRepositoryManagerFor(resource);
        boolean usingDefault = false;

        if (repositoryManager == null) {
            repositoryManager = defaultRepository;
            usingDefault = true;
        }

        Class theClass = (Class) namespaceManagers.get(repositoryManager);

        if (theClass == null) {
            String classname = ResourceImpl.webdavProperties.getProperty(repositoryManager +
                                                                         ".NamespaceManager");

            if (classname == null) {
                if (!usingDefault) {
                    System.err.println(
                            "No NamespaceManager for Repository Manager: " +
                            repositoryManager);
                    System.err.println("Using the fileSystem by default");
                }

                classname = namespacemanagerClass;
            }

            try {
                theClass = Class.forName(classname);
            } catch (Exception exc) {
                System.err.println("Cannot create namespace manager: " +
                                   classname);
                System.err.println(exc);
            }

            namespaceManagers.put(repositoryManager, theClass);
        }

        NamespaceManager namespaceManager = null;

        try {
            namespaceManager = (NamespaceManager) theClass.newInstance();
            namespaceManager.initialize(resource);
        } catch (Exception exc) {
            System.err.println("Cannot create namespace manager: " +
                               theClass.getName());
            System.err.println(exc);
        }

        return namespaceManager;
    }

    /** Create the search manager for a resource specific to a particular
    * repository manager, based on the resource URL.
    * @param resource the client resource
    * @return a SearchManager for this resource
    * @exception com.ibm.webdav.WebDAVException
    */
    public static SearchManager createSearchManager(ResourceImpl resource)
                                             throws WebDAVException {
        String repositoryManager = getRepositoryManagerFor(resource);
        boolean usingDefault = false;

        if (repositoryManager == null) {
            repositoryManager = defaultRepository;
            usingDefault = true;
        }

        Class theClass = (Class) searchManagers.get(repositoryManager);

        if (theClass == null) {
            String classname = ResourceImpl.webdavProperties.getProperty(repositoryManager +
                                                                         ".SearchManager");

            if (classname == null) {
                if (!usingDefault) {
                    System.err.println(
                            "No SearchManager for Repository Manager: " +
                            repositoryManager);
                }

                classname = searchmanagerClass;
            }

            try {
                theClass = Class.forName(classname);
            } catch (Exception exc) {
                System.err.println("Cannot create search manager: " +
                                   classname);
                System.err.println(exc);
            }

            searchManagers.put(repositoryManager, theClass);
        }

        SearchManager searchManager = null;

        try {
            searchManager = (SearchManager) theClass.newInstance();
            searchManager.initialize();
        } catch (Exception exc) {
            System.err.println("Cannot create namespace manager: " +
                               theClass.getName());
            System.err.println(exc);
        }

        return searchManager;
    }

    /** Create the properties manager for a resource specific to a particular
    * repository manager, based on the resource URL.
    * @param resource the client resource
    * @param namespaceManager its namespace manager
    * @return a PropertiesManager for this resource
    * @exception com.ibm.webdav.WebDAVException
    */
    public static PropertiesManager createPropertiesManager(ResourceImpl resource,
                                                            NamespaceManager namespaceManager)
                                                     throws WebDAVException {
        String repositoryManager = getRepositoryManagerFor(resource);
        boolean usingDefault = false;

        if (repositoryManager == null) {
            repositoryManager = defaultRepository;
            usingDefault = true;
        }

        Class theClass = (Class) propertiesManagers.get(repositoryManager);

        if (theClass == null) {
            String classname = ResourceImpl.webdavProperties.getProperty(repositoryManager +
                                                                         ".PropertiesManager");

            if (classname == null) {
                if (!usingDefault) {
                    System.err.println(
                            "No PropertiesManager for Repository Manager: " +
                            repositoryManager);
                    System.err.println("Using the fileSystem by default");
                }

                classname = propertiesmanagerClass;
            }

            try {
                theClass = Class.forName(classname);
            } catch (Exception exc) {
                System.err.println("Cannot create properties manager: " +
                                   classname);
                System.err.println(exc);
            }

            propertiesManagers.put(repositoryManager, theClass);
        }

        PropertiesManager propertiesManager = null;

        try {
            propertiesManager = (PropertiesManager) theClass.newInstance();
            propertiesManager.initialize(resource, namespaceManager);
        } catch (Exception exc) {
            System.err.println("Cannot create properties manager: " +
                               theClass.getName());
            System.err.println(exc);
        }

        return propertiesManager;
    }

    public static UserAuthenticator getAuthenticator(ResourceImpl resource)
                                              throws WebDAVException {
        String repositoryManager = getRepositoryManagerFor(resource);
        boolean usingDefault = false;

        if (repositoryManager == null) {
            repositoryManager = defaultRepository;
            usingDefault = true;
        }

        Class theClass = (Class) authenticators.get(repositoryManager);

        if (theClass == null) {
            String classname = ResourceImpl.webdavProperties.getProperty(repositoryManager +
                                                                         ".Authenticator");

            if (classname == null) {
                if (!usingDefault) {
                    System.err.println(
                            "No Authenticator for Repository Manager: " +
                            repositoryManager);
                }

                classname = authenticatorClass;
            }

            try {
                theClass = Class.forName(classname);
            } catch (Exception exc) {
                System.err.println("Cannot create authenticator: " +
                                   classname);
                System.err.println(exc);
            }

            authenticators.put(repositoryManager, theClass);
        }

        UserAuthenticator authenticator = null;

        try {
            authenticator = (UserAuthenticator) theClass.newInstance();
        } catch (Exception exc) {
            System.err.println("Cannot create authenticator: " +
                               theClass.getName());
            System.err.println(exc);
        }

        return authenticator;
    }

    /** Searches the dav4j.properties file for the longest property name
    * matching a prefix of the file part of the resource URL.
    *
    * @parm resource the resource to find a repository manager for
    * @return the value of the located prefix, the name of a repository
    * manager to use for this resource, or null if no matching prefix
    * was found.
    * @exception com.ibm.webdav.WebDAVException thrown if the resource is not accessible
    */
    private static String getRepositoryManagerFor(ResourceImpl resource)
                                           throws WebDAVException {
        String file = null;
        file = resource.getURL().getFile();

        String matchingPrefix = new String();

        // find the longest property name that matches a prefix of the file part of the url
        Enumeration propertyNames = ResourceImpl.webdavProperties.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();

            if (file.startsWith(propertyName) &&
                    (propertyName.length() > matchingPrefix.length())) {
                matchingPrefix = propertyName;
            }
        }

        String repositoryManager = ResourceImpl.webdavProperties.getProperty(
                                           matchingPrefix);

        return repositoryManager;
    }
}