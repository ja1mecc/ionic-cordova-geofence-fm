# Plugin Geofence sólo para Finmarkets
======

Plugin de iOS y Android.

## Instalación:

Instalar Plugin: `ionic cordova plugin add https://github.com/ja1mecc/ionic-cordova-geofence-fm.git`

## home.ts:

```
(<any>window).MiPlugin.init(this.successCallback, this.errorCallback);

successCallback() {
    (<any>window).MiPlugin.addOrUpdateFence({
      id: "123456789",
      latitud: -33.3984827,
      longitud: -70.5739585,
      radius: 200
    });
    (<any>window).MiPlugin.addOrUpdateFence({
      id: "987654321",
      latitud: -33.4506985,
      longitud: -70.6436498,
      radius: 200
    });

}

errorCallback(error) {
  console.log("errorCallback -> " + error);
}
  
```


## Autor

* **@Ja1meCC 
