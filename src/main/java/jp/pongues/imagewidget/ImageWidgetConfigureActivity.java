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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ImageWidgetConfigureActivity extends Activity {

	private static final int REQUEST_OPEN_FILE = 1;

	private int widgetId;
	private File imageFile;
	private Uri imageUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_widget_configure);

		widgetId = getWidgetId();
		if(widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) cancel();

		findViewById(R.id.button_ok).setOnClickListener((v -> setWidget()));
		findViewById(R.id.button_cancel).setOnClickListener((v -> cancel()));

		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");

		startActivityForResult(intent, REQUEST_OPEN_FILE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_OPEN_FILE){
			if(resultCode == RESULT_OK){
				Uri fromUri = data.getData();
				File toFile = Settings.createImageFile(this);

				try (InputStream inputStream = getContentResolver().openInputStream(fromUri)) {
					if(toFile == null) throw new NullPointerException();
					Files.copy(inputStream, toFile.toPath());
				} catch (NullPointerException|IOException e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), R.string.toast_fail_load_image, Toast.LENGTH_SHORT).show();
					return;
				}

				if(imageFile != null) for(int i=0; i<3; i++) if(imageFile.delete()) break;
				imageFile = toFile;
				imageUri = Settings.getImageUri(this, toFile);
				ImageView imageView = findViewById(R.id.image);
				imageView.setImageURI(imageUri);
			}else{
				cancel();
			}
		}
	}

	private int getWidgetId(){
		Intent intent = getIntent();
		if(intent == null) return AppWidgetManager.INVALID_APPWIDGET_ID;
		return intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	private void cancel(){
		Intent intent = new Intent();
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		setResult(RESULT_CANCELED, intent);
		finish();
	}

	private void setWidget(){
		if(imageUri != null){
			SQLiteDatabase db = Settings.getDatabase(this);
			ContentValues values = new ContentValues();
			values.put(COLUMN_WIDGET_ID, widgetId);
			values.put(COLUMN_IMAGE_URI, imageUri.toString());
			long id = db.insert(TABLE_NAME, null, values);
			db.close();

			if(id != -1){
				AppWidgetManager manager = AppWidgetManager.getInstance(this);
				ImageWidgetProvider.updateWidget(this, manager, widgetId, imageUri);

				ImageWidgetProvider.grantUriReadUri(this, imageUri);

				Intent intent = new Intent();
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	}
}
