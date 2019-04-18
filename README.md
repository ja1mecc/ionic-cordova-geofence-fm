# Plugin Geofence Finmarkets
======

Plugin de iOS y Android.

## Instalación:

Instalar Plugin: `ionic cordova plugin add https://github.com/ja1mecc/ionic-cordova-geofence-fm.git`

## Crear provider/service para implementar Plugin

Implementar provider/service  del plugin para utilizarlo en page

## geofence-fm.service.ts
```
import { Injectable } from '@angular/core';
import { Plugin, cordova, CordovaProperty, CordovaInstance, IonicNativePlugin } from '@ionic-native/core';

@Injectable({
  providedIn: 'root'
})
export class GeofenceFmService extends IonicNativePlugin {
  static pluginName = 'GeofenceFM';
  static plugin = 'cordova-plugin-geofence-fm';
  static pluginRef = 'GeofenceFM';
  static repo = 'https://github.com/ja1mecc/ionic-cordova-geofence-fm.git';
  static platforms = ["Android", "iOS"]

  init(): Promise<any> { 
     return cordova(this, 'init', {}, []);
  }

  addOrUpdateFence(args: any): Promise<any> { 
    console.log(JSON.stringify(args));
    return cordova(this, 'addOrUpdateFence', {}, [args]);
 }
  
}
```


## Implementación en proyecto

Importar provider/service  del plugin en @NgModule

## app.module.ts
```
import { GeofenceFmService } from './providers/geofence-fm.service';

...

@NgModule({
...

  providers: [
...
    GeofenceFmService
...
  ]
...
})
export class AppModule { }
```

Importar provider/service  del plugin en page

## home.ts:

```
import { Component } from '@angular/core';
//Agregar Platform para poder evaluar si ya se cargo y esta lista la plataforma
import { NavController, Platform } from 'ionic-angular';

import { GeofenceFmService } from './../providers/geofence-fm.service';

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {

  constructor(
  	public navCtrl: NavController,
  	public platform: Platform, 
  	public geofenceFmService: GeofenceFmService) {

	this.platform.ready().then(() => {
		geofenceFmService.init().then((data) => {
			if(data.equals("OK"))
				geofenceFmService.addOrUpdateFence({
				    id: { id } + "|" + { rut } + "|" + { tokenDevice },
				    latitud: -33.3984827,
				    longitud: -70.5739585,
				    radius: 200
				});

		}).catch((error) => {
			console.log('Error geoFence Finmarkets -> ' + error);
		});
	});

  }

}

```


## Autor

* **@Ja1meCC 
