RadioSploit 1.0
================

This Android application allows to sniff and inject Zigbee, Mosart and Enhanced ShockBurst packets from a Samsung Galaxy S20 smartphone. 
It interacts with a set of patches installed on the phone Bluetooth controller, allowing to add new capabilities to communicate using the previously mentioned protocols.

This project is a Proof of Concept developed in the context of a research work aiming at exploring the feasibility of cross-protocol pivoting attacks. If you need additional details, we have published multiple papers about it:

   * Romain Cayre, Florent Galtier, Guillaume Auriol, Vincent Nicomette, Mohamed Kaâniche, Géraldine Marconato. POSTER: Cross-protocol attacks : weaponizing a smartphone by diverting its Bluetooth controller. *14th ACM Conference on Security and Privacy in Wireless and Mobile Networks (WiSec 2021)*, Jun 2021, Abu Dhabi (virtual), UAE. **\[en\]**

   * Romain Cayre, Florent Galtier. [Attaques inter-protocolaires par détournement du contrôleur Bluetooth d'un téléphone mobile](https://hal.laas.fr/hal-03221148). *GT Sécurité des Systèmes, Logiciels et Réseaux*, May 2021, En ligne, France. **\[fr\]**

   * Romain Cayre, Florent Galtier, Guillaume Auriol, Vincent Nicomette, Mohamed Kaâniche, et al. [WazaBee: attacking Zigbee networks by diverting Bluetooth Low Energy chips](https://hal.laas.fr/hal-03193299). *IEEE/IFIP International Conference on Dependable Systems and Networks (DSN)*, Jun 2021, Taipei (virtual), Taiwan. **\[en\]**

   * Romain Cayre, Florent Galtier, Guillaume Auriol, Vincent Nicomette, Geraldine Marconato. [WazaBee : attaque de réseaux Zigbee par détournement de puces Bluetooth Low Energy](https://hal.laas.fr/hal-02778262). *Symposium sur la Sécurité des Technologies de l'Information et des Communications (SSTIC 2020)*, Jun 2020, Rennes, France. pp.381-418.**\[fr\]**


This application is released as an opensource software using the MIT License.

Screenshots
============
![](screenshots/zigbeerx.jpg =250x)
![](screenshots/zigbeetx.jpg =250x)
![](screenshots/mosartscan.jpg =250x)
![](screenshots/mosartkeylogger.jpg =250x)
![](screenshots/mosartrx.jpg =250x)
![](screenshots/esbrx.jpg =250x)
![](screenshots/esbtx.jpg =250x)

How to use this application ? 
==============================
* First, you need to root your Samsung Galaxy S20: it should also work on Samsung Galaxy S10, but it has not been tested yet. Multiple tutorials can be found online to enable root.
* Then, you have to install the Radiosploit patches on your Bluetooth controller. The patches are uploaded on a different repository, follow [these instructions](https://github.com/RCayre/radiosploit_patches) !
* Finally, you can install the application using adb:
```
$ adb install radiosploit.apk
```
* Launch the app and allow it to use root permissions.



