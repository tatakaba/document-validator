/**
l * DocumentValidator
 * Copyright (c) 2013-, Takahiko Ito, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package org.unigram.docvalidator.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.unigram.docvalidator.store.FileContent;
import org.unigram.docvalidator.store.Paragraph;
import org.unigram.docvalidator.store.Section;
import org.unigram.docvalidator.store.Sentence;
import org.unigram.docvalidator.util.DocumentValidatorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for wiki formatted file.
 */
public final class WikiParser extends BasicDocumentParser {
  /**
   * Constructor.
   */
  public WikiParser() {
    super();
  }

  public FileContent generateDocument(String fileName)
      throws DocumentValidatorException {
    InputStream inputStream = this.loadStream(fileName);
    return this.generateDocument(inputStream);
  }

  public FileContent generateDocument(InputStream is) {
    BufferedReader br = createReader(is);
    if (br == null) {
      LOG.error("Failed to create reader");
      return null;
    }

    FileContent fileContent = new FileContent();
    // for sentences right below the beginning of document
    List<Sentence> headers = new ArrayList<Sentence>();
    Section currentSection = new Section(0, headers);
    fileContent.appendSection(currentSection);
    LinePattern prevPattern, currentPattern = LinePattern.VOID;
    String line;
    int lineNum = 0;
    String remain = "";
    try {
      while ((line = br.readLine()) != null) {
        prevPattern = currentPattern;
        Vector<String> head = new Vector<String>();
        if (currentPattern == LinePattern.COMMENT) {
          if (check(END_COMMENT_PATTERN, line, head)) {
            currentPattern = LinePattern.VOID;
          }
        } else if (check(HEADER_PATTERN, line, head)) {
          currentPattern = LinePattern.HEADER;
          currentSection = appendSection(fileContent, currentSection, head,
              lineNum);
        } else if (check(LIST_PATTERN, line, head)) {
          currentPattern = LinePattern.LIST;
          appendListElement(currentSection, prevPattern, head, lineNum);
        } else if (check(NUMBERED_LIST_PATTERN, line, head)) {
          currentPattern = LinePattern.LIST;
          appendListElement(currentSection, prevPattern, head, lineNum);
        } else if (check(BEGIN_COMMENT_PATTERN, line, head)) {
          if (!check(END_COMMENT_PATTERN, line, head)) { // skip comment
            currentPattern = LinePattern.COMMENT;
          }
        } else if (line.equals("")) { // new paragraph content
          currentSection.appendParagraph(new Paragraph());
        } else { // usual sentence.
          currentPattern = LinePattern.SENTENCE;
          remain = appendSentencesIntoSection(lineNum, remain + line,
              currentSection);
        }
        prevPattern = currentPattern;
        lineNum++;
      }
    } catch (IOException e) {
      LOG.error("Failed to parse input document: " + e.getMessage());
      return null;
    }
    if (remain.length() > 0) {
      appendLastSentence(fileContent, lineNum, remain);
    }
    return fileContent;
  }

  private void appendListElement(Section currentSection,
      LinePattern prevPattern, Vector<String> head, int lineNum) {
    if (prevPattern != LinePattern.LIST) {
      currentSection.appendListBlock();
    }
    List<Sentence> outputSentences = new ArrayList<Sentence>();
    String remainSentence= obtainSentences(0, head.get(1), outputSentences);
    currentSection.appendListElement(extractListLevel(head.get(0)),
        outputSentences);
    // NOTE: for list content without period
    if (remainSentence != null && remainSentence.length() > 0) {
      outputSentences.add(new Sentence(remainSentence, lineNum));
    }
  }

  private Section appendSection(FileContent fileContent,
      Section currentSection, Vector<String> head, int lineNum) {
    Integer level = Integer.valueOf(head.get(0));
    List<Sentence> outputSentences = new ArrayList<Sentence>();
    String remainHeader = obtainSentences(0, head.get(1), outputSentences);
    // NOTE: for header without period
    if (remainHeader != null && remainHeader.length() > 0) {
      outputSentences.add(new Sentence(remainHeader, lineNum));
    }

    Section tmpSection =  new Section(level, outputSentences);
    fileContent.appendSection(tmpSection);
    if (!addChild(currentSection, tmpSection)) {
      LOG.warn("Failed to add parent for a Seciotn: "
          + tmpSection.getHeaderContents().next());
    }
    currentSection = tmpSection;
    return currentSection;
  }

  private void appendLastSentence(FileContent doc, int lineNum, String remain) {
    Sentence sentence = new Sentence(remain, lineNum);
    parseSentence(sentence); // extract inline elements
    doc.getLastSection().appendSentence(sentence);
  }

  private void parseSentence(Sentence sentence) {
    extractLinks(sentence);
    removeTags(sentence);
  }

  private void removeTags(Sentence sentence) {
    String content = sentence.content;
    for (int i = 0; i< INLINE_PATTERNS.length; ++i) {
      Matcher m = INLINE_PATTERNS[i].matcher(content);
      content= m.replaceAll("$1");
    }
    sentence.content = content;
  }

  private void extractLinks(Sentence sentence) {
    String modContent = "";
    int start = 0;
    Matcher m = LINK_PATTERN.matcher(sentence.content);

    while (m.find()) {
      String[] tagInternal = m.group(1).split("\\|");
      String tagURL = tagInternal[0].trim();
      if (tagInternal.length > 2) {
        modContent += sentence.content.substring(
            start, m.start()) + tagInternal[1].trim();
      } else {
        modContent += sentence.content.substring(start, m.start())
            + tagURL.trim();
      }
      sentence.links.add(tagURL);
      start = m.end();
    }

    if (start > 0) {
      modContent += sentence.content.substring(
          start, sentence.content.length());
      sentence.content = modContent;
    }
  }

  private boolean addChild(Section candidate, Section child) {
    if (candidate.getLevel() < child.getLevel()) {
      candidate.appendSubSection(child);
      child.setParentSection(candidate);
    } else { // search parent
      Section parent = candidate.getParentSection();
      while (parent != null) {
        if (parent.getLevel() < child.getLevel()) {
          parent.appendSubSection(child);
          child.setParentSection(parent);
          candidate = child;
          break;
        }
        parent = parent.getParentSection();
      }
      if (parent == null) {
        return false;
      }
    }
    return true;
  }

  private String obtainSentences(int lineNum, String line,
      List<Sentence> outputSentences) {
    String remain = ParseUtils.extractSentences(line, this.period,
        outputSentences);
    for (Sentence sentence : outputSentences) {
      sentence.position = lineNum;
      parseSentence(sentence); // extract inline elements
    }
    return remain;
  }

  private String appendSentencesIntoSection(int lineNum, String line,
      Section currentSection) {
  List<Sentence> outputSentences = new ArrayList<Sentence>();
  String remain = obtainSentences(lineNum, line, outputSentences);

  for (Sentence sentence : outputSentences) {
    currentSection.appendSentence(sentence);
  }
  return remain;
}

  private static boolean check(Pattern p, String target, Vector<String> head) {
    Matcher m = p.matcher(target);
    if (m.matches()) {
      for (int i = 1; i <= m.groupCount(); i++) {
        head.add(m.group(i));
      }
      return true;
    } else {
      return false;
    }
  }

  private int extractListLevel(String listPrefix) {
    return listPrefix.length();
  }

  private static Logger LOG = LoggerFactory.getLogger(WikiParser.class);

  /**
   * List of elements used in wiki format.
   */
  private enum LinePattern {
    SENTENCE, LIST, NUM_LIST, VOID, HEADER, COMMENT
  }

  /****************************************************************************
   * patterns to handle wiki syntax
   ***************************************************************************/

  private static final Pattern HEADER_PATTERN
  = Pattern.compile("^h([1-6])\\. (.*)$");

  private static final Pattern LIST_PATTERN = Pattern.compile("^(-+) (.*)$");

  private static final Pattern NUMBERED_LIST_PATTERN =
      Pattern.compile("^(#+) (.*)$");

  private static final Pattern LINK_PATTERN =
      Pattern.compile("\\[\\[(.+?)\\]\\]");

  private static final Pattern BEGIN_COMMENT_PATTERN =
      Pattern.compile("\\s*^\\[!--");

  private static final Pattern END_COMMENT_PATTERN =
      Pattern.compile("--\\]$\\s*");

  private static final Pattern ITALIC_PATTERN =
      Pattern.compile("//(.+?)//");

  private static final Pattern UNDERLINE_PATTERN =
      Pattern.compile("__(.+?)__");

  private static final Pattern BOLD_PATTERN =
      Pattern.compile("\\*\\*(.+?)\\*\\*");

  private static final Pattern STRIKETHROUGH_PATTERN =
      Pattern.compile("--(.+?)--");

  private static final Pattern [] INLINE_PATTERNS = {
    ITALIC_PATTERN,
    BOLD_PATTERN,
    UNDERLINE_PATTERN,
    STRIKETHROUGH_PATTERN
  };
}
