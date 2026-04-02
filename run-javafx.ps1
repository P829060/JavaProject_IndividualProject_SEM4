$ErrorActionPreference = "Stop"

$javaFxLib = "C:\Users\pvpmo\OneDrive\Desktop\JavaFX\javafx-sdk-26\lib"
$outputDir = "out-compile"

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
$sources = Get-ChildItem -Recurse "src\main\java" -Filter *.java | ForEach-Object { $_.FullName }

javac --module-path $javaFxLib --add-modules javafx.controls,javafx.fxml -d $outputDir $sources

Copy-Item -Recurse -Force "src\main\resources\*" $outputDir

java --module-path $javaFxLib --add-modules javafx.controls,javafx.fxml -cp $outputDir com.hotel.Launcher
-------------------------------------


javac --module-path "C:\Users\pvpmo\OneDrive\Desktop\JavaFX\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d out-compile (Get-ChildItem -Recurse src\main\java -Filter *.java | ForEach-Object { $_.FullName })
Copy-Item -Recurse -Force src\main\resources\* out-compile                   
java --module-path "C:\Users\pvpmo\OneDrive\Desktop\JavaFX\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -cp out-compile com.hotel.Launcher    

