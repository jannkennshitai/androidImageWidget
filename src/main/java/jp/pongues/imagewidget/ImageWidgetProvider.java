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

import static jp.pongues.imagewidget.Settings.COLUMN_IMAGE_URI;
import static jp.pongues.imagewidget.Settings.COLUMN_WIDGET_ID;
import static jp.pongues.imagewidget.Settings.TABLE_NAME;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import java.util.List;

public class ImageWidgetProvider extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		if(Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)){
			onEnabled(context);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		SQLiteDatabase db = Settings.getDatabase(context);
		for(int widgetId: appWidgetIds){
			db.delete(TABLE_NAME, COLUMN_WIDGET_ID + " = " + widgetId, null);
		}
		db.close();
	}

	@Override
	public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
		SQLiteDatabase db = Settings.getDatabase(context);
		int count = oldWidgetIds.length;
		for(int i=0; i<count; i++){
			ContentValues values = new ContentValues();
			values.put(COLUMN_WIDGET_ID, newWidgetIds[i]);
			db.update(TABLE_NAME, values, COLUMN_WIDGET_ID + " = " + oldWidgetIds[i], null);
		}
		db.close();
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		SQLiteDatabase db = Settings.getDatabase(context);
		for(int widgetId: appWidgetIds){
			Uri imageUri = getImageUri(db, widgetId);
			updateWidget(context, appWidgetManager, widgetId, imageUri);
		}
		db.close();
	}

	@Override
	public void onEnabled(Context context) {
		grantUriReadUris(context);
	}

	@Nullable
	public static Uri getImageUri(SQLiteDatabase db, int widgetId){
		String[] projection = { Settings.COLUMN_IMAGE_URI};
		Cursor cursor = db.query(
				TABLE_NAME, projection, COLUMN_WIDGET_ID + " = " + widgetId, null,
				null, null, null
		);
		String str = null;
		if(cursor != null){
			if(cursor.moveToFirst()){
				int index = cursor.getColumnIndex(Settings.COLUMN_IMAGE_URI);
				if(index >= 0){
					str = cursor.getString(index);
				}
			}
			cursor.close();
		}
		return str == null ? null : Uri.parse(str);
	}

	public static void updateWidget(Context context, AppWidgetManager manager, int widgetId, Uri imageUri){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(imageUri, "image/*");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_IMMUTABLE);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_image);
		views.setOnClickPendingIntent(R.id.image, pendingIntent);
		views.setImageViewUri(R.id.image, imageUri);

		manager.updateAppWidget(widgetId, views);
	}

	public static void grantUriReadUris(Context context){
		SQLiteDatabase database = Settings.getDatabase(context);
		Cursor cursor = database.query(
				TABLE_NAME, new String[]{COLUMN_IMAGE_URI}, null, null,
				null, null, null);

		if(cursor != null){
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			List<ResolveInfo> resolves = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

			int index = cursor.getColumnIndex(COLUMN_IMAGE_URI);
			if(index >= 0){
				while(cursor.moveToNext()){
					String string = cursor.getString(index);
					if(string != null){
						Uri uri = Uri.parse(string);
						for(ResolveInfo resolve: resolves){
							String packageName = resolve.activityInfo.packageName;
							context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
						}

					}
				}
			}
			cursor.close();
		}
	}

	public static void grantUriReadUri(Context context, Uri uri){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolves = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		for(ResolveInfo resolve: resolves){
			String packageName = resolve.activityInfo.packageName;
			context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
	}
}
