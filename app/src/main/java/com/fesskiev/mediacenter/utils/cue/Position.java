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

/**
 * Simple representation for a position field in a cue sheet.
 *
 * @author jwbroek
 */
public class Position {

    /**
     * The number of minutes in this position. Must be >= 0. Should be < 60.
     */
    private int minutes = 0;
    /**
     * The number of seconds in this position. Must be >= 0. Should be < 60.
     */
    private int seconds = 0;
    /**
     * The number of frames in this position. Must be >= 0. Should be < 75.
     */
    private int frames = 0;

    /**
     * Create a new Position.
     */
    public Position() {

    }

    /**
     * Create a new Position.
     *
     * @param minutes The number of minutes in this position. Must be >= 0. Should be < 60.
     * @param seconds The number of seconds in this position. Must be >= 0. Should be < 60.
     * @param frames  The number of frames in this position. Must be >= 0. Should be < 75.
     */
    public Position(final int minutes, final int seconds, final int frames) {
        this.minutes = minutes;
        this.seconds = seconds;
        this.frames = frames;
    }

    /**
     * Get the total number of frames represented by this position. This is equal to
     * frames + (75 * (seconds + 60 * minutes)).
     *
     * @return The total number of frames represented by this position.
     */
    public int getTotalFrames() {
        int result = frames + (75 * (seconds + 60 * minutes));
        return result;
    }

    /**
     * Get the number of frames in this position. Must be >= 0. Should be < 75.
     *
     * @return The number of frames in this position. Must be >= 0. Should be < 75.
     */
    public int getFrames() {
        return this.frames;
    }

    /**
     * Set the number of frames in this position. Must be >= 0. Should be < 75.
     *
     * @param frames The number of frames in this position. Must be >= 0. Should be < 75.
     */
    public void setFrames(final int frames) {
        this.frames = frames;
    }

    /**
     * Get the number of minutes in this position. Must be >= 0. Should be < 60.
     *
     * @return The number of minutes in this position. Must be >= 0. Should be < 60.
     */
    public int getMinutes() {
        return this.minutes;
    }

    /**
     * Set the number of minutes in this position. Must be >= 0. Should be < 60.
     *
     * @param minutes The number of minutes in this position. Must be >= 0. Should be < 60.
     */
    public void setMinutes(final int minutes) {
        this.minutes = minutes;
    }

    /**
     * Get the number of seconds in this position. Must be >= 0. Should be < 60.
     *
     * @return The seconds of seconds in this position. Must be >= 0. Should be < 60.
     */
    public int getSeconds() {
        return this.seconds;
    }

    /**
     * Set the number of seconds in this position. Must be >= 0. Should be < 60.
     *
     * @param seconds The number of seconds in this position. Must be >= 0. Should be < 60.
     */
    public void setSeconds(final int seconds) {
        this.seconds = seconds;
    }
}
