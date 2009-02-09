package mt.discrimreorder;

import java.io.*;
import mt.train.AbstractWordAlignment;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.pennchinese.*;
import edu.stanford.nlp.parser.lexparser.ChineseTreebankParserParams;
import java.util.*;
import java.io.*;

/**
 * This class is to represent the alignment in a 2d matrix form
 * (Note that there's similar class in the mt.train package or mt.train.transtb.
 *  But I think it's good to make a new one in this package so things don't get tangled up.(
 *
 * @author Pi-Chuan Chang
 */

public class AlignmentMatrix {
  String[] f;
  String[] e;
  boolean[][] fe;
  int[] d2g; // dependent to governor
  String[][] tDeps;

  void getParseInfo(Tree t) {
    d2g = new int[f.length];
    tDeps = new String[f.length][f.length];

    try {
      Filter<String> puncWordFilter = Filters.acceptFilter();
      GrammaticalStructure gs = new ChineseGrammaticalStructure(t, puncWordFilter);
      Collection<TypedDependency> typedDeps = gs.typedDependencies(false);
      if (typedDeps.size() != f.length-2-1) {
        System.err.print("f=");
        System.err.println(StringUtils.join(f, " "));
        System.err.println("DEPs=");
        for(TypedDependency d : typedDeps) {
          System.err.println(d);
        }
        throw new RuntimeException();
      }
      if (t.yield().size()+2 != f.length) {
        throw new RuntimeException();
      }
      // The index of dep is 1-based (instead of starting at 0).
      // So it matches well with the AlignmentMatrix, where 0 is <s> anyway.
      for (TypedDependency dep : typedDeps) {
        System.err.println("DEP="+dep);
      }
      for (TypedDependency dep : typedDeps) {
        d2g[dep.dep().index()] = dep.gov().index();
        System.err.printf("d2g[%d]=%d\n", dep.dep().index(), dep.gov().index());
        tDeps[dep.gov().index()][dep.dep().index()] = dep.reln().getShortName();
        System.err.printf("tDeps[%d][%d]=%s\n", dep.gov().index(), dep.dep().index(), dep.reln().getShortName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public String getSourceWord(int i) {
    if (i < 0 || i >= f.length) return "";
    return f[i];
  }

  public String getTargetWord(int i) {
    if (i < 0 || i >= e.length) return "";
    return e[i];
  }

  private static String[] preproc(String[] words) {
    return AbstractWordAlignment.preproc(words);
  }

  public AlignmentMatrix(String fStr, String eStr, String aStr) 
    throws IOException{
    // for now, always append the boundary symbols
    fStr = new StringBuffer("<s> ").append(fStr).append(" </s>").toString();
    eStr = new StringBuffer("<s> ").append(eStr).append(" </s>").toString();
    f = preproc(fStr.split("\\s+"));
    e = preproc(eStr.split("\\s+"));

    fe = new boolean[f.length][e.length];

    for(String al : aStr.split("\\s+")) {
      String[] els = al.split("-");
      if(els.length == 2) {
        int fpos = Integer.parseInt(els[0]);
        int epos = Integer.parseInt(els[1]);
        // adding one because of the boundary symbol
        ++fpos; ++epos;
        if(0 > fpos || fpos >= f.length)
          throw new IOException("f has index out of bounds (fsize="+f.length+",esize="+e.length+") : "+fpos);
        if(0 > epos || epos >= e.length)
          throw new IOException("e has index out of bounds (esize="+e.length+",fsize="+f.length+") : "+epos);
        fe[fpos][epos] = true;
      } else {
        throw new RuntimeException("Warning: bad alignment token: "+al);
      }
    }
    
    // add the boundary alignments
    int lastf = f.length - 1;
    int laste = e.length - 1;
    fe[0][0] = true;
    fe[lastf][laste] = true;
  }
}