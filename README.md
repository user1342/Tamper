<p align="center">
    <img width=100% src="/Images/banner.png">
  </a>
</p>
<p align="center"> 📱 An Android application for idenitfying device state. 🕵️</p>

<br>

# Tamper

<img align="right" width="150" height="150" src="Images\icon.png">
Tamper is a short and simple Tamper-Detection application for Android. Built from concepts used in the Google Play Safety Net Attestation API, Tamper displays a series of system information, including:

 - Device State 📱
 - System Interity 📡
 - SD Card Tamperd State 💾

## What is Google Play Safety Net
[Google Play Safety Net](https://developer.android.com/training/safetynet/attestation) is an attestation API used on devices with Google Play functionality that allows application developers to identify if they are running on a rooted device. This is done by the application reaching out to the API and receiving two boolean variables in response: ```ctsProfileMatch``` and ```basicIntegrity```. While these variables are useful for application developers controlling what functionality runs on compromised / tampered devices there is little information on how Google Play Services come to the conclusion of these variables. While this is unknown, what is known is the variables that feed into this discision. This repo is a re-implementation of the aggregation of these variables.

## Currently implemented 
The versions of Google Play SNet are undocumented, however, in the 12 versions that I have reviewed I have identified ~55 variable types. The below denaotes which of these groups are currently re-implemented in this application:

- Device Data ✔️
- Settings Finder ✔️
- SD Card Analyzer ❌
- CaptivePortalDetector ❌
- Proxy Analyzer ❌
- Preffered Package Finder ❌
- Interesting Files ❌
- More... ❌

## Usage
This application is designed more as a reference point, rather than to be used in production. Each class comprises of one type of variable aggregation. All classes are wrapped in an application which when run displays all aggregated variables (which are stored in Shared Preferences) in a Text View.

<br>
<p align="center">
  <img src="/Images/screenshot_1.png" width="200" />
  <img src="/Images/screenshot_2.png" width="200" />
</p>
<br>


# Thanks
[Colorful Typhoon](https://www.dafont.com/babyblocks.font) for the font, [Maria Leandro](https://www.iconfinder.com/tatica) for the icon, and [UnDraw](https://undraw.co/) for additional images. 
