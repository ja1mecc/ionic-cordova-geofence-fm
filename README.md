# Plugin Geofence sólo para Finmarkets
======

Plugin de iOS y Android.

## Instalación:

Instalar Plugin: `ionic cordova plugin add https://github.com/ja1mecc/ionic-cordova-geofence-fm.git`

## home.ts:

```
import { Component } from '@angular/core';
//Agregar Platform para poder evaluar si ya se cargo y esta lista la plataforma
import { NavController, Platform } from 'ionic-angular';

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {

  constructor(public navCtrl: NavController,public platform: Platform) {
    (<any>window).GeofenceFM.init(this.successCallback, this.errorCallback);
  }

  successCallback(data) {
  
			(<any>window).GeofenceFM.addOrUpdateFence({
				id: "987654321",
				latitud: -33.3984827,
				longitud: -70.5739585,
				radius: 200
			});
      
			(<any>window).GeofenceFM.addOrUpdateFence({
				id: "123456789",
				latitud: -33.4506985,
				longitud: -70.6436498,
				radius: 200
			});
		
	}

	errorCallback(error) {
		console.log("errorCallback -> " + error);
	}

}

```


## Autor

* **@Ja1meCC 
