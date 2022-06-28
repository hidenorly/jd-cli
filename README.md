# What's this?

This is command line Java class disassembler.

# configure

```
export $JD_CORE_PATH=~/work/jd-core/build/libs/jd-core-1.1.4.jar
```

# how to build

```
% ./build.sh
```

Note that this is tentative.


# how to execute

```
% ./class2java.sh out/com/github/hidenorly/jdcli/JdCli.class
package com.github.hidenorly.jdcli;

public class JdCli {
  public static void main(java.lang.String[] paramArrayOfString) {
    for (int i = 0, j = paramArrayOfString.length; i < j; i++) {
      java.lang.Object object1 = new java.lang.Object();
      java.lang.Object object2 = new java.lang.Object();
      org.jd.core.v1.ClassFileToJavaSourceDecompiler classFileToJavaSourceDecompiler = new org.jd.core.v1.ClassFileToJavaSourceDecompiler();
      try {
        classFileToJavaSourceDecompiler.decompile((org.jd.core.v1.api.loader.Loader)object1, (org.jd.core.v1.api.printer.Printer)object2, paramArrayOfString[i]);
      } catch (java.lang.Exception exception) {}
      java.lang.String str = object2.toString();
      java.lang.System.out.println(str);
    } 
  }
}
```
