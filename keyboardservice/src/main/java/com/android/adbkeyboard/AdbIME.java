package com.android.adbkeyboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

public class AdbIME extends InputMethodService {
	private String IME_MESSAGE = "ADB_INPUT_TEXT";
	private String IME_CHARS = "ADB_INPUT_CHARS";
	private String IME_KEYCODE = "ADB_INPUT_CODE";
	private String IME_META_KEYCODE = "ADB_INPUT_MCODE";
	private String IME_EDITORCODE = "ADB_EDITOR_CODE";
	private String IME_MESSAGE_B64 = "ADB_INPUT_B64";
	private String IME_CLEAR_TEXT = "ADB_CLEAR_TEXT";
	private String IME_SETTINGS = "ADB_SETTINGS";
	private static final String GLOBAL_SETTING_SHOW_INPUT_VIEW = "adbkeyboard_show_input_view";
	private BroadcastReceiver mReceiver = null;
	private View mInputView = null;

	private boolean shouldShowInputView() {
		// Check both Global Settings and SharedPreferences
		// Priority: Global Settings (if set) > SharedPreferences > default (false)
		boolean fromGlobal = false;
		boolean fromPrefs = false;
		
		// First check Global Settings
		try {
			int value = Settings.Global.getInt(getContentResolver(), GLOBAL_SETTING_SHOW_INPUT_VIEW, -1);
			if (value != -1) {
				fromGlobal = (value == 1);
				return fromGlobal;
			}
		} catch (Exception e) {
			// Ignore, will fallback to SharedPreferences
		}
		
		// Fallback to SharedPreferences if Global Settings not set
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		fromPrefs = prefs.getBoolean("show_input_view", false);
		return fromPrefs;
	}

	@Override
	public View onCreateInputView() {
		// Always create the view, but we'll control its visibility in onStartInputView()
		if (mInputView == null) {
			mInputView = getLayoutInflater().inflate(R.layout.view, null);
		}
		return mInputView;
	}

	@Override
	public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
		super.onStartInputView(info, restarting);
		
		// Update input view visibility based on current setting
		// This ensures the view is updated when settings change
		boolean showInputView = shouldShowInputView();
		
		if (mInputView != null) {
			// Control visibility based on setting
			mInputView.setVisibility(showInputView ? View.VISIBLE : View.GONE);
		}
		
		// Register BroadcastReceiver when input view starts
		if (mReceiver == null) {
			IntentFilter filter = new IntentFilter(IME_MESSAGE);
			filter.addAction(IME_CHARS);
			filter.addAction(IME_KEYCODE);
			filter.addAction(IME_MESSAGE); // IME_META_KEYCODE // Change IME_MESSAGE to get more values.
			filter.addAction(IME_EDITORCODE);
			filter.addAction(IME_MESSAGE_B64);
			filter.addAction(IME_CLEAR_TEXT);
			filter.addAction(IME_SETTINGS);
			mReceiver = new AdbReceiver();
			registerReceiver(mReceiver, filter);
		}
	}

	public void onDestroy() {
		if (mReceiver != null)
			unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	class AdbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(IME_MESSAGE)) {
				// normal message
				String msg = intent.getStringExtra("msg");
				if (msg != null) {
					InputConnection ic = getCurrentInputConnection();
					if (ic != null)
						ic.commitText(msg, 1);
				}
				// meta codes
				String metaCodes = intent.getStringExtra("mcode"); // Get message.
				if (metaCodes != null) {
					String[] mcodes = metaCodes.split(","); // Get mcodes in string.
					if (mcodes != null) {
						int i;
						InputConnection ic = getCurrentInputConnection();
						for (i = 0; i < mcodes.length - 1; i = i + 2) {
							if (ic != null) {
								KeyEvent ke;
								if (mcodes[i].contains("+")) { // Check metaState if more than one. Use '+' as delimiter
									String[] arrCode = mcodes[i].split("\\+"); // Get metaState if more than one.
									ke = new KeyEvent(
											0,
											0,
											KeyEvent.ACTION_DOWN, // Action code.
											Integer.parseInt(mcodes[i + 1].toString()), // Key code.
											0, // Repeat. // -1
											Integer.parseInt(arrCode[0].toString()) | Integer.parseInt(arrCode[1].toString()), // Flag
											0, // The device ID that generated the key event.
											0, // Raw device scan code of the event.
											KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE, // The flags for this key event.
											InputDevice.SOURCE_KEYBOARD // The input source such as SOURCE_KEYBOARD.
									);
								} else { // Only one metaState.
									ke = new KeyEvent(
											0,
											0,
											KeyEvent.ACTION_DOWN, // Action code.
											Integer.parseInt(mcodes[i + 1].toString()), // Key code.
											0, // Repeat.
											Integer.parseInt(mcodes[i].toString()), // Flag
											0, // The device ID that generated the key event.
											0, // Raw device scan code of the event.
											KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE, // The flags for this key event.
											InputDevice.SOURCE_KEYBOARD // The input source such as SOURCE_KEYBOARD.
									);
								}
								ic.sendKeyEvent(ke);
							}
						}
					}
				}
			}

			if (intent.getAction().equals(IME_MESSAGE_B64)) {
				String data = intent.getStringExtra("msg");

				byte[] b64 = Base64.decode(data, Base64.DEFAULT);
				String msg = "NOT SUPPORTED";
				try {
					msg = new String(b64, "UTF-8");
				} catch (Exception e) {

				}

				if (msg != null) {
					InputConnection ic = getCurrentInputConnection();
					if (ic != null)
						ic.commitText(msg, 1);
				}
			}

			if (intent.getAction().equals(IME_CHARS)) {
				int[] chars = intent.getIntArrayExtra("chars");
				if (chars != null) {
					String msg = new String(chars, 0, chars.length);
					InputConnection ic = getCurrentInputConnection();
					if (ic != null)
						ic.commitText(msg, 1);
				}
			}

			if (intent.getAction().equals(IME_KEYCODE)) {
				int code = intent.getIntExtra("code", -1);
				if (code != -1) {
					InputConnection ic = getCurrentInputConnection();
					if (ic != null)
						ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
				}
			}

			if (intent.getAction().equals(IME_EDITORCODE)) {
				int code = intent.getIntExtra("code", -1);
				if (code != -1) {
					InputConnection ic = getCurrentInputConnection();
					if (ic != null)
						ic.performEditorAction(code);
				}
			}

			if (intent.getAction().equals(IME_CLEAR_TEXT)) {
				InputConnection ic = getCurrentInputConnection();
				if (ic != null) {
					//REF: stackoverflow/33082004 author: Maxime Epain
					CharSequence curPos = ic.getExtractedText(new ExtractedTextRequest(), 0).text;
					CharSequence beforePos = ic.getTextBeforeCursor(curPos.length(), 0);
					CharSequence afterPos = ic.getTextAfterCursor(curPos.length(), 0);
					ic.deleteSurroundingText(beforePos.length(), afterPos.length());
				}
			}

			if (intent.getAction().equals(IME_SETTINGS)) {
				// Handle settings via ADB broadcast (deprecated, use adb shell settings set global instead)
				String setting = intent.getStringExtra("setting");
				if (setting != null && setting.equals("show_input_view")) {
					boolean value = intent.getBooleanExtra("value", false);
					try {
						// Try to write to Global Settings (requires system permission)
						Settings.Global.putInt(context.getContentResolver(), GLOBAL_SETTING_SHOW_INPUT_VIEW, value ? 1 : 0);
					} catch (SecurityException e) {
						// Fallback to SharedPreferences if no permission
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
						prefs.edit().putBoolean("show_input_view", value).apply();
					}
				}
			}
		}
	}
}
