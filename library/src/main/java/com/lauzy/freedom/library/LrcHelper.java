package com.lauzy.freedom.library;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Desc : 歌词解析
 * Author : Lauzy
 * Date : 2017/10/13
 * Blog : http://www.jianshu.com/u/e76853f863a9
 * Email : freedompaladin@gmail.com
 */
public class LrcHelper {

    private static final String CHARSET = "utf-8";
    //[03:56.00][03:18.00][02:06.00][01:07.00]原谅我这一生不羁放纵爱自由
    private static final String LINE_REGEX = "((\\[\\d{2}:\\d{2}\\.\\d{2}])+)(.*)";
    private static final String TIME_REGEX = "\\[(\\d{2}):(\\d{2})\\.(\\d{2})]";

    public static LyricInfo parseLrcFromAssets(Context context, String fileName) {
        try {
            return parseInputStream(context.getResources().getAssets().open(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LyricInfo parseLrcFromFile(File file) {
        try {
            return parseInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static LyricInfo parseLrcFromString(@NonNull String data) {
        try {
            return parseInputStream(new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8"))));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static LyricInfo parseInputStream(InputStream inputStream) {
        LyricInfo lyricInfo = new LyricInfo();
        List<Lrc> lrcs = new ArrayList<>();
        lyricInfo.songLines = lrcs;

        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            isr = new InputStreamReader(inputStream, CHARSET);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                int index = line.lastIndexOf("]");
                if (line.startsWith("[offset:")) {
                    // time offset
                    lyricInfo.songOffset = Long.parseLong(line.substring(8, index).trim());
                }
                else if (line.startsWith("[length:")) {
                    // length
                    String lengthString = line.substring(8, index).trim();
                    String[] values = lengthString.split(":");
                    if (values.length == 2){
                        int minute = Integer.parseInt(values[0]);
                        float second = Float.parseFloat(values[1]);
                        lyricInfo.length = (long) ((minute * 60 + second) * 1000);
                    }
                }
                else if (line.startsWith("[ti:")) {
                    // title
                    lyricInfo.songTitle = line.substring(4, index).trim();
                }
                else if (line.startsWith("[ar:")) {
                    // artist
                    lyricInfo.songArtist = line.substring(4, index).trim();
                }
                else if (line.startsWith("[al:")) {
                    // album
                    lyricInfo.songAlbum = line.substring(4, index).trim();
                }
                else if (line.startsWith("[au:")) {
                    // Creator of the Songtext
                    lyricInfo.author = line.substring(4, index).trim();
                }
                else if (line.startsWith("[re:")) {
                    // editor that created the LRC file
                    lyricInfo.re = line.substring(4, index).trim();
                }
                else if (line.startsWith("[ve:")) {
                    // version of program
                    lyricInfo.version = line.substring(4, index).trim();
                }
                else if (line.startsWith("[by:")) {
                    // Creator of the LRC file
                    lyricInfo.by = line.substring(4, index).trim();
                }
                else {
                    List<Lrc> lrcList = parseLrc(line);
                    if (lrcList != null && lrcList.size() != 0) {
                        lrcs.addAll(lrcList);
                    }
                }
            }
            sortLrcs(lrcs);
            return lyricInfo;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return lyricInfo;
    }

    private static void sortLrcs(List<Lrc> lrcs) {
        Collections.sort(lrcs, new Comparator<Lrc>() {
            @Override
            public int compare(Lrc o1, Lrc o2) {
                return (int) (o1.getTime() - o2.getTime());
            }
        });
    }

    private static List<Lrc> parseLrc(String lrcLine) {
        if (lrcLine.trim().isEmpty()) {
            return null;
        }
        List<Lrc> lrcs = new ArrayList<>();
        Matcher matcher = Pattern.compile(LINE_REGEX).matcher(lrcLine);
        if (!matcher.matches()) {
            return null;
        }

        String time = matcher.group(1);
        String content = matcher.group(3);
        Matcher timeMatcher = Pattern.compile(TIME_REGEX).matcher(time);

        while (timeMatcher.find()) {
            Lrc lrc = new Lrc();
            if (content != null && content.length() != 0) {
                lrc.setTime(parseTime(timeMatcher));
                lrc.setText(content);
                lrcs.add(lrc);
            }
        }
        return lrcs;
    }

    public static long parseTime(Matcher timeMatcher) {
        String min = timeMatcher.group(1);
        String sec = timeMatcher.group(2);
        String mil = timeMatcher.group(3);
        return Long.parseLong(min) * 60 * 1000 + Long.parseLong(sec) * 1000
                + Long.parseLong(mil) * 10;
    }

    public static String formatTime(long time) {
        int min = (int) (time / 60000);
        int sec = (int) (time / 1000 % 60);
        int millis = (int) (time - (min * 60 + sec) * 1000) / 10;
        return adjustFormat(min) + ":" + adjustFormat(sec) + "." + adjustFormat(millis);
    }

    private static String adjustFormat(int time) {
        if (time < 10) {
            return "0" + time;
        }
        return time + "";
    }
}