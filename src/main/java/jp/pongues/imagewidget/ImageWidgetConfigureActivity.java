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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

public class ImageWidgetConfigureActivity extends Activity {

	private int widgetId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_widget_configure);

		widgetId = getWidgetId();
		if(widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();

		findViewById(R.id.button_ok).setOnClickListener((v -> setWidget(true)));
		findViewById(R.id.button_cancel).setOnClickListener((v -> setWidget(false)));
	}

	private int getWidgetId(){
		Intent intent = getIntent();
		if(intent == null) return AppWidgetManager.INVALID_APPWIDGET_ID;
		return intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	private void setWidget(boolean succeeded){
		Intent intent = new Intent();
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		setResult(succeeded ? RESULT_OK : RESULT_CANCELED, intent);
		finish();
	}
}
