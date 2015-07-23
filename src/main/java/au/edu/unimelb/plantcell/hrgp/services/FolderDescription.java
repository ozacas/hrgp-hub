/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services;

import au.edu.unimelb.plantcell.hrgp.interfaces.StreamResourceLoaderCallback;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The sole purpose of this class is to return HTML descriptions for particular folders within the download page,
 * which aids the user to navigate such a large set of data files.
 * 
 * @author acassin
 */
public class FolderDescription {
    private final static Logger l = Logger.getLogger("FolderDescription");
    private StreamResourceLoaderCallback cb;
    
    public FolderDescription(final StreamResourceLoaderCallback cb) {
        assert(cb != null);
        this.cb = cb;
    }
    
    /**
     * Returns a HTML fragment describing the data within the specified folder. The purpose of this
     * is to enable the reviewer/researcher to understand what each plot/dataset signifies and its purpose. The only
     * public member in this class. so that caller does not know how this description is obtained.
     * 
     * @param folder the folder identifying what the description returned should relate to
     * @return never null, but may be empty in the case of a folder with no description
     * 
     */
    public final String get(final File folder) {
        String relative = ServiceCore.relativize(folder);
        if (relative == null) {
            l.log(Level.INFO, "Unknown folder ignored: {0}", folder.getAbsolutePath());
            // fallthru
        } else {
            String description = lookup_description(relative);
            if (description == null) {
                l.log(Level.INFO, "No description available for {0}", relative);
                // do not fallthru: report it for fixing by the developer
                return "<h3>"+relative+"</h3>";
            } else {
                return description;
            }
        }
        return "";
    }
    
    private String lookup_description(final String key) {
        assert(key != null && key.length() > 0);
        String final_key = key;
        while (final_key.endsWith("/")) {
            final_key = final_key.substring(0, final_key.length()-1);
        }
        final_key = final_key.replaceAll("/", "_");
        l.log(Level.INFO, "Final key is {0}", final_key);
        InputStream is = cb.resolve("/descriptions/"+final_key);
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader rdr = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = rdr.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                return sb.toString();
            } catch (IOException ioe) {
                l.log(Level.INFO, "Failed to find description for "+key,ioe);
                return null;
            }
        }
        return null;
    }
}
