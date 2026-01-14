# ADBKeyBoard

Android Virtual Keyboard Input via ADB

ADBKeyBoard is a virtual keyboard that receives commands from system broadcast intents, which you can send text input using adb.

There is a shell command 'input', which can help you send text input to the Android system.

<pre>
usage: input [text|keyevent]
  input text <string>
  input keyevent <event_code>
</pre>

But you cannot send unicode characters using this command, as it is not designed to use it this way.
<br />
Reference : http://stackoverflow.com/questions/14224549/adb-shell-input-unicode-character

<pre>
e.g.
adb shell input text '你好嗎' 
is not going to work.
</pre>

ADBKeyboard will help in these cases, especially in device automation and testings.

**Features:**

- Send text input via ADB broadcast intents
- Support Unicode characters (via base64 encoding)
- Support key events and editor actions
- Configurable input view visibility (show/hide status bar)
- Settings interface accessible from app launcher or system settings

## Download APK from release page

- APK download: [https://github.com/senzhk/ADBKeyBoard/blob/master/ADBKeyboard.apk]

## Build and Install APK

### Prerequisites

- Android SDK installed
- Java JDK 17 or higher (Java 17 is recommended for Android development)
- Gradle (included via wrapper)

### Build Steps

1. **Get the source code:**

   ```bash
   git clone https://github.com/senzhk/ADBKeyBoard.git
   cd ADBKeyBoard
   ```

2. **Set Android SDK location:**

   Option 1: Set environment variable

   ```bash
   export ANDROID_HOME=$HOME/Android/Sdk
   ```

   Option 2: Create `local.properties` file in project root:

   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   ```

3. **Build the project:**

   Build Debug APK:

   ```bash
   ./gradlew assembleDebug
   ```

   Build Release APK (signed with debug keystore):

   ```bash
   ./gradlew assembleRelease
   ```

   The APK files will be generated at:

   - Debug: `keyboardservice/build/outputs/apk/debug/keyboardservice-debug.apk`
   - Release: `keyboardservice/build/outputs/apk/release/keyboardservice-release.apk`

### Install Steps

1. **Connect your device or start emulator:**

   ```bash
   adb devices
   ```

2. **Install the APK:**

   For Debug APK:

   ```bash
   adb install keyboardservice/build/outputs/apk/debug/keyboardservice-debug.apk
   ```

   For Release APK:

   ```bash
   adb install keyboardservice/build/outputs/apk/release/keyboardservice-release.apk
   ```

   Or install directly from project root:

   ```bash
   adb install ADBKeyboard.apk
   ```

3. **Enable and set as default input method:**
   ```bash
   adb shell ime enable com.android.adbkeyboard/.AdbIME
   adb shell ime set com.android.adbkeyboard/.AdbIME
   ```

## How to Use

- Enable 'ADBKeyBoard' in the Language&Input Settings OR from adb.

```
adb install ADBKeyboard.apk
adb shell ime enable com.android.adbkeyboard/.AdbIME
adb shell ime set com.android.adbkeyboard/.AdbIME
```

- Set it as Default Keyboard OR Select it as the current input method of certain EditText view.
- Sending Broadcast intent via Adb or your Android Services/Apps.

Usage Example:

<pre>
1. Sending text input
adb shell am broadcast -a ADB_INPUT_TEXT --es msg '你好嗎? Hello?'

* This may not work for Oreo/P, am/adb command seems not accept utf-8 text string anymore

1.1 Sending text input (base64) if (1) is not working.

* For Mac/Linux, you can use the latest base64 input type with base64 command line tool:
adb shell am broadcast -a ADB_INPUT_B64 --es msg `echo -n '你好嗎? Hello?' | base64`

* For Windows, please try this script (provided by ssddi456): 
https://gist.github.com/ssddi456/889d5e8a2571a33e8fcd0ff6f1288291

* Sample python script to send b64 codes (provided by sunshinewithmoonlight):
import os
import base64
chars = '的广告'
charsb64 = str(base64.b64encode(chars.encode('utf-8')))[1:]
os.system("adb shell am broadcast -a ADB_INPUT_B64 --es msg %s" %charsb64)

2. Sending keyevent code  (67 = KEYCODE_DEL)
adb shell am broadcast -a ADB_INPUT_CODE --ei code 67

3. Sending editor action (2 = IME_ACTION_GO)
adb shell am broadcast -a ADB_EDITOR_CODE --ei code 2

4. Sending unicode characters
To send 😸 Cat
adb shell am broadcast -a ADB_INPUT_CHARS --eia chars '128568,32,67,97,116'

5. Send meta keys
To send Ctrl + A as below: (4096 is META_CONTROL_ON, 8192 is META_CONTROL_LEFT_ON, 29 is KEYCODE_A)
adb shell am broadcast -a ADB_INPUT_TEXT --es mcode '4096,29' // one metaState.
or
adb shell am broadcast -a ADB_INPUT_TEXT --es mcode '4096+8192,29' // two metaState.


6. CLEAR all text (starting from v2.0)
adb shell am broadcast -a ADB_CLEAR_TEXT

</pre>

Enable ADBKeyBoard from adb :

<pre>
adb shell ime enable com.android.adbkeyboard/.AdbIME
</pre>

Switch to ADBKeyBoard from adb (by [robertio](https://github.com/robertio)) :

<pre>
adb shell ime set com.android.adbkeyboard/.AdbIME   
</pre>

Switch back to original virtual keyboard: (swype in my case...)

<pre>
adb shell ime set com.nuance.swype.dtc/com.nuance.swype.input.IME  
</pre>

Check your available virtual keyboards:

<pre>
adb shell ime list -a  
</pre>

Reset to default, don't care which keyboard was chosen before switch:

<pre>
adb shell ime reset
</pre>

## Settings Configuration

### Show/Hide Input View

By default, ADB Keyboard runs completely invisible (no status bar displayed). You can control whether to show the input view status bar through Settings or ADB commands.

#### Method 1: Via Settings Activity

You can open the settings activity in several ways:

**Via App Launcher (Recommended):**

1. Open the app drawer on your device
2. Find and tap **"ADBKeyBoard"** app icon
3. The settings activity will open automatically
4. Toggle **"Show Input View"** switch
   - **ON**: Display "ADB Keyboard {ON}" status bar when input method is active
   - **OFF**: Completely hide the input view (default)

**Via ADB command:**

```bash
# Start MainActivity (recommended)
adb shell am start -n com.android.adbkeyboard/.MainActivity

# Or start SettingsActivity directly
adb shell am start -n com.android.adbkeyboard/.SettingsActivity
```

**Via System Settings (if available):**

1. Open **Settings** → **Language & input** → **On-screen keyboard** (or **Virtual keyboard**)
2. Find **"ADB Keyboard"** in the list
3. Tap on the **"ADB Keyboard"** entry (if settings icon is not available)
4. The settings activity will open
5. Toggle **"Show Input View"** switch

**Note:** After changing the setting in the UI, you may need to switch to another input method and back, or restart the input method service for the change to take effect.

#### Method 2: Via ADB Command (Recommended)

Use `adb shell settings put global` to configure the setting:

Show the input view status bar:

```bash
adb shell settings put global adbkeyboard_show_input_view 1
```

Hide the input view (default):

```bash
adb shell settings put global adbkeyboard_show_input_view 0
```

Check current setting:

```bash
adb shell settings get global adbkeyboard_show_input_view
```

**Note:** After changing the setting, you may need to switch to another input method and back, or restart the input method service for the change to take effect.

#### Method 3: Via Broadcast Intent (Deprecated)

You can also use broadcast intent (less recommended):

```bash
# Show input view
adb shell am broadcast -a ADB_SETTINGS --es setting show_input_view --ez value true

# Hide input view
adb shell am broadcast -a ADB_SETTINGS --es setting show_input_view --ez value false
```

You can try the apk with my debug build: https://github.com/senzhk/ADBKeyBoard/raw/master/ADBKeyboard.apk

KeyEvent Code Ref: http://developer.android.com/reference/android/view/KeyEvent.html

Editor Action Code Ref: http://developer.android.com/reference/android/view/inputmethod/EditorInfo.html
