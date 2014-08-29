Flywheel
=================
The missed Flywheel widget for Android!

It is highly customizable.

Document will be updated soon (with the help of God)

Usage
=================
**1)** Add this line to your `build.gradle` file inside your app project folder:

```groovy
dependencies {
    compile 'info.semsamot:flywheel:1.+@aar'
}
```

**2)** Add `Flywheel` widget anywhere you want in your xml as:

```xml
<info.semsamot.flywheel.Flywheel
  android:layout_width="200dp"
  android:layout_height="200dp">
```

**3)** To add your items:

```java
yourFlywheel.addItem("item1");
```

**4)** To retrieve selected item:
```java
yourFlywheel.getSelectedItem();
```
Or listen for the event:
```java
yourFlywheel.setOnAutoCenterListener(new Flywheel.OnAutoCenterListener() {
    @Override
    public void onAutoCenter(Item itemAtCenter) {
        // do stuff here
    }
});
```

License
=================
```
Copyright 2014 semsamot

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
