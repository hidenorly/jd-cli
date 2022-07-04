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

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;


public class FileLister implements FileVisitor<Path> {
  protected List<Path> mFiles = new ArrayList<Path>();
  protected String mFilterRegexp;

  protected FileLister(String filterRegexp){
    mFilterRegexp = filterRegexp;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    //mFiles.add( dir.toString() );
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    String _path = file.toString();
    if( mFilterRegexp.isEmpty() || _path.matches(mFilterRegexp) ){
      mFiles.add( file );
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  protected List<Path> getFiles(){
    return mFiles;
  }

  public static List<Path> getFileList(String path, String filterRegexp){
    Path root = null;
    try{
      if( path.endsWith(".zip") || path.endsWith(".jar") || path.endsWith(".apk") ){
        Path zipFile = Paths.get( path );
        ClassLoader loader = null;
        FileSystem fs = FileSystems.newFileSystem( zipFile, loader );
        root = fs.getPath("/");
      } else {
        root = Paths.get( path );
      }
    } catch(IOException ex){
    }
    FileLister visitor = new FileLister( filterRegexp );

    if( root != null ){
      try{
        Files.walkFileTree(root, visitor);
      } catch(IOException ex ){

      }
    }

    return visitor.getFiles();
  }


  public static List<Path> getFileList(String path){
    return getFileList( path, "" );
  }
}