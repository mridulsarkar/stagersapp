package com.poc.stagers;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Hashtable;

public class ResourceManager
{
    private static Hashtable<String, ResourceManager> bundles;
    private boolean debug;
    private String name;
    private ResourceBundle rsrc;
    
    public static void initialize() {
        ResourceManager.bundles = new Hashtable<String, ResourceManager>();
    }
    
    public static void load(final String id, final String name) {
        try {
            if (null == id || id.isEmpty()) {
                throw new NullPointerException("Resource Bundle Id is Blank");
            }
            final ResourceBundle rsrc = ResourceBundle.getBundle(name, Locale.US);
            final ResourceManager rMgr = new ResourceManager(name, rsrc);
            ResourceManager.bundles.put(id, rMgr);
        }
        catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void load(final String id, final String name, final Locale locale) {
        try {
            if (null == id || id.isEmpty()) {
                throw new NullPointerException("Resource Bundle Id is Blank");
            }
            final ResourceBundle rsrc = ResourceBundle.getBundle(name, locale);
            final ResourceManager rMgr = new ResourceManager(name, rsrc);
            ResourceManager.bundles.put(id, rMgr);
        }
        catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }
    
    public static ResourceManager get(final String id) {
        return ResourceManager.bundles.get(id);
    }
    
    public static ResourceManager put(final String id, final ResourceManager rMgr) {
        return ResourceManager.bundles.put(id, rMgr);
    }
    
    private ResourceManager() {
        
    }
    
    public ResourceManager(final String name, final ResourceBundle rsrc) throws MissingResourceException {
        this.name = name;
        this.rsrc = rsrc;
    }
    
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }
    
    public String getString(final String key) {
        return this.rsrc.getString(key);
    }
    
    public String getFormat(final String key, final Object[] args) {
        return MessageFormat.format(this.rsrc.getString(key), args);
    }
}