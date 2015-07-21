/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.File;

/**
 * The sole purpose of this class is to return HTML descriptions for particular folders within the download page,
 * which aids the user to navigate such a large set of data files.
 * 
 * @author acassin
 */
public class FolderDescription {
    
    /**
     * Returns a HTML fragment describing the data within the specified folder. The purpose of this
     * is to enable the reviewer/researcher to understand what each plot/dataset signifies and its purpose. The only
     * public member in this class. so that caller does not know how this description is obtained.
     * 
     * @param folder the folder identifying what the description returned should relate to
     * @return never null, but may be empty in the case of a folder with no description
     * 
     */
    public static final String get(final File folder) {
        assert(folder != null);
        return "";
    }
}
