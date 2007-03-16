package org.geworkbench.components.cascript;

//we want to use CTRL . though, remember that!

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * editor class: this class is used for the CTRLPERIOD functionality
 *
 * @author Behrooz Badii - badiib@gmail.com
 */
public class CTP {
  public String id, token, entirecode, partofcode, moduletype, moduleid, functionid, lastid, lasttype;
  public boolean isModuleOrDatatype = false, isDataType = false, notfirstcheck = true, nosemi = false;
  public CTPSymbolTable symt = new CTPSymbolTable(null, -1, "global");
  
  //this is used to check the correctness of any identifier
  public boolean IDcheck(String tok) {
    char[] check = tok.toCharArray();
    if (!(Character.isLetter(check[0]) || check[0] == '_')) {
      return false;
    }
    for (int i = 1; i < check.length; i++) {
      if (!(Character.isLetterOrDigit(check[i]) || check[i] == '_')) {
        return false;
      }
    }
    return true;
  }
  
  //this method is almost identical to IDcheck, but it is not used only once
  //ask Manju what the standard is for naming modules in geWorkBench!
  //if it is the same as the identifiers for antlr, eliminate this method and use IDcheck(String)
  public boolean modTypeCheck(String md) {
    char[] check = md.toCharArray();
    if (!(Character.isLetter(check[0]) || check[0] == '_')) {
      return false;
    }
    for (int i = 1; i < check.length; i++) {
      if (!(Character.isLetterOrDigit(check[i]) || check[i] == '_')) {
        return false;
      }
    }
    return true;
  }
  
  //this method eats all delimiters except the semicolon
  /*public static void eatDelim(StringTokenizer st) {
      token = st.nextToken();
      while (token.equals("\t") || token.equals("\n") || token.equals("\r") || 
              token.equals("\f") || token.equals(" ")) {
        st.hasMoreTokens();
      token = st.nextToken();
      }
  }*/
  
  //this method checks for the end of a variable declaration statement, which is a semicolon
  //after successfully getting to the end, you add the public variable to the symbol table!
  // we also make moduleid and moduletype null afterwards
  public void varDecEnd(StringTokenizer st) {
    //System.out.println("found " + moduleid);
    if (st.hasMoreTokens()) { //if there are more tokens, go forwards
        token = st.nextToken();
        if (token.equals(";") || (token.equals("=") && isDataType)) { //does the token equals semicolon?
            //System.out.println("found ;");
            symt.putVar(moduleid, moduletype); //end of a pub var statement, put it into symboltable
            moduletype = null;
            moduleid = null;
            //System.out.println("symbol table insertion");
        }
    }
  }
  
  //this method checks for the name of a module and records it in variable moduleid
  //if that is successful, we move onto the variable declaration end method
  public void varDecModName(StringTokenizer st) {
    //System.out.println("found " + moduletype);
    if (st.hasMoreTokens()) { //if there are more tokens, go forwards
        token = st.nextToken();
        if (IDcheck(token)) { //is the token a viable caScript identifier?
            moduleid = token; // remember module name
            varDecEnd(st); //call variable Declaration statement end method
         }
    }
  }
  
  //this method checks for the type of a module and records it in variable moduletype
  //if that is successful, we move onto the module name method
  public void varDecModType(StringTokenizer st) {
    //System.out.println("found module")  ;
    if (st.hasMoreTokens()) { //if there are more tokens, go forward
        token = st.nextToken();
        if (modTypeCheck(token)) { //if the token a module type?
            moduletype = token; //remember module type
            varDecModName(st); //call module identifier checking method
        }
    }
  }
  
  //this method checks for the keyword "module" in a variable declaration
  //if that is successful, we move onto the module type method
  public void varDecMod(StringTokenizer st) {
    //System.out.println("found public");
    if (st.hasMoreTokens()) { //if there are more tokens, go forward
        token = st.nextToken();
        //check for module, if so, go in deeper       
        if (token.equals("module") || token.equals("datatype")) //does token equals module?
            isDataType = false;
            if (token.equals("datatype")) { isDataType = true; }
            varDecModType(st); //call module type checking method
    }
  }
  
  //this method obtains all the public declarations in a caScript
  //read entirecode in piece by piece with string tokenizer
  //while there are more tokens
  //if last token was public, eat delimiters, check for module
    //if last token was module, eat deliminters, get the module type
        //if last token was module type, eat delminters, get the module identifier
            //if last token was module identifier, eat delimiters, get semicolon
                //if last token was semicolon, put module id and module type into symbol table
  //if last token began with"//", look for "\n"
  //if last token began with"/*", look for token ending with"*/"
  //nullify module id, module type, and token strings
  public void getPubDecs() {
    //StringTokenizer st = new StringTokenizer(entirecode, "; \t\n\r\f", true);
    StringTokenizer st = new StringTokenizer(entirecode, " \t\n\r\f");
    while (st.hasMoreTokens()) {
        token = st.nextToken();
        //time to test for a public variable
        if (token.equals("public")) //does token equal public?
            varDecMod(st); //call module checking method
    }
  token = null;
  }

  //helper function for getLocDecs and end of file
  //if there are no more tokens, use symbol table to find identifier and use it
  //if no identifier in symbol table, do nothing
  public boolean moreTokens(StringTokenizer st) {
      if (st.hasMoreTokens()) {
        nextTokenIDCheck(st);
        return true;
      }
      else if (notfirstcheck){
        //System.out.println("last token was " + token);
        lasttype = symt.findVar(lastid);
        //System.out.println(lasttype);
        notfirstcheck = false;
      }
      return false;
  }

  //this function checks if we have a return type for the function, we just want to make sure it exists
  public boolean retType(String tok) {
      if (token.equals("void") || token.equals("int") || token.equals("float") || token.equals("string")) {
          //System.out.println("in retType primitive returns");
          return true;
      }
      else if (token.equals("module") || token.equals("datatype")) {
          //System.out.println("in retType complex returns");
          isModuleOrDatatype = true;
          return true;
      }
      else
          return false;
  }
  
  //this method discards the module or datatype type, which is useless for function declarations for us
  public void eliminatemodType (StringTokenizer st) {
    isModuleOrDatatype = false;
    if (moreTokens(st)) { //if there are more tokens, go forward
      if (IDcheck(token)) { //is the token a viable caScript identifier?
          checkFuncName(st); //get the functionname
      }
    }
  }
  
  //this method finds out the name of the function
  public void checkFuncName (StringTokenizer st) {
    //System.out.println("in checkFuncName");
    if (moreTokens(st)) { //if there are more tokens, go forward
        //System.out.println(token + " should be the function name");
        if (IDcheck(token)) {//check if token is an id
            functionid = token; //record function name
            //System.out.println("found " + functionid);
            checkParen(st); //check for parentheses
        }
    }
  }
  
  //this function checks the opening, close parentheses and opening brace of a function
  //if they exist, we are inside a function
  //that being the case, we need a new symbol table for that function and exploration of said function
  public void checkParen(StringTokenizer st) {
    //System.out.println("inside checkParen");
   // System.out.println(token);
    if (moreTokens(st)) {
        //System.out.println(token);
        if (token.equals("(")) { //there can be declarations in the parentheses!
            //System.out.println("symbol table's parent");
            //System.out.println(symt.toString());
            symt = new CTPSymbolTable(symt, symt.getLevel()+1, functionid);    
            while ((!(token.equals("}"))) && moreTokens(st)) { //equality check comes first!
                if (token.equals("module") || token.equals("datatype")) {
                    isDataType = false;
                    if (token.equals("datatype")) { isDataType = true; }
                    nosemi = true;
                    varDecModTypef(st);
                }
                if (token.equals (")")) {
                    if (moreTokens(st)) {
                        if (token.equals("{")) {
                            enterFunction(st);
                            //System.out.println(symt.toString());
                        }
                    }
                }
            }
            //System.out.println("leaving function");
            //System.out.println(symt.toString());
            symt = symt.Parent();    
        }
    }
  }
  
  //this function checks in a new token to variable token and checks it for being a viable identifier
  public void nextTokenIDCheck(StringTokenizer st) { //every time we get a token
      token = st.nextToken(); 
      if (IDcheck(token)) //we want to check if it's an ID
        lastid = token; //if yes, we hold onto it for the ctrl . purpose
  }
  
  //this method checks for the type of a module and records it in variable moduletype
  //if that is successful, we move onto the module name method
  public void varDecModTypef(StringTokenizer st) {
    if (moreTokens(st)) { //if there are more tokens, go forward
        if (modTypeCheck(token)) { //if the token a module type?
            moduletype = token; //remember module type
            varDecModNamef(st); //call module identifier checking method
        }
    }
  }
  
  //this method checks for the name of a module and records it in variable moduleid
  //if that is successful, we move onto the variable declaration end method
  public void varDecModNamef(StringTokenizer st) {
    if (moreTokens(st)) { //if there are more tokens, go forwards
        if (IDcheck(token)) { //is the token a viable caScript identifier?
            moduleid = token; // remember module name
            varDecEndf(st); //call variable Declaration statement end method
        }
    }
  }
  
  //this method checks for the end of a variable declaration statement, which is a semicolon
  //after successfully getting to the end, you add the public variable to the symbol table!
  // we also make moduleid and moduletype null afterwards
  public void varDecEndf(StringTokenizer st) {
    if (moreTokens(st)) { //if there are more tokens, go forwards
        if (nosemi == true) {
            symt.putVar(moduleid, moduletype); //end of a pub var statement, put it into symboltable
            //System.out.println("symbol table insertion of " + moduletype + " : " + moduleid);
            moduletype = null;
            moduleid = null;
            nosemi = false;
        }
        else if (token.equals(";") || (token.equals("=") && isDataType)) { //does the token equals semicolon?
            symt.putVar(moduleid, moduletype); //end of a pub var statement, put it into symboltable
            //System.out.println("symbol table insertion of " + moduletype + " : " + moduleid);
            moduletype = null;
            moduleid = null;
        }
    }
  }
  
  //this is the beginning of the while statement test!
  public void enterwhile(StringTokenizer st) {
    int numbraces = 0; //for ifs and other braces;
    int numparen = 0;
    //System.out.println("work on the while");
    boolean notwhileend = true;
    //System.out.println(symt.toString());
    symt = new CTPSymbolTable(symt, symt.getLevel()+1, "while");
    //System.out.println("after symt renewal");
    //System.out.println(symt.toString());
    if (moreTokens(st)) {//more tokens, go forward
        if (token.equals("(")) {  //caught the parentheses
           numparen++;
           //System.out.println("found parentheses");
           while (notwhileend && moreTokens(st)) {//more tokens, go forward
               if (token.equals("(")) { 
                   numparen++;
               }
               if(token.equals(")")) {
                   numparen--;
                   if (numparen == 0) {
                    while (notwhileend && moreTokens(st)) {
                       if (token.equals("{")) {
                           numbraces++;
                           //System.out.println("we're past the first for brace!");
                           while (notwhileend && moreTokens(st)) {
                               isDataType = false;
                               if (token.equals("{")) { numbraces++; }
                               if (token.equals("module") || token.equals("datatype")) { varDecModTypef(st);}
                               if (token.equals("datatype")) { isDataType = true; }
                               if (token.equals("}")) {
                                   numbraces--;
                                   if (numbraces == 0) {
                                    //System.out.println("second brace found!");
                                    notwhileend = false;
                                   }
                               }
                           }
                       }
                       else if (moreTokens(st)) {
                           isDataType = false;
                           if (token.equals("module") || token.equals("datatype")) {
                               if (token.equals("datatype")) { isDataType = true; }
                               varDecModTypef(st);
                               notwhileend = false;
                           }
                           else if (token.equals(";")) { notwhileend = false;}
                           while (moreTokens(st)) { //single line, check for semicolon!
                               if (token.equals(";")) { notwhileend = false; }
                           }
                       }
                    }
                   }
              }
           }
        }
    }
    //System.out.println("leaving while");
    //System.out.println(symt.toString());
    symt = symt.Parent();
  }
  
  //this is the beginning of the for statement test!
  public void enterfor(StringTokenizer st) {
    int numbraces = 0; //for ifs and other braces;
    int numparen = 0;
    boolean notforend = true;  
    //System.out.println("at for");
    symt = new CTPSymbolTable(symt, symt.getLevel()+1, "for");
    if (moreTokens(st)) {//more tokens, go forward
        if (token.equals("(")) {  //caught the parentheses
           numparen++;
           //System.out.println("found parentheses");
           while (notforend && moreTokens(st)) {//more tokens, go forward
               isDataType = false;
               if (token.equals("module") || token.equals("datatype")) {
                   if (token.equals("datatype")) { isDataType = true; }
                   varDecModTypef(st); //let's see if we can add something here to the symbol table!
               }
               if (token.equals("(")) {
                   numparen++;
               }
               else if (token.equals(")")) {
                   numparen--;
                   if (numparen == 0) {
                    while (notforend && moreTokens(st)) {
                        if (token.equals("{")) {
                            numbraces++;
                            //System.out.println("we're past the first for brace!");
                            while (notforend && moreTokens(st)) {
                               isDataType = false;
                               if (token.equals("{")) {
                                   numbraces++;
                               }
                               if (token.equals("module") || token.equals("datatype")) {
                                   if (token.equals("datatype")) { isDataType = true; }
                                   varDecModTypef(st);
                               }
                               if (token.equals("}")) {
                                   numbraces--;
                                   if (numbraces == 0) {
                                    //System.out.println("second brace found!");
                                    notforend = false;
                                   }
                               }
                            }
                       }
                       else if (moreTokens(st)) {
                           isDataType = false;
                           if (token.equals("module") || token.equals("datatype")) {
                               if (token.equals("datatype")) { isDataType = true; }
                               varDecModTypef(st);
                               notforend = false;
                           }
                           else if (token.equals(";")) {
                               notforend = false;
                           }
                           while (moreTokens(st)) { //single line, check for semicolon!
                               if (token.equals(";")) {
                                   notforend = false;
                               }
                           }
                       }
                    }
                   }
               }
           }
        }
    }
    //System.out.println("leaving for");
    symt = symt.Parent();
  }
  
  //after understanding that we have a function declaration, go into function, 
  //look for variable declarations and tokens that pass as IDs
  public void enterFunction(StringTokenizer st) {
      //System.out.println("inside function");
      //System.out.println(functionid);
      while (moreTokens(st) && !(token.equals("}"))) { //check for more tokens
          //System.out.println(token);
          isDataType = false;
          if (token.equals("module") || token.equals("datatype")) { //do you see the identifier module?
              if (token.equals("datatype")) { isDataType = true; }
              varDecModTypef(st); //start stepping into the variable declaration
          }
          else if (token.equals("for")) { //deal with fors
            enterfor(st);
          }
          else if (token.equals("while")) { //deal with whiles
            enterwhile(st);
          }
          //do while
      }
  }
  
  //this function obtains all function declaration and explores inside all functions
  public void getFuncDecs() {
    //System.out.println("checking out functions now");
    /*StringTokenizer st1 = new StringTokenizer(partofcode, " \t\n\r\f"); //worry about the part of the code before ctrl .
    while (st1.hasMoreTokens()) {
        token = st1.nextToken();
        //System.out.println(token);
    }*/
    StringTokenizer st = new StringTokenizer(partofcode, " \t\n\r\f"); //worry about the part of the code before ctrl .
    while (moreTokens(st)) { //check for more tokens
        
        //time to test for a function declaration
        if (retType(token)) { //beginning of a function declaration
           // System.out.println("found " + token);
            if (isModuleOrDatatype) { //if it is a module or datatype, kill the modType
               // System.out.println("eliminating mod type in func declaration");
               // System.out.println("touching getFuncDecs");
                eliminatemodType(st);
            }
            else //if not, get the functionname
                //System.out.println("touching getFuncDecs");
                checkFuncName(st);
        }
    }
      
/*//if last token was module, get the type
//if 2nd to last token was module, get the identifier
//eliminate semicolon from end of identifier if necessary
*/
    
/*
    //if last token was void, int, float, string look for an identifier
    //if last token was module, datatype, get the type
    //if 2nd to last token was module, datatype, get identifier
    //if identifier has parentheses at end, good, if not, look for it in the next token
    //keep going until you get a close parentheses either as a lone token or at the end of a token
    //find brace immediately afterwards!
    //you are in a function, enter out of the last one and enter into this new one
*/
    //still to do
    //if for keyword found, great, you're in a for loop look for opening paren
    //open a new entry in the symbol table
    //look at things inside the for loop for a declaration
    //if opening brace was not there, look for semicolon and end symbol table
    //if opening brace was there, end symbol table

    //if while keyword found, great, you're in a while loop
    //open a new entry in the symbol table
    //look at things inside while body for a declaration
    //if opening brace was not there, look for semicolon and end symbol table
    //if opening brace is there, end symbol table
    //if at anytime you hit the end of the file, work with the symbol tables you have ready

  }

  //readies everything for tokenization
  //this tokenizer is cheating very badly, because we're ignoring
  //the meaning of many of the operators!
  //do NOT use this as a true parser
  String readyfortokenization(String code){
    code = code.replaceFirst("null", "");
    //this could be a problem, gets rid of single line comments
    code = code.replaceAll("//.*"," "); 
    //this could be a problem, get rid of multi line comments
    code = code.replaceAll("/\\*.*?\\*/", " "); 
    //distances semicolons, braces, parentheses, and periods from other things
    code = code.replaceAll(";", " ; ");
    code = code.replaceAll("\\{", " { ");
    code = code.replaceAll("\\}", " } ");
    code = code.replaceAll("\\(", " ( ");
    code = code.replaceAll("\\)", " ) ");
    code = code.replaceAll("\\[", " [ ");
    code = code.replaceAll("\\]", " ] ");
    code = code.replaceAll("\\.", " . ");
    code = code.replaceAll("\\*", " * ");
    //problematic: +,++,-,--,!=,==,>=,<=,=
    code = code.replaceAll("\\+", " + ");
    code = code.replaceAll("-", " - ");
    code = code.replaceAll("!", " ! ");
    code = code.replaceAll("=", " = ");
    code = code.replaceAll("-", " - ");
    code = code.replaceAll(">", " > ");
    code = code.replaceAll("<", " < ");
    code = code.replaceAll("/", " / ");
    code = code.replaceAll("%", " % ");
    code = code.replaceAll(",", " , ");
    code = code.replaceAll(":", " : ");
    code = code.replaceAll("#", " # ");
    code = code.replaceAll("\\|\\|", " || ");
    code = code.replaceAll("&&", " && ");
    
    return code;
  }
  //call pubdecs first
  //call loc decs second
  //do IDcheck for last token from string tokenizer from pub decs
  
  //this should change to a single string, instead of array for return type;
  
  public CTP(String ec, String pc) {
      entirecode = ec;
      partofcode = pc;
  }
  public String testid() {
    //String[] ret = new String[2];
    //id = args[0]; //taken out for test purposes;
    /*try {
      //BufferedReader in = new BufferedReader(new FileReader(args[1])); //taken out for test purposes;
      BufferedReader in = new BufferedReader(new FileReader("test4.script")); 
      int c = in.read();
      while(c != -1){
        entirecode += (char)c;
        c = in.read();
      }
      in.close();
      c = -1;
      in = new BufferedReader(new FileReader("test4.script.part")); 
      c = in.read();
      while(c != -1){
        partofcode += (char)c;
        c = in.read();
      }
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    //System.out.println(entirecode);
    entirecode = readyfortokenization(entirecode);
    partofcode = readyfortokenization(partofcode);
    //System.out.println(entirecode);
    //System.out.println(IDcheck(id));
    //call a method that checks out the entire page for public variable declarations!
    getPubDecs();
    //call a method that checks out the part up to ctrl .
    getFuncDecs();
    //System.out.println(symt.toString());
    //System.out.println(lastid +" : "+ lasttype);
    //ret[0] = lastid;
    //ret[1] = lasttype;
    //return ret;
    return lasttype;
  }
}
