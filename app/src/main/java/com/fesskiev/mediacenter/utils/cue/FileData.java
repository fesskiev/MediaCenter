/*
 * Cuelib library for manipulating cue sheets.
 * Copyright (C) 2007-2008 Jan-Willem van den Broek
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.fesskiev.mediacenter.utils.cue;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple representation of a FILE block in a cue sheet.
 *
 * @author jwbroek
 */
public class FileData {
    /**
     * The track data for this file data.
     */
    private final List<TrackData> trackData = new ArrayList<TrackData>();
    /**
     * The file for this file data. May be null, though this is not compliant.
     */
    private String file = null;
    /**
     * The file type for this file data. May be null, or any string value, though this is not necessarily compliant.
     */
    private String fileType = null;
    /**
     * The CueSheet that this FileData belongs to.
     */
    private CueSheet parent;

    /**
     * Create a new FileData instance.
     *
     * @param parent The CueSheet that this FileData is associated with.
     */
    public FileData(final CueSheet parent) {
        this.parent = parent;
    }

    /**
     * Create a new FileData instance.
     *
     * @param parent   The CueSheet that this FileData is associated with.
     * @param file     The file that this FileData applies to. May be null, though this is not compliant.
     * @param fileType The file type for this FileData. May be null, or any string value, though this is not necessarily
     *                 compliant.
     */
    public FileData(final CueSheet parent, final String file, final String fileType) {
        this.parent = parent;
        this.file = file;
        this.fileType = fileType;
    }

    /**
     * Get all indices of all tracks that belong to this file data.
     *
     * @return All indices of all tracks that belong to this file data.
     */
    public List<Index> getAllIndices() {
        List<Index> allIndices = new ArrayList<Index>();

        for (TrackData trackData : this.trackData) {
            allIndices.addAll(trackData.getIndices());
        }

        return allIndices;
    }

    /**
     * Get the file that this FileData applies to. May be null, though this is not compliant.
     *
     * @return The file that this FileData applies to. May be null, though this is not compliant.
     */
    public String getFile() {
        return this.file;
    }

    /**
     * Set the file that this FileData applies to. May be null, though this is not compliant.
     *
     * @param file The file that this FileData applies to. May be null, though this is not compliant.
     */
    public void setFile(final String file) {
        this.file = file;
    }

    /**
     * Get the file type for this FileData. May be null, or any string value, though this is not necessarily
     * compliant.
     *
     * @return The file type for this FileData. May be null, or any string value, though this is not necessarily
     * compliant.
     */
    public String getFileType() {
        return this.fileType;
    }

    /**
     * Set the file type for this FileData. May be null, or any string value, though this is not necessarily
     * compliant.
     *
     * @param fileType The file type for this FileData. May be null, or any string value, though this is not necessarily
     *                 compliant.
     */
    public void setFileType(final String fileType) {
        this.fileType = fileType;
    }

    /**
     * Get the track data for this file data.
     *
     * @return The track data for this file data.
     */
    public List<TrackData> getTrackData() {
        return this.trackData;
    }

    /**
     * Get the CueSheet that this FileData belongs to.
     *
     * @return The CueSheet that this FileData belongs to.
     */
    public CueSheet getParent() {
        return this.parent;
    }

    /**
     * Set the CueSheet that this FileData belongs to.
     *
     * @param parent The CueSheet that this FileData belongs to.
     */
    public void setParent(final CueSheet parent) {
        this.parent = parent;
    }
}
