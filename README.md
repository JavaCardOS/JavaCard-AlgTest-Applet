# JavaCard-AlgTest-Applet
Automated testing tool for algorithms supported by all smart card with JavaCard platform.

Detailed usage:

  1. Download prepared version (AlgTest_JavaCard/AlgTest_***.cap) or compile your own modification of AlgTest applet. In case of use of provided *.cap file, use version that is supported by your card or simply start with version converted with highest version of converter (e.g., AlgTest_v1.1_jc2.2.2.cap) and then use lower version if card refuses to accept this file.

  2. Use pyApduTool to upload AlgTest package to your smart card and install it :  Package AID: 6D 79 70 61 63 6B 61 67 31, Applet AID: 6D 79 70 61 63 30 30 30  31. No special installation parameters are given.
  Sometimes,you need your card's authentication keys to have permission to upload and install applet on it.

  3. Run application AlgTestJClient.jar:  To run the project from the command line, go to the dist folder and type the following:
  "java -jar AlgTestJClient.jar"
  Choose the target reader for card with uploaded AlgTest applet and let it run. CSV file with values separated by the semicolon is created (such as AlgTest_ATR.csv).

  4. You can visit  "http://javacardos.com/javacardforum/viewforum.php?f=30" to get more scripts and leave your comments and suggestions.

Note:

1, This source code is freely and under the LGPL license.

2,  "JCAlgTest_JavaCard"  directory is the whole JCIDE project. You can click http://www.javacardos.com/JCIDE/downloads/JCKit.zip to download and use it.
  
