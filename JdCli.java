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

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class JdCli {
  static protected void doDisassemble(String path){
    Loader loader = new Loader() {
      protected InputStream getStream(String internalName){
        InputStream is = this.getClass().getResourceAsStream( "/" + internalName + ".class" );
        if( is == null ){
          try{
            is = new FileInputStream( internalName );
          } catch( FileNotFoundException e ){
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
        return true;//this.getClass().getResource("/" + internalName + ".class") != null;
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
    System.out.println( source );
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
    options.add( new OptParseItem("-o", "--output", true, ".", "Specify output path") );

    OptParse opt = new OptParse( args, options, "JdCli [options] target1.class [target2.class ...]");

    for(int i=0, c=opt.args.size(); i<c; i++){
      String anArg = opt.args.get(i);
      List<Path> paths = getClassPaths( anArg );
      for(int j=0, d=paths.size(); j<d; j++ ){
        Path thePath = paths.get(j);
        if( thePath.toString().equals( anArg ) ){
          doDisassemble( anArg );
        } else {
          doDisassemble( anArg+":"+thePath.toString() );
        }
      }
    }
  }
}