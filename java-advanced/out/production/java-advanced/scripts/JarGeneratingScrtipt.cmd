cd ..\java-solutions
SET ModuleJar=..\..\java-advanced-2022\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET IMPLEMENTOR=info\kgeorgiy\ja\kononov\implementor\Implementor.java
SET ImplementorClass=info\kgeorgiy\ja\kononov\implementor\Implementor.class
javac -cp %ModuleJar% %IMPLEMENTOR%
jar cmvf MANIFEST.MF Implementor.jar %ImplementorClass%