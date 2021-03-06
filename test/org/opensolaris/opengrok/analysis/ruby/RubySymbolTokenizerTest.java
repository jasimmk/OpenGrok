/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2017, Chris Fraire <cfraire@me.com>.
 */

package org.opensolaris.opengrok.analysis.ruby;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.opensolaris.opengrok.analysis.JFlexTokenizer;

/**
 * Tests the {@link RubySymbolTokenizer} class.
 */
public class RubySymbolTokenizerTest {

    /**
     * Test sample.rb v. samplesymbols.txt
     * @throws java.lang.Exception thrown on error
     */
    @Test
    public void testRubySymbolStream() throws Exception {
        InputStream rbres = getClass().getClassLoader().getResourceAsStream(
            "org/opensolaris/opengrok/analysis/ruby/sample.rb");
        assertNotNull("despite sample.rb as resource,", rbres);
        InputStream wdsres = getClass().getClassLoader().getResourceAsStream(
            "org/opensolaris/opengrok/analysis/ruby/samplesymbols.txt");
        assertNotNull("despite samplesymbols.txt as resource,", wdsres);

        List<String> expectedSymbols = new ArrayList<>();
        try (BufferedReader wdsr = new BufferedReader(new InputStreamReader(
            wdsres, "UTF-8"))) {
            String line;
            while ((line = wdsr.readLine()) != null) {
                int hasho = line.indexOf('#');
                if (hasho != -1) line = line.substring(0, hasho);
                expectedSymbols.add(line.trim());
            }            
        }

        testSymbolStream(RubySymbolTokenizer.class, rbres, expectedSymbols);
    }

    /**
     * Runs the test on one single implementation class with the specified
     * input text and expected tokens.
     */
    private void testSymbolStream(Class<? extends JFlexTokenizer> klass,
        InputStream iss, List<String> expectedTokens)
        throws Exception {

        JFlexTokenizer tokenizer = klass.getConstructor(Reader.class).
            newInstance(new InputStreamReader(iss, "UTF-8"));

        CharTermAttribute term = tokenizer.addAttribute(
            CharTermAttribute.class);

        int count = 0;
        while (tokenizer.incrementToken()) {
            assertTrue("too many tokens at term" + (1 + count) + ": " +
                term.toString(), count < expectedTokens.size());
            String expected = expectedTokens.get(count);
            // 1-based offset to accord with line #
            assertEquals("term" + (1 + count), expected, term.toString());
            count++;
        }

        assertEquals("wrong number of tokens", expectedTokens.size(), count);
    }
}
