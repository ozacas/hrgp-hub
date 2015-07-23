/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.interfaces;

import java.io.InputStream;

/**
 *
 * @author acassin
 */
public interface StreamResourceLoaderCallback {
    
    /**
     * Looks up the specified file in the app (ie. war file) and return an input stream to it.
     * 
     * @param key
     * @return null if the stream cannot be loaded 
     */
    public InputStream resolve(final String key);
    
}
