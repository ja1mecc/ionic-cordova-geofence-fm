# Plugin Geofence Finmarkets IONIC v4
======

Plugin de iOS y Android.

### Instalación:

Instalar Plugin: `ionic cordova plugin add https://github.com/ja1mecc/ionic-cordova-geofence-fm.git`

### Remover:

Remover Plugin: `ionic cordova plugin rm ionic-cordova-geofence-fm`

### Crea provider/service de Plugin

Crear provider/service del plugin para utilizarlo en page `ionic generate service providers/geofence-fm`

#### geofence-fm.service.ts
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


### Implementación en proyecto

Importar provider/service  del plugin en @NgModule

#### app.module.ts
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

#### home.page.ts:

```
import { Component } from '@angular/core';
import { Platform } from '@ionic/angular';

import { GeofenceFmService } from './../providers/geofence-fm.service';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {

  constructor(
  	public platform: Platform, 
  	public geofenceFmService: GeofenceFmService) {

	this.platform.ready().then(() => {
		geofenceFmService.init().then((data) => {
			if(data == "OK")
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

### Permisos para iOS

Para iOS se deben solicitar los permisos de localización en `config.xml`

```
<platform name="ios">
	...
	<edit-config file="*-Info.plist" mode="merge" target="NSLocationAlwaysAndWhenInUseUsageDescription">
	    <string>Permítenos acceder a tu ubicación para Geofence</string>
	</edit-config>
	<edit-config file="*-Info.plist" mode="merge" target="NSLocationWhenInUseUsageDescription">
	    <string>Permítenos acceder a tu ubicación para Geofence</string>
	</edit-config>
	...
</platform>
```

### Autor

* **@Ja1meCC 
