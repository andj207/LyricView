package com.lauzy.freedom.library;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LyricInfo {
    @Nullable
    public List<Lrc> songLines;

    public long songOffset;
    public long length;

    @Nullable
    public String songTitle;

    @Nullable
    public String songArtist;

    @Nullable
    public String songAlbum;

    /**
     * Creator of the Song text
     */
    @Nullable
    public String author;

    /**
     * Creator of the LRC file
     */
    @Nullable
    public String by;

    /**
     * The player or editor that created the LRC file
     */
    @Nullable
    public String re;

    /**
     * version of program
     */
    @Nullable
    public String version;

}
