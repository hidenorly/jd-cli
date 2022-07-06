/*
  Copyright (C) 2022 hidenorly

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package com.github.hidenorly.jdcli;

import com.github.hidenorly.jdcli.OptParse;
import com.github.hidenorly.jdcli.OptParse.OptParseItem;

import org.jd.core.v1.api.*;
import org.jd.core.v1.api.Decompiler;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.core.v1.api.printer.Printer;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class JdCli {
  static protected String mOutputPath;
  static protected boolean mIsOutputFile;

  protected static void ensureDirectory(String path){
    try{
      String basePath = new File(path).getParent();
      Files.createDirectories( Paths.get( basePath ) );
    } catch(Exception ex) {
    }
  }

  protected static String getNumberedFilename(String filename, int num){
    int nPos1 = filename.lastIndexOf( ".java" );
    int nPos2 = filename.lastIndexOf( "_", nPos1 );
    if( nPos2 !=-1 ){
      filename = filename.substring( 0, nPos2 + 1 ) + String.valueOf( num ) + ".java";
    } else {
      if( num > 1 ){
        filename = filename.substring( 0, nPos1 ) + "_" + String.valueOf( num ) + ".java";
      }
    }
    return filename;
  }


  protected static String getOutputPath(String source){
    String filename = "";
    int nPos1 = source.indexOf( "package " );
    int nPos2 = source.indexOf( ";", nPos1 );
    if( nPos1!=-1 && nPos2!=-1 ){
      filename = source.substring( nPos1 + 8, nPos2 );
      filename = filename.replace( ".", "/" );

      int nPos3 = filename.indexOf( ":" );
      if( nPos3 != -1 ){
        filename = filename.substring( nPos3 + 1 );
      }

      nPos1 = source.indexOf( "class ",  nPos2 + 1);
      if( nPos1 !=-1 ){
        nPos2 = source.indexOf( " ", nPos1 + 7 );
        if( nPos2 !=-1 ){
          filename = filename + "/" + source.substring( nPos1 + 6, nPos2 );
        }
      }

      filename = filename+".java";
    }

    filename = mOutputPath + "/" + filename;

    int num = 1;
    while( Files.exists( Paths.get( filename ) ) ){
      num++;
      filename = getNumberedFilename( filename, num );
    }

    return filename;
  }

  static protected void doDisassemble(String path){
    Loader loader = new Loader() {
      protected InputStream getStream(String internalName){
        InputStream is = this.getClass().getResourceAsStream( "/" + internalName + ".class" );

        Path targetPath = null;
        try{
          int nPos = internalName.indexOf(":");
          if( nPos !=-1 ){
            // this is .jar case
            String zipPath = internalName.substring(0, nPos);
            String classPath = internalName.substring(nPos+1);
            Path zipFile = Paths.get( zipPath );
            ClassLoader loader = null;
            FileSystem fs = FileSystems.newFileSystem( zipFile, loader );
            targetPath = fs.getPath( classPath );
          } else {
            targetPath = Paths.get( path );
          }
        } catch (Exception ex) {

        }

        if( is == null && targetPath != null ){
          try{
            is = Files.newInputStream( targetPath );
          } catch( Exception ex ){
          }
        }
        return is;
      }

      @Override
      public byte[] load(String internalName) throws LoaderException {
        InputStream is = getStream( internalName );

        if (is == null) {
          System.out.println( "Loader::load:"+internalName+":failed" );
          return null;
        } else {
          try (InputStream in=is; ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);

            while (read > 0) {
              out.write(buffer, 0, read);
              read = in.read(buffer);
            }

            return out.toByteArray();
          } catch (IOException e) {
            throw new LoaderException(e);
          }
        }
      }

      @Override
      public boolean canLoad(String internalName) {
        return true;
      }
    };

    Printer printer = new Printer() {
      protected static final String TAB = "  ";
      protected static final String NEWLINE = "\n";

      protected int indentationCount = 0;
      protected StringBuilder sb = new StringBuilder();

      @Override public String toString() { return sb.toString(); }

      @Override public void start(int maxLineNumber, int majorVersion, int minorVersion) {}
      @Override public void end() { }

      @Override public void printText(String text) { sb.append(text); }
      @Override public void printNumericConstant(String constant) { sb.append(constant); }
      @Override public void printStringConstant(String constant, String ownerInternalName) { sb.append(constant); }
      @Override public void printKeyword(String keyword) { sb.append(keyword); }
      @Override public void printDeclaration(int type, String internalTypeName, String name, String descriptor) { sb.append(name); }
      @Override public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) { sb.append(name); }

      @Override public void indent() { this.indentationCount++; }
      @Override public void unindent() { this.indentationCount--; }

      @Override public void startLine(int lineNumber) { for (int i=0; i<indentationCount; i++) sb.append(TAB); }
      @Override public void endLine() { sb.append(NEWLINE); }
      @Override public void extraLine(int count) { while (count-- > 0) sb.append(NEWLINE); }

      @Override public void startMarker(int type) {}
      @Override public void endMarker(int type) {}
    };

    ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
    try{
      decompiler.decompile(loader, printer, path);
    } catch (Exception ex) {

    }
    String source = printer.toString();
    if( !mIsOutputFile ){
      System.out.println( source );
    } else {
      String outputPath = getOutputPath( source );
      ensureDirectory( outputPath );
      try {
        PrintStream ps = new PrintStream( new FileOutputStream( outputPath ) );
        ps.print( source );
        ps.close();
      } catch( Exception ex ){
      }
    }
  }

  protected static List<Path> getClassPaths(String path){
    List<Path> result = new ArrayList<Path>();

    Path thePath = Paths.get( path );
    if( Files.isDirectory( thePath ) || path.endsWith(".apk") || path.endsWith(".zip") || path.endsWith(".jar") ){
      result = FileLister.getFileList( path, ".*\\.class" );
    } else {
      result.add( thePath );
    }

    return result;
  }

  public static void main(String[] args) {
    Vector<OptParseItem> options = new Vector<OptParseItem>();
    options.add( new OptParseItem("-o", "--output", true, "", "Specify output path if you want to output as file") );

    OptParse opt = new OptParse( args, options, "JdCli [options] target1.class [target2.class ...]");
    mOutputPath = opt.values.get("-o");
    mIsOutputFile = !opt.values.get("-o").isEmpty();

    for(int i=0, c=opt.args.size(); i<c; i++){
      String anArg = opt.args.get(i);
      List<Path> paths = getClassPaths( anArg );
      for(int j=0, d=paths.size(); j<d; j++ ){
        Path thePath = paths.get(j);
        if( thePath.toString().startsWith( anArg ) ){
          doDisassemble( thePath.toString() );
        } else {
          doDisassemble( anArg+":"+thePath.toString() );
        }
      }
    }
  }
}