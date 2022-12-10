# URL Shortener App

## Cómo desplegar un JAR

1- Crear un nuevo proyecto de JavaFX con el SDK de Java19

```IntellyJ
File -> New -> Project -> JavaFX -> JavaFX Application
```

2- Renombrar HelloController a Controller y HelloApp a App

```IntellyJ
Refactor -> Rename
```

3- Crear una nueva clase main

```IntellyJ
File -> New -> Java Class
```

4 - Llamar a la clase App

```Java
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
```

5- Crear la configuración para generar el JAR

```IntellyJ
File -> Project Structure -> Artifacts -> + -> JAR -> From modules with dependencies
```

6- Hacer el build del proyecto

```IntellyJ
Build -> Build Artifacts...
```

7- Ejecutar el JAR con comandos o doble click

```bash
java -jar out/artifacts/URLShortener_jar/URLShortener.jar
```

## Generar un pkg instalable en Mac

1- Ir al directorio `out/artifacts/desktopApp_jar` y en él crear los siguientes directorios:

```bash
mkdir ../package/
mkdir ../package/macos
```

2- Copiar en `macos/` el fichero `.icns`.

3- Generar el instalador con este comando en el directorio `out/artifacts/desktopApp_jar`:

```bash
jpackage --name UrlShortener --input . --main-jar desktopApp.jar --resource-dir ../../../desktopApp/src/main/resources --type pkg --icon ../../../desktopApp/src/main/resources/images/icon.icns
```