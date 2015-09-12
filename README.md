## Android Five Stars Library

Android Five Stars Library is a small library that helps developers add a **"Rate My App"** dialog to their applications.

It's called "Five Stars" because the dialog has a different behaviour based on the rating given by the user.

If the user gives **4 or 5 stars out of 5**, the user is sent to the *Google Play Store* page to give an actual rating.

If the user gives **3 or less stars out of 5**, the user is asked to *send a bug report* to the developer.

## Preview


![Preview](fivestarslibrary/src/main/res/drawable/screen.png=250?raw=true )




## Installation

To use the library, first include it your project using Gradle


    allprojects {
        repositories {
            jcenter()
            maven { url "https://jitpack.io" }
        }
    }

	dependencies {
	        compile 'com.github.Angtrim:Android-Five-Stars-Library:1.0'
	}



## How to use
To use this library just add this snippet in the `onCreate` of your activity.

The `showAfter(int numbersOfAccess)` method tells the library after how many access the dialog has to be shown.

You can use `show()` to show the dialog at the first access.

```java
new FiveStarsDialog(this,"your@email.com").showAfter(5);
```
You can also customize the **buttons color**, the **rate dialog text** (default is empty), the **support dialog text** using the different setters.

Example:
```java
new FiveStarsDialog(this,"your@email.com").setRateText("my rate text").showAfter(5);
```
## Features

The library is very simple, just note that :
* When the user tap OK or NEVER the dialog will not show again
* When the user tap NOT NOW the dialog will show after `0 - numberOfAccess*2` times

## Used by

If you use my library, please tell me at angelo.gallarello [at] gmail [dot] com.
So I can add your app here!


## License

Do what you want with this library.

However this library uses [this library](https://github.com/afollestad/material-dialogs) so please check its license.