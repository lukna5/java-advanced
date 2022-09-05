echo off
cd ../java-solutions
SET junit=..\lib\junit-4.11.jar
SET hamcrest=..\lib\hamcrest-core-1.3.jar
SET classpath=info\kgeorgiy\ja\lorents\bank
SET classname=info.kgeorgiy.ja.lorents.bank
SET temp=%java-solutions%\temp
mkdir .\temp
javac -cp %junit%; %hamcrest% .\info\kgeorgiy\ja\kononov\bank\*.java -d %temp%
cd %tmp%
java -cp %junit%;%hamcrest%;%temp% org.junit.runner.JUnitCore %classname%.BankTests
cd %java-solutions%
rmdir /s /q %tmp%