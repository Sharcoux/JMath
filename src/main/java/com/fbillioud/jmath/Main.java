/* 
 * Copyright 2016 François Billioud.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fbillioud.jmath;

import com.fbillioud.jmath.components.JMathDisplayer;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author François Billioud
 */
public class Main {
    public static void main(String[] args) {
        
//        final String testString = "<mrow><mrow>test</mrow><mo>{</mo><mtable columnalign=\"left\"> \n" +
//" <mtr><mtd><mrow><mn>2</mn><mi>x</mi><mn>+</mn><mi>y</mi><msqrt><mrow><mn>2</mn></mrow></msqrt><mn>=18</mn></mrow></mtd> \n" +
//" </mtr><mtr><mtd><mrow><mn>3y-</mn><mi>x</mi><mn>×</mn><mfrac><mrow><msqrt><mrow><mn>2 \n" +
//"        </mn></mrow></msqrt></mrow><mrow><mn>2</mn></mrow></mfrac><mn>=1</mn></mrow></mtd></mtr></mtable></mrow>";
//        final String testString = "<mrow>test<mo>{</mo><mtable><mtr><mtd>adsf</mtd><mtd>b</mtd></mtr><mtr><mtd>adsf</mtd><mtd>b</mtd></mtr><mtr><mtd>c</mtd><mtd>dsdfs</mtd></mtr></mtable></mrow>";
//        final String testString = "<mrow>test<mtable><mtr><mtd>adsf</mtd><mtd>b</mtd></mtr><mtr><mtd>c</mtd><mtd>dsdfs</mtd></mtr></mtable></mrow>";
//        final String testString = "<mrow>test<mmultiscripts><mi>x</mi><mi>a</mi><mn>2</mn><mprescripts /><mi>c</mi><mn>4</mn></mmultiscripts></mrow>";
//        final String testString = "<mrow>test<msubsup><mi>x</mi><mi>i</mi><mn>2</mn></msub></mrow>";
//        final String testString = "<mrow>test<msub><mi>x</mi><mn>2</mn></msub></mrow>";
//        final String testString = "<mrow>test<msup><mi>x</mi><mn>2</mn></msup></mrow>";
//        final String testString = "<mrow>test<mover><mrow>ABC</mrow><mo>^</mo></mover></mrow>";
//        final String testString = "<mrow>test<munder><mrow>ABC</mrow><mo>^</mo></munder></mrow>";
//        final String testString = "<mrow>test<mrow>b</mrow></mrow>";
//        final String testString = "<mrow>test<mrow>a<mi>a</mi><mi>b</mi></mrow></mrow>";
//        final String testString = "<mrow>test<mo>{</mo><mfrac><mi>a</mi><mi>b</mi></mfrac></mrow>";
//        final String testString = "<mrow>test<msqrt><mfrac><mn>2</mn><mfrac><mn>1</mn><mn>2</mn></mfrac></mfrac></msqrt></mrow>";
//        final String testString = "<mrow>test<msqrt><mfrac><mfrac><mn>1</mn><mn>2</mn></mfrac><mn>2</mn></mfrac></msqrt></mrow>";
//        final String testString = "<mrow>test<msqrt><mrow>2</mrow></msqrt></mrow>";
//        final String testString = "<mrow>test<msqrt>2</msqrt></mrow>";
//        final String testString = "<mrow>test<mroot><mn>2</mn><mn>3</mn></mroot></mrow>";
//        final String testString = "<mrow>test<mfrac><mn>2</mn><mn>2</mn></mfrac><mfrac><mfrac><mn>1</mn><mn>2</mn></mfrac><mn>2</mn></mfrac></mrow>";
//        final String testString = "<mrow>test<mfrac><mn>2</mn><mn>2</mn></mfrac><mfrac><mn>2</mn><mfrac><mn>1</mn><mn>2</mn></mfrac></mfrac></mrow>";
//        final String testString = "<mrow>test<mfrac><mn>2</mn><mn>2</mn></mfrac></mrow>";
//        final String testString = "<mrow>test<mfenced><mfrac><mi>a</mi><mi>b</mi></mfrac></mfenced></mrow>";
//        final String testString = "<mrow>test<msqrt><mrow>test<mfrac><mn>1</mn><mn>2</mn></mfrac></mrow></msqrt></mrow>";
//        final String testString = "<mrow>test<msqrt><mfrac><mn>1</mn><mn>2</mn></mfrac></msqrt></mrow>";
//        final String testString = "<mrow>test<mfrac><mrow>test<msqrt><mfrac><mn>2</mn><mn>1</mn></mfrac></msqrt></mrow><mn>2</mn></mfrac></mrow>";
//        final String testString = "<mrow>test<mfrac><mrow>test<msqrt><mrow>test<mfrac><mn>1</mn><mn>2</mn></mfrac></mrow></msqrt></mrow><mn>2</mn></mfrac></mrow>";
        final String testString = "<mrow><mi>test</mi><mrow><mfrac><mrow>test<mroot mathcolor=\"#ff0000\"><mrow mathcolor=\"#000000\">test<mfrac><mn>2</mn><mn>2</mn></mfrac><mfrac><mn>2</mn><mfrac><mn>1</mn><mn>2</mn></mfrac></mfrac></mrow><mn>2</mn></mroot></mrow><mrow mathcolor=\"#000000\">2</mrow></mfrac></mrow></mrow>";
//        final String testString = "<mrow><mi>test</mi><mfrac><mrow>test<msqrt><mfrac><mrow>1</mrow><mrow>2</mrow></mfrac></msqrt></mrow><mrow>2</mrow></mfrac></mrow>";
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame mainFrame = new JFrame("test");
                mainFrame.setSize(400, 300);
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                Container pane = mainFrame.getContentPane();
                pane.setLayout(new BorderLayout());
                JMathDisplayer mathComp = new JMathDisplayer(testString);
                mathComp.setFont(new Font("Arial", Font.PLAIN, 60));
                pane.add(mathComp);
//                mainFrame.setContentPane(mathComp);
                mainFrame.pack();
                mainFrame.setVisible(true);
                mathComp.setMathML("<mrow><mi>test2</mi><mfrac><mrow>test<msqrt><mfrac><mrow>1</mrow><mrow>2</mrow></mfrac></msqrt></mrow><mrow>2</mrow></mfrac></mrow>");
            }
        });
    }
}
