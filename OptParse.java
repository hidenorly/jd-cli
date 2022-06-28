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

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList; 
import java.util.List;

public class OptParse {
  public List<String> args = new ArrayList<String>();
  public Map<String, String> values = new HashMap<String, String>();

  public static class OptParseItem
  {
	  public String option;     // "-h"
	  public String fullOption; // "--help"
	  public boolean bArgRequired; // true: the value required / false: the value not required
	  public String value;
	  public String description;

    public OptParseItem(){};
    public OptParseItem(String theOption, String theFullOption, boolean theArgRequired, String defaultValue, String theDescription){
    	option = theOption;
    	fullOption = theFullOption;
    	bArgRequired = theArgRequired;
    	value = defaultValue;
    	description = theDescription;
    }
  };

	protected void parseOption(OptParseItem anOption, String[] argv)
  {
    for( int i=0, c=argv.length; i<c; i++ ){
      if( argv[i].equals( anOption.option ) ){
        if( anOption.bArgRequired && (i+1) < c ){
          values.put( anOption.option, argv[ i+1 ] );
        } else {
          values.put( anOption.option, anOption.option );
        }
        break;
      } else if( argv[i].equals( anOption.fullOption ) ){
        int nPos = argv[i].indexOf("=");
        if( nPos != -1 ){
          values.put( anOption.option, argv[i].substring( nPos+1, argv[i].length() ) );
        } else {
          values.put( anOption.option, anOption.option );
        }
        break;
      }
    }
  }

  protected void parseArgs(Vector<OptParseItem> options, String[] argv){
  	for(int i=0, c=argv.length; i<c; i++ ){
  		if( argv[i].startsWith("-") ){
  			for( OptParseItem anOption : options ){
  				if( anOption.option.equals( argv[i] ) || anOption.fullOption.equals( argv[i] ) ){
            if( anOption.bArgRequired ){
  						i++;
  					}
  					break;
  				}
  			}
  		} else {
  			args.add( argv[i] );
  		}
  	}
  }

  protected void parseOpts(Vector<OptParseItem> options, String[] argv){
  	parseArgs( options, argv );

    for( OptParseItem anOption : options ){
      parseOption( anOption, argv );
    }
    for( OptParseItem anOption : options ){
      if( anOption.option != "-h" && !values.containsKey( anOption.option ) ){
        values.put( anOption.option, anOption.bArgRequired ? anOption.value : anOption.option );
      }
    }
  };

  public boolean isOptionIncluded(Vector<OptParseItem> options, String option, String fullOption){
    boolean bFound = false;
    for( OptParseItem anOption : options ){
      if( anOption.option == option || anOption.fullOption == fullOption ){
        bFound = true;
        break;
      }
    }
    return bFound;
  }

	protected String ljust(String str, int nLength){
    String result = str;
    for(int i=0, c=nLength-str.length(); i<c; i++){
      result += " ";
    }
    return result;
  }

	public void printHelp(Vector<OptParseItem> options){
    int nOptionMax = 0;
    int nFullOptionMax = 0;

    for( OptParseItem anOption : options ){
      nOptionMax = Math.max( nOptionMax, (int)anOption.option.length() );
      nFullOptionMax = Math.max( nFullOptionMax, (int)anOption.fullOption.length() );
    }
    for( OptParseItem anOption : options ){
      System.out.println( "  " + ljust(anOption.option, nOptionMax) + "\t" + ljust(anOption.fullOption, nFullOptionMax) + " : " + anOption.description );
    }
    System.exit(0);
  }

  public OptParse(String[] argv, Vector<OptParseItem> options, String description){
    if( !isOptionIncluded(options, "-h", "--help") ){
      options.add( new OptParseItem("-h", "--help", false, "", "Show help") );
    }
    parseOpts( options, argv );
    if( values.containsKey("-h") ){
      if( !description.isEmpty() ){
        System.out.println( description );
      }
      printHelp(options);
    }
  }
}
