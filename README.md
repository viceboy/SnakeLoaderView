# SnakeLoaderView
A simple loader view which draws a moving snake around the borders and on top of image.

## Demo
<p float="left">
  <img src="demo/demo_load.gif" width="250" />
</p>


## Install
```gradle
dependencies {
  implementation 'com.viceboy.reptileloader:snakeloaderview:1.0.0'
}
```


## Usage

### XML

```xml

<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/rootContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/holo_blue_light"
    tools:context=".MainActivity">

    <com.viceboy.reptileloader.SnakeLoaderView
        android:id="@+id/loader"
        android:layout_width="200dp"
        android:layout_height="100dp"
        android:layout_gravity="center"
        android:layout_marginTop="64dp"
        android:elevation="8dp"
        app:bgColor="@android:color/background_dark"
        app:imageScaleType="CENTER"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:loaderColor="@android:color/white"
        app:loaderImage="@drawable/loading"
        app:loaderLength="200dp"
        app:loaderPadding="1dp"
        app:loaderSpeed="30" />

</androidx.constraintlayout.widget.ConstraintLayout>

```

### Kotlin

### Basic Usage
```kotlin

        val root = findViewById<ConstraintLayout>(R.id.rootContainer)
        val loader = findViewById<SnakeLoaderView>(R.id.loader)

        root.setOnClickListener(object : View.OnClickListener {
            private var flag = false
            override fun onClick(p0: View) {
                flag = if (!flag) {
                    loader.startAnimation{ //Callback for onStartLoaderAnimation }         
                    true
                } else {
                    loader.stopAnimation{ //Callback for onStopLoaderAnimation }
                    false
                }
            }
        })
}
```

### Attributes

* **bgColor**, Background color of view.
* **imageScaleType**, set image to fit center or occupy the complete width and height .
* **loaderColor**, set the loader color.
* **loaderImage**, set the background image.
* **loaderLength**, length of loader.
* **loaderSpeed**, speed at which loader moves around the borders.
* **loaderWidth**, width of loader.
* **loaderPadding**, left and top padding from borders.



## License

```
   Copyright 2020 Sumit Jha

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```