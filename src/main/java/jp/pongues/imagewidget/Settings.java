/*
 * Copyright (c) 2022 Hiroaki Sano
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package jp.pongues.imagewidget;

import static android.database.sqlite.SQLiteDatabase.CREATE_IF_NECESSARY;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Arrays;

public class Settings {

	public static final String IMAGE_DIR = "images";

	public static final String DATABASE_NAME = "settings.db";
	public static final int VER = 1;

	public static final String TABLE_NAME = "entry";

	public static final String COLUMN_ID = "id"; // integer
	public static final String COLUMN_WIDGET_ID = "widget_id"; // integer
	public static final String COLUMN_IMAGE_URI = "image"; // text

	public static SQLiteDatabase getDatabase(Context context){
		File file = new File(context.getFilesDir(), DATABASE_NAME);
		SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null, CREATE_IF_NECESSARY);
		int ver = db.getVersion();
		if(ver != VER){
			db.setVersion(VER);
			db.execSQL(
					"create table " + TABLE_NAME + "( "
					+ COLUMN_ID + " INTEGER PRIMARY KEY, "
					+ COLUMN_WIDGET_ID + " INTEGER, "
					+ COLUMN_IMAGE_URI + " TEXT "
					+ ")"
			);
		}
		return db;
	}

	@MainThread
	public static @Nullable File createImageFile(Context context){
		File dir = new File(context.getFilesDir(), IMAGE_DIR);
		if(!dir.exists() && !dir.mkdir()) return null;
		String[] names = dir.list();
		if(names == null) return null;
		Arrays.sort(names, Settings::compare);
		int f = 0;
		for(String name: names){
			int x = getInt(name);
			if(x != f + 1) break;
			f = x;
		}
		return new File(dir, String.valueOf(f + 1));
	}

	private static int compare(String o1, String o2){
		int c = Integer.compare(o1.length(), o2.length());
		if(c != 0) return c;
		return o1.compareTo(o2);
	}

	private static int getInt(String str){
		int x = 0;
		for(int i=0, l=str.length(); i<l; i++){
			char c = str.charAt(i);
			if(!Character.isDigit(c)) return -1;
			x = x * 10 + c - '0';
		}
		return x;
	}

	public static Uri getImageUri(Context context, File file) {
		return FileProvider.getUriForFile(context, context.getPackageName(), file);
	}
}
