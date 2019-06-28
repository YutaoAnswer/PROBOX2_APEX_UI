package com.netxeon.newprobox2.utils;

import android.util.Log;

public class Logger {
	public static final String TAG_FOCUS = "focus";
	public static final boolean DEBUG_FOCUS = false;
	public static final String TAG_ADD = "add";
	public static final boolean DEBUG_ADD = false;
	public static final String TAG_CLICK = "click";
	public static final boolean DEBUG_CLICK = false;
	public static final String TAG_PACKAGE = "package";
	public static final boolean DEBUG_PACKAGECHANGED = false;
	public static final String TAG_COPY = "copy";
	public static final boolean DEBUG_COPY = false;
	public static final String TAG_SHORTCUT = "shortcut";
	public static final boolean DEBUG_SHORTCUT = false;
	public static final String TAG_WEATHER = "weather";
	public static final boolean DEBUG_WEATHER = true;
	public static final String TAG_MEMORY = "memory";
	public static final boolean DEBUG_MEMORY = false;
	
	public static void log(String tag, String info){
		if(tag.equals(TAG_FOCUS) && DEBUG_FOCUS){
			Log.v(TAG_FOCUS, info);
		} else if(tag.equals(TAG_ADD) && DEBUG_ADD){
			Log.v(TAG_ADD, info);
		} else if(tag.equals(TAG_CLICK) && DEBUG_CLICK){
			Log.v(TAG_CLICK, info);
		} else if(tag.equals(TAG_PACKAGE) && DEBUG_PACKAGECHANGED){
			Log.v(TAG_PACKAGE, info);
		} else if(tag.equals(TAG_COPY) && DEBUG_COPY){
			Log.v(TAG_COPY, info);
		} else if(tag.equals(TAG_SHORTCUT) && DEBUG_SHORTCUT){
			Log.v(TAG_SHORTCUT, info);
		} else if(tag.equals(TAG_WEATHER) && DEBUG_WEATHER){
			Log.v(TAG_WEATHER, info);
		} else if(tag.equals(TAG_MEMORY) && DEBUG_MEMORY){
			Log.v(TAG_MEMORY, info);
		}
	}
}
