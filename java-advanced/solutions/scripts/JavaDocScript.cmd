SET OUT=C:\Users\vovak\java-advanced\javadoc
SET INTERFACE=..\..\..\java-advanced-2022\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\Impler.java
SET InterfaceJar=..\..\..\java-advanced-2022\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\JarImpler.java
SET IMPLEMENTOR=..\..\java-solutions\info\kgeorgiy\ja\kononov\implementor\Implementor.java
SET ImplerException=..\..\..\java-advanced-2022\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\ImplerException.java
cd ..
if not exist javadoc (mkdir javadoc)
cd javadoc
if not exist implementor (mkdir implementor)
cd implementor
SET implementorIn2022=..\..\..\java-advanced-2022\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor
javadoc -author -private %IMPLEMENTOR% %INTERFACE% %InterfaceJar% %ImplerException%
