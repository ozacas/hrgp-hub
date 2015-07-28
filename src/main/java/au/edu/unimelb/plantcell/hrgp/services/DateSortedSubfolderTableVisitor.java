/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services;

import au.edu.unimelb.plantcell.hrgp.interfaces.StreamResourceLoaderCallback;
import java.io.File;
import java.util.Comparator;

/**
 *
 * @author acassin
 */
public class DateSortedSubfolderTableVisitor extends SubfolderTableVisitor {
    public DateSortedSubfolderTableVisitor() {
        super();
    }
    
    public DateSortedSubfolderTableVisitor(final StreamResourceLoaderCallback cb, File f) {
        super(cb, f);
    }
    
    public DateSortedSubfolderTableVisitor(final StreamResourceLoaderCallback cb, String prefix) {
        super(cb, prefix);
    }
    
    /**
     * Returns a comparator which uses the last modified data to ensure the most recent folder
     * is at the top of the page.
     * 
     * @return 
     */
    @Override
    public Comparator<File> getDirectoryComparator() {
        return new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                // newest folders at top of traversal
                long date1 = o1.lastModified();
                long date2 = o2.lastModified();
                if (date1 < date2) {
                    return 1;
                } else if (date2 < date1) {
                    return -1;
                } else {
                    return 0;
                }
            }
            
        };
    }
}
